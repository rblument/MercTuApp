package edu.regis.merc.svc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.model.*;
import java.lang.reflect.Proxy;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LogoutSessionTest {
    private static final String EMAIL = "student@example.com";
    private final Gson gson = new Gson();

    private final class Fixture {
        private final Account account = new Account(EMAIL);
        private final TutoringSession saved = new TutoringSession(new Student(account));
        private String stored = SecurityTokens.generate();
        private boolean failDatabase;
        private int rotations;
        private final MercTutor tutor;

        private Fixture() {
            account.setSecurityAnswer("existing-answer-hash");
            account.setSecurityQuestion(0);
            saved.setId(42);
            saved.setSecurityToken(stored);
            Task task = new Task(70);
            Step step = new Step(120);
            task.addStep(step);
            PendingTask pending = new PendingTask(task);
            PendingStep current = new PendingStep(300, step);
            current.setCurrentHintIndex(2);
            pending.setCurrentStep(current);
            saved.addTask(pending);
            SessionSvc sessions = proxy(SessionSvc.class, (name, args) -> {
                if (failDatabase) throw new NonRecoverableException("Test database unavailable");
                switch (name) {
                    case "retrieve": saved.setSecurityToken(stored); return saved;
                    case "retrieveSecurityToken": return stored;
                    case "updateCurrentStep":
                    case "updateCurrentTask": return null;
                    case "replaceSecurityToken": stored = (String) args[1]; rotations++; return null;
                    case "revokeSecurityToken":
                        if (!SecurityTokens.matches(stored, (String) args[1])) return false;
                        stored = "";
                        return true;
                    default: throw new AssertionError("Unexpected persistence operation: " + name);
                }
            });
            AccountSvc accounts = proxy(AccountSvc.class, (name, args) -> {
                switch (name) {
                    case "validatePassword": return "correct-password".equals(args[1]) ? Optional.of(account) : Optional.empty();
                    case "exists": return true;
                    case "retrieve": return account;
                    case "update": return null;
                    default: throw new AssertionError("Unexpected account operation: " + name);
                }
            });
            StudentModelSvc models = proxy(StudentModelSvc.class, (name, args) -> {
                if (name.equals("retrieve")) return new StudentModel(EMAIL);
                throw new AssertionError("Unexpected model operation: " + name);
            });
            tutor = new MercTutor(sessions, accounts, models);
        }

        private TutorReply logout(String token) {
            ClientRequest request = new ClientRequest(ServerRequestType.LOGOUT);
            request.setUserId(EMAIL);
            request.setSecurityToken(token);
            return tutor.request(request);
        }

        private TutorReply login() {
            return tutor.signIn("{\"userId\":\"" + EMAIL + "\",\"password\":\"correct-password\"}");
        }
    }

    @Test void logoutRevokesOldTokenAndLoginRestoresProgressWithFreshToken() {
        Fixture f = new Fixture();
        String old = f.stored;
        assertEquals("LoggedOut", f.logout(old).getStatus());
        assertEquals("", f.stored);
        assertFalse(SecurityTokens.matches(f.stored, old));
        TutorReply reply = f.login();
        assertEquals("Authenticated", reply.getStatus());
        assertNotEquals(old, f.stored);
        JsonObject restored = gson.fromJson(reply.getData(), JsonObject.class);
        assertEquals(f.stored, restored.get("securityToken").getAsString());
        assertEquals(42, restored.get("id").getAsInt());
        JsonObject task = restored.getAsJsonArray("tasks").get(0).getAsJsonObject();
        assertEquals(70, task.getAsJsonObject("task").get("id").getAsInt());
        assertEquals(300, task.getAsJsonObject("currentStep").get("id").getAsInt());
        assertEquals(120, task.getAsJsonObject("currentStep").getAsJsonObject("step").get("id").getAsInt());
        assertEquals(2, task.getAsJsonObject("currentStep").get("currentHintIndex").getAsInt());
        assertEquals(1, f.rotations);
    }

    @Test void delayedLogoutCannotRevokeNewLogin() {
        Fixture f = new Fixture();
        String old = f.stored;
        assertEquals("Authenticated", f.login().getStatus());
        String current = f.stored;
        assertEquals("InvalidToken", f.logout(old).getStatus());
        assertEquals(current, f.stored);
        assertEquals("LoggedOut", f.logout(current).getStatus());
    }

    @Test void blankWrongAndRevokedTokensAreRejectedByAuthorization() {
        Fixture f = new Fixture();
        String revoked = f.stored;
        f.logout(revoked);
        for (String token : new String[] {null, "", " ", "wrong", revoked}) {
            assertEquals("InvalidToken", f.logout(token).getStatus());
            ClientRequest protectedRequest = new ClientRequest(ServerRequestType.RESET_PASSWORD);
            protectedRequest.setUserId(EMAIL);
            protectedRequest.setSecurityToken(token);
            protectedRequest.setData("{}");
            assertEquals(":ERR", f.tutor.request(protectedRequest).getStatus());
        }
        assertFalse(SecurityTokens.matches("", ""));
    }

    @Test void recoveryAfterLogoutIssuesUsableTokenAndPasswordResetStillWorks() throws Exception {
        Fixture f = new Fixture();
        String old = f.stored;
        f.logout(old);
        Account recovery = new Account(EMAIL);
        recovery.setSecurityQuestion(0);
        recovery.setSecurityAnswer("existing-answer-hash");
        TutorReply reply = f.tutor.verifyUser(gson.toJson(recovery));
        assertEquals("Verified", reply.getStatus());
        String token = gson.fromJson(reply.getData(), String.class);
        assertFalse(token.isBlank());
        assertNotEquals(old, token);
        assertTrue(SecurityTokens.matches(f.stored, token));
        ClientRequest reset = new ClientRequest(ServerRequestType.RESET_PASSWORD);
        reset.setUserId(EMAIL);
        reset.setSecurityToken(token);
        reset.setData("{\"userId\":\"" + EMAIL + "\",\"password\":\"replacement-password\"}");
        assertEquals("PasswordReset", f.tutor.request(reset).getStatus());
        assertEquals(300, f.saved.currentTask().currentStep().getId());
    }

    @Test void failedAuthenticationDoesNotRotateToken() {
        Fixture f = new Fixture();
        String old = f.stored;
        assertEquals("InvalidPassword", f.tutor.signIn("{\"userId\":\"" + EMAIL + "\",\"password\":\"wrong\"}").getStatus());
        assertEquals(old, f.stored);
        assertEquals(0, f.rotations);
    }

    @Test void databaseErrorsNeverReportLogoutOrLoginSuccess() {
        Fixture f = new Fixture();
        String old = f.stored;
        f.failDatabase = true;
        assertEquals(":ERR", f.logout(old).getStatus());
        assertEquals(old, f.stored);
        assertEquals(":ERR", f.login().getStatus());
    }

    @Test void newTokensAreNonblankDistinctAndCaseSensitive() {
        String first = SecurityTokens.generate();
        String second = SecurityTokens.generate();
        assertEquals(64, first.length());
        assertNotEquals(first, second);
        assertFalse(SecurityTokens.matches("aBc", "AbC"));
    }

    @Test
    @org.junit.jupiter.api.parallel.ResourceLock("console-and-loggers")
    void logoutLogsRequestTypeWithoutCredentialsOrPayload() {
        Fixture f = new Fixture();
        String token = f.stored;
        ClientRequest request = new ClientRequest(ServerRequestType.LOGOUT);
        request.setUserId(EMAIL);
        request.setSecurityToken(token);
        request.setData("{\"password\":\"TEST_PASSWORD_DO_NOT_LOG\",\"securityAnswer\":\"TEST_ANSWER_DO_NOT_LOG\"}");
        try (edu.regis.merc.test.LogCapture logs = new edu.regis.merc.test.LogCapture(MercTutor.class)) {
            assertEquals("LoggedOut", f.tutor.request(request).getStatus());
            String output = logs.output();
            assertTrue(output.contains(":Logout"));
            assertFalse(output.contains(token));
            assertFalse(output.contains("TEST_PASSWORD_DO_NOT_LOG"));
            assertFalse(output.contains("TEST_ANSWER_DO_NOT_LOG"));
            assertFalse(output.contains(request.getData()));
        }
    }

    @Test void stepAndTaskAdvancementSurviveRepeatedLogoutAndTokenRotation() {
        Fixture f = new Fixture();
        Problem problem = new Problem(0);
        for (int taskIndex = 0; taskIndex < 2; taskIndex++) {
            Task task = new Task(70 + taskIndex);
            task.setSequenceIndex(taskIndex);
            int first = taskIndex == 0 ? 50 : 52;
            int last = taskIndex == 0 ? 51 : 54;
            for (int stepId = first; stepId <= last; stepId++) {
                Step step = new Step(stepId);
                step.setData("{\"representation\":\"LAMBDA_CALCULUS\",\"correctComponentId\":101}");
                task.addStep(step);
            }
            problem.addTask(task);
        }
        f.saved.setProblem(problem);
        PendingTask initial = new PendingTask(problem.findTaskBySequence(0));
        initial.setCurrentStep(new PendingStep(300, initial.getTask().getSteps().get(0)));
        f.saved.setTasks(new java.util.ArrayList<>(java.util.List.of(initial)));
        for (int stepId = 50; stepId < 54; stepId++) {
            ClientRequest answer = new ClientRequest(ServerRequestType.COMPLETED_STEP);
            answer.setUserId(EMAIL);
            answer.setSecurityToken(f.stored);
            StudentPayload payload = new StudentPayload(stepId);
            payload.setRepresentation(RepresentationKind.LAMBDA_CALCULUS);
            payload.setSelectedComponentId(101);
            answer.setData(gson.toJson(payload));
            assertEquals(stepId == 51 ? "TaskComplete" : "Success", f.tutor.request(answer).getStatus());
            assertEquals(stepId + 1, f.saved.currentTask().currentStep().getStep().getId());
            String oldToken = f.stored;
            assertEquals("LoggedOut", f.logout(oldToken).getStatus());
            TutorReply login = f.login();
            assertEquals("Authenticated", login.getStatus());
            assertNotEquals(oldToken, f.stored);
            JsonObject restored = gson.fromJson(login.getData(), JsonObject.class);
            JsonObject task = restored.getAsJsonArray("tasks").get(0).getAsJsonObject();
            assertEquals(stepId + 1, task.getAsJsonObject("currentStep").getAsJsonObject("step").get("id").getAsInt());
            assertEquals(42, restored.get("id").getAsInt());
        }
    }

    private interface Call { Object invoke(String name, Object[] args) throws Throwable; }

    private static <T> T proxy(Class<T> type, Call call) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
                (object, method, args) -> call.invoke(method.getName(), args)));
    }
}
