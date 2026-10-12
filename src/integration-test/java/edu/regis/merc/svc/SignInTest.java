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
package edu.regis.merc.svc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import edu.regis.merc.BaseMysqlIT;
import edu.regis.merc.ServerConfig;
import edu.regis.merc.model.Account;
import edu.regis.merc.model.TutoringSession;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration test for sign-in functionality. Tests the complete flow from
 * client request to server response using a real MySQL database and a running
 * server instance.
 */
@Tag("integration")
@DisplayName("Sign-In Integration Test")
public class SignInTest extends BaseMysqlIT {

    private MercServer server;
    private Thread serverThread;

    @AfterEach
    public void tearDown() {
        cleanupTestAccounts();
        if (server != null) {
            server.close();
        }
        if (serverThread != null) {
            try {
                serverThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void cleanupTestAccounts() {
        // Delete child rows first so repeated runs on a reused container do
        // not accumulate orphans.
        String[] tables = { "TutoringSession", "Assessment", "StudentModel", "Account" };
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(edu.regis.merc.dao.MySqlDAO.URL)) {
            for (String table : tables) {
                try (java.sql.PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + table + " WHERE UserId LIKE ?")) {
                    stmt.setString(1, "test_signin_%");
                    stmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Account buildTestAccount(String userId) {
        Account account = new Account();
        account.setUserId(userId);
        account.setPassword("TestP@ss123");
        account.setFirstName("Test");
        account.setLastName("User");
        account.setSecurityQuestion(0);
        account.setSecurityAnswer("testanswer");
        return account;
    }

    private void startServer() throws IOException, InterruptedException {
        ServerConfig config = new ServerConfig();
        server = new MercServer(config);
        serverThread = new Thread(server, "test-server");
        serverThread.start();
        
        // Wait for server to be ready
        Thread.sleep(500);
    }

    @Test
    @DisplayName("Sign-in with valid credentials returns Authenticated status and session")
    public void testSignInSuccess() throws IOException, InterruptedException {
        // 1. Build test account
        String userId = "test_signin_" + System.currentTimeMillis() + "@university.edu";
        Account account = buildTestAccount(userId);

        // 2. Start server
        startServer();

        SvcFacade facade = SvcFacade.instance();
        Gson gson = new Gson();

        // 3. Create the account through the server, which also creates the
        // student model and tutoring session rows that sign-in requires.
        ClientRequest createRequest = new ClientRequest(ServerRequestType.CREATE_ACCOUNT);
        createRequest.setData(gson.toJson(account));

        TutorReply createReply = facade.tutorRequest(createRequest);
        assertEquals("Created", createReply.getStatus(),
                "Account creation should succeed but was: "
                        + createReply.getStatus() + " " + createReply.getData());

        // 4. Make sign-in request via SvcFacade
        ClientRequest request = new ClientRequest(ServerRequestType.SIGN_IN);
        request.setData(gson.toJson(account));

        TutorReply reply = facade.tutorRequest(request);

        System.out.println("Output: " + reply.toString());
        // 5. Assert reply status is "Authenticated"
        assertEquals("Authenticated", reply.getStatus(), "Reply status should be 'Authenticated'");

        // 5. Assert response contains valid session data
        assertNotNull(reply.getData(), "Reply data should not be null");

        // 6. Verify session can be parsed and contains valid student data
        TutoringSession session = gson.fromJson(reply.getData(), TutoringSession.class);
        assertNotNull(session, "Session should be parseable");
        assertNotNull(session.getStudent(), "Session should contain student");
        assertNotNull(session.getStudent().getAccount(), "Student should have account");
        assertEquals(userId, session.getStudent().getAccount().getUserId(), "Session student should match test user");

        // 7. Verify session has required fields populated
        assertNotNull(session.getSecurityToken(), "Session should have security token");
        assertNotNull(session.getStartDate(), "Session should have start date");
        assertNotNull(session.getTasks(), "Session should have tasks");
    }
}
