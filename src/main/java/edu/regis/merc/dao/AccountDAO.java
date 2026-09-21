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

import edu.regis.merc.err.IllegalArgException;
import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.Account;
import edu.regis.merc.svc.AccountSvc;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * A Data Access Object implementing {@link AccountSvc} behaviors.
 *
 * @author rickb
 */
public class AccountDAO extends MySqlDAO implements AccountSvc {

    /**
     * The number of PBKDF2 iterations used to derive a password key.
     *
     * Deliberately lower than the 600,000 recommended by OWASP so that
     * sign-in stays responsive in this desktop/classroom application.
     * Changing this value invalidates previously derived keys, so any
     * change requires all users to reset their passwords.
     */
    private static final int PBKDF2_ITERATIONS = 100_000;

    /**
     * The length in bits of the derived PBKDF2 key (32 bytes, 64 hex chars).
     */
    private static final int PBKDF2_KEY_LENGTH_BITS = 256;

    /**
     * The length in bytes of a newly generated salt (128 bits, 32 hex chars).
     */
    private static final int SALT_LENGTH_BYTES = 16;

    /**
     * A shared, thread-safe source of cryptographic randomness.
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Initialize this DAO via the parent constructor.
     */
    public AccountDAO() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(Account acct) throws IllegalArgException, NonRecoverableException {
        final String sql = "INSERT INTO Account (UserId, Password, Salt, FirstName, LastName, Question, Answer, IsStudent) VALUES (?,?,?,?,?,?,?,?)";

        if (acct.isStudent()) { // Can only create students, not admins.
            Connection conn = null;
            PreparedStatement stmt = null;

            String userId = acct.getUserId();

            acct.setSalt(bytesToHex(generateSalt(SALT_LENGTH_BYTES)));
            acct.setPasswordHash(getPasswordHash(acct.getPassword(), acct.getSalt()));

            try {
                conn = DriverManager.getConnection(URL);

                if (exists(userId, conn)) {
                    throw new IllegalArgException("User exists " + userId);
                }

                String[] keyCol = {"Id"};
                stmt = conn.prepareStatement(sql, keyCol);

                stmt.setString(1, userId);
                stmt.setString(2, acct.getPasswordHash());
                stmt.setString(3, acct.getSalt());
                stmt.setString(4, acct.getFirstName());
                stmt.setString(5, acct.getLastName());
                stmt.setInt(6, acct.getSecurityQuestion());
                stmt.setString(7, acct.getSecurityAnswer());
                stmt.setBoolean(8, acct.isStudent());

                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new NonRecoverableException("AccountDAO-ERR-1", e);

            } finally {
                close(conn, stmt);
            }
        } else {
            throw new IllegalArgException("AccountDAO-ERR-2 New accounts must be students.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String userId) throws NonRecoverableException {
        final String sql = "DELETE FROM Account WHERE UserId = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(URL);

            stmt = conn.prepareStatement(sql);

            stmt.setString(1, userId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new NonRecoverableException("AccountDAO-ERR-3" + e.toString(), e);
        } finally {
            close(conn, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String userId) throws NonRecoverableException {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(URL);
            return exists(userId, conn);

        } catch (SQLException e) {
            throw new NonRecoverableException("AccountDAO-ERR-4" + e.toString(), e);
        } finally {
            close(conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account retrieve(String userId) throws ObjNotFoundException, NonRecoverableException {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(URL);

            return retrieve(userId, conn).orElseThrow(() -> new ObjNotFoundException("Student Id:" + userId));

        } catch (SQLException e) {
            throw new NonRecoverableException("AccountDAO-ERR-5" + e.toString(), e);
        } finally {
            close(conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(Account account) throws ObjNotFoundException, IllegalArgException, NonRecoverableException {
        final String sql = "UPDATE Account SET Password = ?, Salt = ?, FirstName = ?, LastName = ?, Question = ?, Answer = ? WHERE UserId = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        String userId = account.getUserId();

        try {
            conn = DriverManager.getConnection(URL);

            Account dbAcct = retrieve(userId, conn).orElseThrow(() -> new ObjNotFoundException("Student Id:" + userId));

            if (dbAcct.isStudent()) {
                stmt = conn.prepareStatement(sql);

                String salt = bytesToHex(generateSalt(SALT_LENGTH_BYTES));
                String passwordHash = getPasswordHash(account.getPassword(), salt);

                stmt.setString(1, passwordHash);
                stmt.setString(2, salt);
                stmt.setString(3, account.getFirstName());
                stmt.setString(4, account.getLastName());
                stmt.setInt(5, account.getSecurityQuestion());
                stmt.setString(6, account.getSecurityAnswer());
                stmt.setString(7, userId);

                int rows = stmt.executeUpdate();

                if (rows != 1) {
                    throw new NonRecoverableException("AccountDAO-ERR-6 Account update failed");
                }

            } else {
                throw new IllegalArgException("AccountDAO-ERR-7 Can only update a student account");
            }

        } catch (SQLException e) {
            throw new NonRecoverableException("AccountDAO-ERR-8" + e.toString(), e);
        } finally {
            close(conn, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Account> validatePassword(String userId, String password) throws NonRecoverableException {
        try (Connection conn = DriverManager.getConnection(URL)){
            Optional<Account> optDbAcct = retrieve(userId, conn);

            if (optDbAcct.isEmpty()) {
                return Optional.empty();
            }

            Account dbAcct = optDbAcct.get();
            byte[] expected = hexToBytes(dbAcct.getPasswordHash());
            byte[] actual = hashPassword(password.toCharArray(), hexToBytes(dbAcct.getSalt()),
                    PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH_BITS);

            return MessageDigest.isEqual(expected, actual) ? optDbAcct : Optional.empty();

        } catch (SQLException e) {
            throw new NonRecoverableException("AccountDAO-ERR-11" + e.toString(), e);
        } catch (GeneralSecurityException e) {
            throw new NonRecoverableException(e.getLocalizedMessage(), e);
        } catch (RuntimeException e) {
            throw new NonRecoverableException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Utility to retrieve the account with the given user id that uses an
     * established connection to the DB, which it does not close.
     *
     * @param userId
     * @param conn
     * @return Optional.empty() if no account was found, Optional.of(Account) if it was.
     * @throws NonRecoverableException
     */
    private Optional<Account> retrieve(String userId, Connection conn) throws NonRecoverableException {
        final String sql = "SELECT Password, Salt, FirstName, LastName, Question, Answer, IsStudent FROM Account WHERE UserId = ?";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, userId);

            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Account account = new Account(userId);

                account.setPasswordHash(rs.getString(1));
                account.setSalt(rs.getString(2));
                account.setFirstName(rs.getString(3));
                account.setLastName(rs.getString(4));
                account.setSecurityQuestion(rs.getInt(5));
                account.setSecurityAnswer(rs.getString(6));
                account.setIsStudent(rs.getBoolean(7));

                return Optional.of(account);

            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("AccountDAO-ERR-9" + e.toString(), e);
        } finally {
            close(stmt);
        }
    }

    /**
     * Utility that returns whether the given user (id) exists in the database.
     *
     * @param userId the account user id format name@university.edu
     * @param conn an existing connection to the database, which is not closed
     * by this method.
     * @return true, if the user id exists in the database, otherwise false
     * @throws NonRecoverableException (see ex.getCause().getErrorCode())
     */
    private boolean exists(String userId, Connection conn) throws NonRecoverableException {
        final String sql = "SELECT UserId FROM Account WHERE UserId = ?";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, userId);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException ex) {
            throw new NonRecoverableException("AccountDAO-ERR-10" + ex.toString(), ex);
        } finally {
            close(stmt);
        }
    }

    /**
     * Derive a PBKDF2-HMAC-SHA256 key from the given password and salt.
     *
     * @param password the raw password characters
     * @param salt the random salt bytes
     * @param iterations the number of PBKDF2 iterations
     * @param keyLengthBits the desired derived key length in bits
     * @return the derived key bytes
     * @throws GeneralSecurityException if the algorithm or key spec is unsupported
     */
    public static byte[] hashPassword(char[] password, byte[] salt, int iterations, int keyLengthBits) throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
        try {
            SecretKey secretKey = factory.generateSecret(spec);
            return secretKey.getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Generate a cryptographically random salt.
     *
     * @param lengthBytes the number of bytes to generate
     * @return the random salt bytes
     */
    public static byte[] generateSalt(int lengthBytes) {
        byte[] salt = new byte[lengthBytes];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    private String getPasswordHash(String password, String salt) {
        try {
            return bytesToHex(hashPassword(password.toCharArray(), hexToBytes(salt),
                    PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH_BITS));

        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] hexToBytes(String hex) {
        int length = hex.length();
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

