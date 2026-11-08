package edu.regis.merc.svc;

import com.google.gson.Gson;
import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.Account;
import edu.regis.merc.model.RecoveryRequest;
import edu.regis.merc.model.SecurityQuestion;
import edu.regis.merc.model.SecurityQuestionPrompt;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Looks up recovery questions and checks answers against server-side accounts. */
public class RecoverySvc {
    @FunctionalInterface
    public interface AccountLookup {
        Account retrieve(String userId) throws ObjNotFoundException, NonRecoverableException;
    }

    private final AccountLookup accounts;

    public RecoverySvc(AccountLookup accounts) {
        this.accounts = accounts;
    }

    public TutorReply questionFor(String userId) {
        if (userId == null || userId.isBlank()) {
            return unavailable();
        }
        try {
            Account account = accounts.retrieve(userId);
            SecurityQuestion question = SecurityQuestion.fromId(account.getSecurityQuestion());
            return new TutorReply("SecurityQuestion",
                    new Gson().toJson(new SecurityQuestionPrompt(question)));
        } catch (ObjNotFoundException | IllegalArgumentException ex) {
            return unavailable();
        } catch (NonRecoverableException ex) {
            return new TutorReply(":ERR", "Unable to load the security question. Please try again.");
        }
    }

    /** Returns the stored account only after its answer has been verified. */
    public Account verifyAnswer(RecoveryRequest request)
            throws ObjNotFoundException, NonRecoverableException {
        if (request == null || request.getUserId() == null || request.getUserId().isBlank()
                || request.getSecurityAnswer() == null || request.getSecurityAnswer().isBlank()) {
            return null;
        }
        Account account = accounts.retrieve(request.getUserId());
        try {
            SecurityQuestion.fromId(account.getSecurityQuestion());
        } catch (IllegalArgumentException ex) {
            return null;
        }
        String expected = account.getSecurityAnswer();
        if (expected == null || expected.isBlank()) {
            return null;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                request.getSecurityAnswer().getBytes(StandardCharsets.UTF_8)) ? account : null;
    }

    private TutorReply unavailable() {
        return new TutorReply("RecoveryUnavailable",
                "Unable to recover this account. Check your email or contact support.");
    }
}
