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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.util.Optional;

import edu.regis.merc.BaseMysqlIT;
import edu.regis.merc.err.IllegalArgException;
import edu.regis.merc.model.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for AccountDAO. Runs against a real MySQL container.
 */
@Tag("integration")
@DisplayName("AccountDAO Integration Tests")
public class AccountDAOTest extends BaseMysqlIT {

    private AccountDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new AccountDAO();
    }

    @AfterEach
    public void tearDown() {
        cleanupTestAccounts();
    }

    private void cleanupTestAccounts() {
        String sql = "DELETE FROM Account WHERE UserId LIKE ?";
        try (Connection conn = DriverManager.getConnection(MySqlDAO.URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "test_%");
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Account createTestAccount(String userId) {
        Account account = new Account();
        account.setUserId(userId);
        account.setPassword("TestP@ss123");
        account.setFirstName("Test");
        account.setLastName("User");
        account.setSecurityQuestion(0);
        account.setSecurityAnswer("testanswer");
        return account;
    }

    @Test
    @DisplayName("Create a new student account")
    public void testCreateAccount() throws Exception {
        Account account = createTestAccount("test_create@university.edu");

        dao.create(account);

        assertTrue(dao.exists("test_create@university.edu"));
    }

    @Test
    @DisplayName("Retrieve account after creation")
    public void testRetrieveAccount() throws Exception {
        Account account = createTestAccount("test_retrieve@university.edu");
        dao.create(account);

        Account retrieved = dao.retrieve("test_retrieve@university.edu");

        assertNotNull(retrieved);
        assertEquals("test_retrieve@university.edu", retrieved.getUserId());
        assertEquals("Test", retrieved.getFirstName());
        assertEquals("User", retrieved.getLastName());
        assertNotNull(retrieved.getSalt());
        assertNotNull(retrieved.getPasswordHash());
    }

    @Test
    @DisplayName("Reject duplicate user ID on creation")
    public void testCreateDuplicateAccount() throws Exception {
        Account account1 = createTestAccount("test_duplicate@university.edu");
        dao.create(account1);

        Account account2 = createTestAccount("test_duplicate@university.edu");
        try {
            dao.create(account2);
            fail("Expected IllegalArgException for duplicate user ID");
        } catch (IllegalArgException e) {
            assertTrue(e.getMessage().contains("User exists"));
        }
    }

    @Test
    @DisplayName("Reset password creates new salt and hash")
    public void testPasswordReset() throws Exception {
        Account account = createTestAccount("test_reset@university.edu");
        dao.create(account);

        Account retrieved = dao.retrieve("test_reset@university.edu");
        String oldSalt = retrieved.getSalt();
        String oldHash = retrieved.getPasswordHash();

        Account updateAccount = new Account("test_reset@university.edu");
        updateAccount.setPassword("NewP@ss456");
        updateAccount.setFirstName("Test");
        updateAccount.setLastName("User");
        updateAccount.setSecurityQuestion(0);
        updateAccount.setSecurityAnswer("testanswer");

        dao.update(updateAccount);

        Account updated = dao.retrieve("test_reset@university.edu");
        assertFalse(oldSalt.equals(updated.getSalt()));
        assertFalse(oldHash.equals(updated.getPasswordHash()));
    }

    @Test
    @DisplayName("Successful password validation")
    public void testValidatePasswordSuccess() throws Exception {
        Account account = createTestAccount("test_login@university.edu");
        dao.create(account);

        Optional<Account> result = dao.validatePassword("test_login@university.edu", "TestP@ss123");

        assertTrue(result.isPresent());
        assertEquals("test_login@university.edu", result.get().getUserId());
    }

    @Test
    @DisplayName("Failed password validation for non-existent user")
    public void testValidatePasswordNonExistentUser() throws Exception {
        Optional<Account> result = dao.validatePassword("nonexistent@university.edu", "any password");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Failed password validation for wrong password")
    public void testValidatePasswordWrongPassword() throws Exception {
        Account account = createTestAccount("test_wrongpass@university.edu");
        dao.create(account);

        Optional<Account> result = dao.validatePassword("test_wrongpass@university.edu", "wrongpassword");

        assertFalse(result.isPresent());
    }
}
