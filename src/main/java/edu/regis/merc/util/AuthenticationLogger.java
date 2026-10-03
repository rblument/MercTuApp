package edu.regis.merc.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Logs authentication outcomes without accepting accounts or credential payloads. */
public final class AuthenticationLogger {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationLogger.class.getName());

    private AuthenticationLogger() {}

    public static void signInSucceeded(String userId) {
        LOGGER.log(Level.INFO, "Sign-in successful; userId={0}", sanitize(userId));
    }

    public static void signInFailed(String userId) {
        LOGGER.log(Level.WARNING, "Sign-in failed: invalid credentials; userId={0}", sanitize(userId));
    }

    public static void unexpectedSignInResponse(String userId) {
        LOGGER.log(Level.WARNING, "Unexpected sign-in response; userId={0}", sanitize(userId));
    }

    private static String sanitize(String value) {
        return value == null ? "(missing)" : value.replaceAll("[\\p{Cc}\\p{Zl}\\p{Zp}]", "_");
    }
}
