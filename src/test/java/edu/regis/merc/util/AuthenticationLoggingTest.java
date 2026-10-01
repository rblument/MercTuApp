package edu.regis.merc.util;

import edu.regis.merc.test.LogCapture;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import static org.junit.jupiter.api.Assertions.*;

@ResourceLock("console-and-loggers")
class AuthenticationLoggingTest {
    @Test
    void successfulLoginIncludesUserIdAtInfoLevel() {
        try (LogCapture logs = new LogCapture(AuthenticationLogger.class)) {
            AuthenticationLogger.signInSucceeded("student@example.com");
            assertEquals(1, logs.records.size());
            assertEquals(Level.INFO, logs.records.get(0).getLevel());
            assertTrue(logs.output().contains("Sign-in successful; userId=student@example.com"));
        }
    }

    @Test
    void failedLoginIncludesUserIdAtWarningLevel() {
        try (LogCapture logs = new LogCapture(AuthenticationLogger.class)) {
            AuthenticationLogger.signInFailed("student@example.com");
            assertEquals(1, logs.records.size());
            assertEquals(Level.WARNING, logs.records.get(0).getLevel());
            assertTrue(logs.output().contains("Sign-in failed: invalid credentials; userId=student@example.com"));
        }
    }

    @Test
    void allOutcomesSanitizeControlCharactersAndUnicodeLineSeparators() {
        try (LogCapture logs = new LogCapture(AuthenticationLogger.class)) {
            String userId = "student@example.com\r\nINFO Forged\t\u001b\u0085\u2028\u2029";
            AuthenticationLogger.signInSucceeded(userId);
            AuthenticationLogger.signInFailed(userId);
            AuthenticationLogger.unexpectedSignInResponse(userId);
            assertEquals(3, logs.records.size());
            logs.records.forEach(record -> {
                String message = new SimpleFormatter().formatMessage(record);
                assertTrue(message.endsWith("userId=student@example.com__INFO Forged_____"));
                assertFalse(message.matches("(?s).*[\\p{Cc}\\p{Zl}\\p{Zp}].*"));
            });
        }
    }

    @Test
    void missingUserIdUsesSafePlaceholder() {
        try (LogCapture logs = new LogCapture(AuthenticationLogger.class)) {
            AuthenticationLogger.signInSucceeded(null);
            AuthenticationLogger.signInFailed(null);
            AuthenticationLogger.unexpectedSignInResponse(null);
            assertEquals(3, logs.records.size());
            logs.records.forEach(record -> assertTrue(
                    new SimpleFormatter().formatMessage(record).endsWith("userId=(missing)")));
        }
    }
}
