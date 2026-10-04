package edu.regis.merc.svc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.Account;
import edu.regis.merc.model.RecoveryRequest;
import edu.regis.merc.model.SecurityQuestion;
import edu.regis.merc.test.LogCapture;
import edu.regis.merc.view.ForgotPasswordPanel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import static org.junit.jupiter.api.Assertions.*;

@ResourceLock("console-and-loggers")
class RecoverySvcTest {
    private final Gson gson = new Gson();

    @Test
    void allQuestionsReturnOnlyPublicPromptAndPreserveLegacyIds() {
        assertEquals(0, SecurityQuestion.BIRTH_CITY.getId());
        assertEquals(1, SecurityQuestion.MOTHERS_MAIDEN_NAME.getId());
        assertEquals(2, SecurityQuestion.FIRST_CAR_MAKE.getId());
        assertEquals(3, SecurityQuestion.STREET_AT_TEN.getId());
        for (SecurityQuestion question : SecurityQuestion.values()) {
            Account account = account(question.getId());
            account.setPassword("TEST_PASSWORD_DO_NOT_LOG");
            account.setSecurityAnswer("TEST_ANSWER_HASH_DO_NOT_LOG");
            RecoverySvc service = new RecoverySvc(id -> account);
            try (LogCapture logs = new LogCapture(RecoverySvc.class)) {
                TutorReply reply = service.questionFor(account.getUserId());
                assertEquals("SecurityQuestion", reply.getStatus());
                JsonObject data = gson.fromJson(reply.getData(), JsonObject.class);
                assertEquals(2, data.size());
                assertEquals(question.getId(), data.get("questionId").getAsInt());
                assertEquals(question.toString(), data.get("text").getAsString());
                assertFalse(reply.getData().contains("TEST_"));
                assertFalse(logs.output().contains("TEST_"));
            }
        }
    }

    @Test
    void unknownAccountAndInvalidStoredQuestionAreUnavailable() {
        RecoverySvc unknown = new RecoverySvc(id -> { throw new ObjNotFoundException("missing"); });
        RecoverySvc invalid = new RecoverySvc(id -> account(999));
        assertEquals("RecoveryUnavailable", unknown.questionFor("missing@example.com").getStatus());
        assertEquals("RecoveryUnavailable", invalid.questionFor("student@example.com").getStatus());
        assertEquals("RecoveryUnavailable", unknown.questionFor(null).getStatus());
        assertEquals("RecoveryUnavailable", unknown.questionFor(" ").getStatus());
    }

    @Test
    void databaseErrorsReturnSafeMessage() {
        RecoverySvc service = new RecoverySvc(id -> {
            throw new NonRecoverableException("Database connection unavailable");
        });
        TutorReply reply = service.questionFor("student@example.com");
        assertEquals(":ERR", reply.getStatus());
        assertFalse(reply.getData().contains("Database connection unavailable"));
    }

    @Test
    void verifiesStoredAnswerWithoutClientQuestionSelection() throws Exception {
        Account account = account(3);
        account.setSecurityAnswer(ForgotPasswordPanel.encryptSHA256("Oak Street"));
        RecoverySvc service = new RecoverySvc(id -> {
            assertEquals("student@example.com", id);
            return account;
        });
        // A forged legacy question field is ignored; only the stored account matters.
        RecoveryRequest request = gson.fromJson("{\"userId\":\"student@example.com\","
                + "\"securityQuestion\":0,\"securityAnswer\":\"" + account.getSecurityAnswer() + "\"}",
                RecoveryRequest.class);
        assertSame(account, service.verifyAnswer(request));
        assertNull(service.verifyAnswer(new RecoveryRequest("student@example.com", "wrong")));
        assertNull(service.verifyAnswer(new RecoveryRequest("student@example.com", null)));
        assertNull(service.verifyAnswer(null));
        account.setSecurityQuestion(999);
        assertNull(service.verifyAnswer(request));
    }

    @Test
    void existingAnswerHashFormatIsUnchanged() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                ForgotPasswordPanel.encryptSHA256("abc"));
    }

    private Account account(int questionId) {
        Account account = new Account("student@example.com");
        account.setSecurityQuestion(questionId);
        return account;
    }
}
