package edu.regis.merc.dao;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import edu.regis.merc.model.Account;
import edu.regis.merc.model.PendingStep;
import edu.regis.merc.model.PendingTask;
import edu.regis.merc.model.Problem;
import edu.regis.merc.model.RecoveryRequest;
import edu.regis.merc.model.RepresentationKind;
import edu.regis.merc.model.SignInRequest;
import edu.regis.merc.model.Task;
import edu.regis.merc.model.TutoringSession;
import edu.regis.merc.svc.ClientRequest;
import edu.regis.merc.svc.MercTutor;
import edu.regis.merc.svc.ServerRequestType;
import edu.regis.merc.svc.StudentPayload;
import edu.regis.merc.svc.TutorReply;
import edu.regis.merc.view.act.SignInAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Real authentication and persisted progress, without sockets or native Swing windows. */
public class LogoutResumeIntegrationTest extends BaseMysqlIT {
    private final Gson gson = SignInAction.createSessionGson();
    private final SessionDAO sessions = new SessionDAO();

    private final class Fixture {
        private final String user = "logout_it_" + UUID.randomUUID() + "@example.invalid";
        private final String password = UUID.randomUUID().toString();
        private final String answer = UUID.randomUUID().toString();

        private Fixture() throws Exception {
            Account account = new Account(user);
            account.setPassword(password);
            account.setSecurityQuestion(0);
            account.setSecurityAnswer(answer);
            assertEquals("Created", new MercTutor().createAccount(gson.toJson(account)).getStatus());
        }

        private TutoringSession login() throws Exception {
            String previous = sessions.retrieveSecurityToken(user);
            TutorReply reply = new MercTutor().signIn(gson.toJson(new SignInRequest(user, password)));
            assertEquals("Authenticated", reply.getStatus());
            TutoringSession restored = gson.fromJson(reply.getData(), TutoringSession.class);
            assertFalse(restored.getSecurityToken().isBlank());
            assertFalse(previous.equals(restored.getSecurityToken()), "Successful login must rotate the token");
            assertStoredToken(restored.getSecurityToken());
            return restored;
        }

        private TutorReply request(ServerRequestType type, String token, String data) {
            ClientRequest request = new ClientRequest(type);
            request.setUserId(user);
            request.setSecurityToken(token);
            request.setData(data);
            return new MercTutor().request(request);
        }

        private void assertStoredToken(String expected) throws Exception {
            // Boolean assertions avoid printing credentials if a regression occurs.
            assertTrue(expected.equals(sessions.retrieveSecurityToken(user)), "Stored token differs from expected lifecycle state");
        }

        private void assertRejected(String token) {
            assertEquals(":ERR", request(ServerRequestType.REQUEST_HINT, token, "{\"stepId\":50,\"hintIndex\":0}").getStatus());
        }
    }

