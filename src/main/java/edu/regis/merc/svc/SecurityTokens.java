package edu.regis.merc.svc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/** Small shared helpers for opaque session tokens; no account or progress state. */
final class SecurityTokens {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private SecurityTokens() { }

    static String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    static boolean matches(String stored, String presented) {
        return stored != null && !stored.isBlank() && presented != null && !presented.isBlank()
                && MessageDigest.isEqual(stored.getBytes(StandardCharsets.UTF_8), presented.getBytes(StandardCharsets.UTF_8));
    }
}
