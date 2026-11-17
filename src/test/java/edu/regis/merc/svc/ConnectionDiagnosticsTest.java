package edu.regis.merc.svc;

import edu.regis.merc.ClientConfig;
import edu.regis.merc.ServerConfig;
import edu.regis.merc.test.LogCapture;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import static org.junit.jupiter.api.Assertions.*;

@ResourceLock("console-and-loggers")
class ConnectionDiagnosticsTest {
    @TempDir Path directory;

    @Test void diagnosticsKeepTypesStacksAndCausesWithoutSensitiveMessages() {
        IOException cause = new IOException("SECRET_STORE_PASSWORD");
        RuntimeException original = new IllegalArgumentException("SECRET_REQUEST_TOKEN", cause);
        original.addSuppressed(new IOException("SECRET_CONFIGURATION"));
        Throwable safe = ConnectionDiagnostics.safeCause(original);
        assertEquals(original.getClass().getName(), safe.getMessage());
        assertArrayEquals(original.getStackTrace(), safe.getStackTrace());
        assertEquals(IOException.class.getName(), safe.getCause().getMessage());
        assertArrayEquals(cause.getStackTrace(), safe.getCause().getStackTrace());
        java.io.StringWriter text = new java.io.StringWriter();
        safe.printStackTrace(new java.io.PrintWriter(text));
        assertFalse(text.toString().contains("SECRET_"));
        assertEquals(1, safe.getSuppressed().length);
    }

    @Test void eofReturnsConnectionErrorAndLogsCauseWithoutRequest() throws Exception {
        try (ServerSocket listener = new ServerSocket(0)) {
            Thread peer = new Thread(() -> {
                try (Socket socket = listener.accept()) {
                    new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
                } catch (IOException ex) {
                    throw new java.io.UncheckedIOException(ex);
                }
            });
            peer.setDaemon(true);
            peer.start();
            Path config = directory.resolve("client.properties");
            Files.writeString(config, "server.host=localhost\nserver.port=" + listener.getLocalPort());
            try (LogCapture logs = new LogCapture(SvcFacade.class)) {
                SvcFacade facade = new SvcFacade(TransportSockets.create(new ClientConfig(config.toString())));
                ClientRequest request = new ClientRequest(ServerRequestType.SIGN_IN);
                request.setData("SECRET_REQUEST_TOKEN");
                assertEquals(":ERR", facade.tutorRequest(request).getStatus());
                assertTrue(logs.output().contains(IOException.class.getName()));
                assertFalse(logs.output().contains("SECRET_REQUEST_TOKEN"));
                assertNotNull(logs.records.get(0).getThrown());
            } finally {
                listener.close();
                peer.join(2000);
            }
            assertFalse(peer.isAlive());
        }
    }

    @Test void bindFailureLogsOriginalException() throws Exception {
        try (ServerSocket occupied = new ServerSocket(0);
                LogCapture logs = new LogCapture(MercServer.class)) {
            Path config = directory.resolve("server.properties");
            Files.writeString(config, "server.port=" + occupied.getLocalPort());
            MercServer server = new MercServer(new ServerConfig(config.toString()));
            server.run();
            assertFalse(server.awaitListening(1, TimeUnit.SECONDS));
            assertTrue(logs.records.stream().anyMatch(record -> record.getThrown() instanceof java.net.BindException));
        }
    }

    @Test void malformedRequestLogsRuntimeStackWithoutPayloadAndListenerSurvives() throws Exception {
        MercServer server = new MercServer(new ServerConfig());
        int port = server.startListening(0);
        Thread listener = new Thread(server, "diagnostics-server-test");
        listener.setDaemon(true);
        try (LogCapture logs = new LogCapture(MercServer.class)) {
            listener.start();
            try (Socket socket = new Socket("localhost", port)) {
                socket.setSoTimeout(2000);
                java.io.PrintWriter writer = new java.io.PrintWriter(socket.getOutputStream(), true);
                writer.println("{\"requestType\":{\"securityToken\":\"SECRET_REQUEST_TOKEN\"}}");
                assertEquals(-1, socket.getInputStream().read());
            }
            Path config = directory.resolve("retry.properties");
            Files.writeString(config, "server.host=localhost\nserver.port=" + port);
            SvcFacade facade = new SvcFacade(TransportSockets.create(new ClientConfig(config.toString())));
            ClientRequest request = new ClientRequest(ServerRequestType.CREATE_ACCOUNT);
            request.setData("{\"userId\":\"local-test@example.com\",\"password\":\"\"}");
            assertEquals("A password is required to create an account.", facade.tutorRequest(request).getData());
            String output = logs.output();
            assertTrue(output.contains("JsonSyntaxException"));
            assertTrue(output.contains("MercTuConnection.run"));
            assertFalse(output.contains("SECRET_REQUEST_TOKEN"));
        } finally {
            server.close();
            listener.join(2000);
        }
        assertFalse(listener.isAlive());
    }
}