    @Test public void logoutLoginRestoresEveryStepAndCompletesTheSeededUnit() throws Exception {
        Fixture f = new Fixture();
        TutoringSession current = f.login();
        int sessionId = current.getId();
        long startDate = current.getStartDate().getTimeInMillis();
        int[] steps = {50, 51, 52, 53, 54};
        int[] answers = {101, 102, 245, 243, 246};
        int[] nextSteps = {51, 52, 53, 54, 54};
        String[] statuses = {"Success", "ProblemComplete", "Success", "Success", "UnitComplete"};
        for (int i = 0; i < steps.length; i++) {
            assertEquals(steps[i], current.currentTask().currentStep().getStep().getId());
            StudentPayload payload = new StudentPayload(steps[i]);
            payload.setRepresentation(RepresentationKind.LAMBDA_CALCULUS);
            payload.setSelectedComponentId(answers[i]);
            TutorReply reply = f.request(ServerRequestType.COMPLETED_STEP, current.getSecurityToken(), gson.toJson(payload));
            assertEquals(statuses[i], reply.getStatus());
            TutoringSession returned = gson.fromJson(reply.getData(), TutoringSession.class);
            TutoringSession saved = sessions.retrieve(f.user);
            assertPosition(returned, nextSteps[i], i == 0 ? 10 : 11);
            assertPosition(saved, nextSteps[i], i == 0 ? 10 : 11);
            assertEquals(i == 0 ? 0 : 1, saved.getProblem().getId());
            String oldToken = current.getSecurityToken();
            assertEquals("LoggedOut", f.request(ServerRequestType.LOGOUT, oldToken, "").getStatus());
            f.assertStoredToken("");
            f.assertRejected(oldToken);
            current = f.login();
            assertEquals(sessionId, current.getId());
            assertEquals(startDate, current.getStartDate().getTimeInMillis());
            assertPosition(current, nextSteps[i], i == 0 ? 10 : 11);
            assertEquals("InvalidToken", f.request(ServerRequestType.LOGOUT, oldToken, "").getStatus());
            f.assertStoredToken(current.getSecurityToken());
        }
        assertTrue(current.currentTask().currentStep().isCompleted());
        try (Connection conn = DriverManager.getConnection(MySqlDAO.getURL());
                PreparedStatement statement = conn.prepareStatement(
                        "SELECT StepId FROM PendingStep WHERE SessionId = ? AND IsCompleted = 1 ORDER BY StepId")) {
            statement.setInt(1, sessionId);
            List<Integer> completed = new ArrayList<>();
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) completed.add(result.getInt(1));
            }
            assertEquals(List.of(50, 51, 52, 53, 54), completed);
        }
    }

    @Test public void loginRotationRejectsOldBlankAndDelayedLogoutTokens() throws Exception {
        Fixture f = new Fixture();
        String first = f.login().getSecurityToken();
        String newest = f.login().getSecurityToken();
        assertEquals("InvalidToken", f.request(ServerRequestType.LOGOUT, first, "").getStatus());
        f.assertStoredToken(newest);
        for (String invalid : new String[] {null, "", " ", first}) f.assertRejected(invalid);
        assertEquals("LoggedOut", f.request(ServerRequestType.LOGOUT, newest, "").getStatus());
        f.assertRejected(newest);
        assertEquals("InvalidToken", f.request(ServerRequestType.LOGOUT, newest, "").getStatus());
        f.assertStoredToken("");
    }

    @Test public void staleProgressCannotRestoreRevokedOrSupersededTokens() throws Exception {
        for (boolean revoked : new boolean[] {true, false}) {
            Fixture f = new Fixture();
            TutoringSession stale = f.login();
            String oldToken = stale.getSecurityToken();
            String expected;
            if (revoked) {
                assertEquals("LoggedOut", f.request(ServerRequestType.LOGOUT, oldToken, "").getStatus());
                expected = "";
            } else {
                expected = f.login().getSecurityToken();
            }
            // Model a request already authenticated before logout or the newer login.
            stale.currentTask().currentStep().setIsCompleted(true);
            assertTrue(stale.currentTask().advanceStep());
            sessions.updateCurrentStep(stale);
            f.assertStoredToken(expected);
            assertPosition(sessions.retrieve(f.user), 51, 10);
            Problem next = new ProblemDAO().retrieve(1);
            Task task = next.findTaskBySequence(0);
            PendingTask pending = new PendingTask(task);
            pending.setCurrentStep(new PendingStep(task.getCurrentStep()));
            stale.setProblem(next);
            stale.setTasks(new ArrayList<>(List.of(pending)));
            sessions.updateCurrentTask(stale);
            sessions.update(stale);
            f.assertStoredToken(expected);
            f.assertRejected(oldToken);
            assertPosition(f.login(), 52, 11);
        }
    }

    @Test public void passwordRecoveryAfterLogoutKeepsQuestionAndProgress() throws Exception {
        Fixture f = new Fixture();
        TutoringSession original = f.login();
        assertEquals("LoggedOut", f.request(ServerRequestType.LOGOUT, original.getSecurityToken(), "").getStatus());
        MercTutor tutor = new MercTutor();
        assertEquals("SecurityQuestion", tutor.getSecurityQuestion(gson.toJson(new RecoveryRequest(f.user, null))).getStatus());
        TutorReply recovery = tutor.verifyUser(gson.toJson(new RecoveryRequest(f.user, f.answer)));
        assertEquals("Verified", recovery.getStatus());
        String recoveryToken = gson.fromJson(recovery.getData(), String.class);
        f.assertStoredToken(recoveryToken);
        f.assertRejected(original.getSecurityToken());
        Account replacement = new Account(f.user);
        String newPassword = UUID.randomUUID().toString();
        replacement.setPassword(newPassword);
        assertEquals("PasswordReset", f.request(ServerRequestType.RESET_PASSWORD, recoveryToken, gson.toJson(replacement)).getStatus());
        TutorReply login = tutor.signIn(gson.toJson(new SignInRequest(f.user, newPassword)));
        assertEquals("Authenticated", login.getStatus());
        TutoringSession restored = gson.fromJson(login.getData(), TutoringSession.class);
        assertEquals(original.getId(), restored.getId());
        assertPosition(restored, 50, 10);
        Account saved = new AccountDAO().retrieve(f.user);
        assertEquals(0, saved.getSecurityQuestion());
        assertTrue(f.answer.equals(saved.getSecurityAnswer()), "Recovery must preserve the existing answer");
    }

    private void assertPosition(TutoringSession session, int stepId, int taskId) {
        assertEquals(stepId, session.currentTask().currentStep().getStep().getId());
        assertEquals(taskId, session.currentTask().getTask().getId());
    }
    // Unique fixtures remain in the disposable database for inspection; existing suites clean up their own fixtures.
}
