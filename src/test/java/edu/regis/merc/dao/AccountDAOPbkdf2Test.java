/*
 * MERC^T: Multiple External Representations of Computation Tutor
 *
 *  (C) Richard Blumenthal, All rights reserved
 *
 *  Unauthorized use, duplication or distribution without the authors'
 *  permission is strictly prohibited.
 *
 *  Unless required by applicable law or agreed to in writing, this
 *  software is distributed on an "AS IS" basis without warranties
 *  or conditions of any kind, either expressed or implied.
 */
package edu.regis.merc.dao;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.junit.jupiter.api.Test;

/**
 * Tests for the PBKDF2 password hashing utilities in {@link AccountDAO}.
 */
public class AccountDAOPbkdf2Test {

    private static final int PRODUCTION_ITERATIONS = 100_000;
    private static final int KEY_LENGTH_BITS = 256;

    @Test
    void hashPasswordMatchesKnownAnswerVector() throws Exception {
        byte[] dk = AccountDAO.hashPassword("password".toCharArray(),
                "salt".getBytes(StandardCharsets.UTF_8), 1, KEY_LENGTH_BITS);

        assertEquals("120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b", toHex(dk));
    }

    @Test
    void hashPasswordIsDeterministic() throws Exception {
        byte[] salt = AccountDAO.generateSalt(16);
        char[] password = "S3cret!".toCharArray();

        byte[] first = AccountDAO.hashPassword(password, salt, PRODUCTION_ITERATIONS, KEY_LENGTH_BITS);
        byte[] second = AccountDAO.hashPassword(password, salt, PRODUCTION_ITERATIONS, KEY_LENGTH_BITS);

        assertArrayEquals(first, second);
    }

    @Test
    void hashPasswordDependsOnSalt() throws Exception {
        char[] password = "S3cret!".toCharArray();

        byte[] first = AccountDAO.hashPassword(password, AccountDAO.generateSalt(16), PRODUCTION_ITERATIONS, KEY_LENGTH_BITS);
        byte[] second = AccountDAO.hashPassword(password, AccountDAO.generateSalt(16), PRODUCTION_ITERATIONS, KEY_LENGTH_BITS);

        assertFalse(MessageDigest.isEqual(first, second));
    }

    @Test
    void hashPasswordProducesRequestedKeyLength() throws Exception {
        byte[] dk = AccountDAO.hashPassword("x".toCharArray(), new byte[16], 1, KEY_LENGTH_BITS);

        assertEquals(KEY_LENGTH_BITS / 8, dk.length);
    }

    @Test
    void generateSaltReturnsRequestedLengthAndVaries() {
        byte[] first = AccountDAO.generateSalt(16);
        byte[] second = AccountDAO.generateSalt(16);

        assertEquals(16, first.length);
        assertEquals(16, second.length);
        assertFalse(MessageDigest.isEqual(first, second));
    }

    @Test
    void hexToBytesRoundTrips() {
        byte[] salt = AccountDAO.generateSalt(16);

        assertArrayEquals(salt, AccountDAO.hexToBytes(toHex(salt)));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            hex.append(Character.forDigit((b >> 4) & 0xf, 16));
            hex.append(Character.forDigit(b & 0xf, 16));
        }
        return hex.toString();
    }
}
