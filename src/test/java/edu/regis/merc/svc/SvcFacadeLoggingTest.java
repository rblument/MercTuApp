package edu.regis.merc.svc;

import com.google.gson.Gson;
import edu.regis.merc.test.LogCapture;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import static org.junit.jupiter.api.Assertions.*;

@ResourceLock("console-and-loggers")
class SvcFacadeLoggingTest {
    private static final String PAYLOAD = """
            {"userId":"student@example.com","password":"TEST_PASSWORD_DO_NOT_LOG",
             "passwordHash":"TEST_HASH_DO_NOT_LOG","securityAnswer":"TEST_ANSWER_DO_NOT_LOG",
             "securityToken":"TEST_TOKEN_DO_NOT_LOG"}
            """;

    @Test
    void debugLoggingNeverDumpsCredentialsOrPayloads() {
        verifyRequests(true);
    }

    @Test
    void normalLoggingNeverDumpsCredentialsOrPayloads() {
        verifyRequests(false);
    }

    private void verifyRequests(boolean debug) {
        Gson gson = new Gson();
        for (ServerRequestType type : new ServerRequestType[] {
                ServerRequestType.SIGN_IN, ServerRequestType.VERIFY_USER,
                ServerRequestType.RESET_PASSWORD }) {
            AtomicReference<String> transmitted = new AtomicReference<>();
            String response = gson.toJson(new TutorReply("Authenticated", PAYLOAD));
            SvcFacade facade = new SvcFacade(json -> {
                transmitted.set(json);
                return response;
            }, debug);
            ClientRequest request = new ClientRequest(type);
            request.setUserId("student@example.com");
            request.setSecurityToken("TEST_TOKEN_DO_NOT_LOG");
            request.setData(PAYLOAD);

            try (LogCapture logs = new LogCapture(SvcFacade.class)) {
                TutorReply reply = facade.tutorRequest(request);
                assertEquals("Authenticated", reply.getStatus());
                assertEquals(PAYLOAD, reply.getData());
                ClientRequest sent = gson.fromJson(transmitted.get(), ClientRequest.class);
                assertEquals(type, sent.getRequestType());
                assertEquals(PAYLOAD, sent.getData());
                assertEquals("TEST_TOKEN_DO_NOT_LOG", sent.getSecurityToken());

                String output = logs.output();
                for (String secret : new String[] { "TEST_PASSWORD_DO_NOT_LOG", "TEST_HASH_DO_NOT_LOG",
                        "TEST_ANSWER_DO_NOT_LOG", "TEST_TOKEN_DO_NOT_LOG" }) {
                    assertFalse(output.contains(secret), "Credential marker appeared in logs");
                }
                assertFalse(output.contains(PAYLOAD));
                assertFalse(output.contains(transmitted.get()));
                assertFalse(output.contains(response));
                assertEquals(debug ? 2 : 0, logs.records.size());
                if (debug) {
                    assertTrue(output.contains("Sending tutor request"));
                    assertTrue(output.contains("Tutor reply received"));
                } else {
                    assertEquals("", output);
                }
            }
        }
    }
}
