package edu.regis.merc.svc;

import edu.regis.merc.Config;
import edu.regis.merc.ClientConfig;
import edu.regis.merc.ServerConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** Real JSSE sockets and the application JSON protocol, with no database or Docker. */
class LocalTlsTest {
    @TempDir static Path directory;
    private static final String PASSWORD = UUID.randomUUID().toString();

    @BeforeAll static void certificates() throws Exception {
        keytool("-genkeypair", "-alias", "server", "-keyalg", "RSA", "-keysize", "2048",
                "-validity", "2", "-dname", "CN=localhost", "-ext", "SAN=dns:localhost",
                "-keystore", directory.resolve("server.p12").toString(), "-storetype", "PKCS12",
                "-storepass:env", "MERCTU_TEST_STORE_PASSWORD");
        keytool("-exportcert", "-alias", "server", "-keystore", directory.resolve("server.p12").toString(),
                "-storepass:env", "MERCTU_TEST_STORE_PASSWORD", "-file", directory.resolve("server.cer").toString());
        keytool("-importcert", "-noprompt", "-alias", "server", "-file", directory.resolve("server.cer").toString(),
                "-keystore", directory.resolve("client.p12").toString(), "-storetype", "PKCS12",
                "-storepass:env", "MERCTU_TEST_STORE_PASSWORD");
    }

    private static void keytool(String... args) throws Exception {
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        Path executable = Path.of(System.getProperty("java.home"), "bin", windows ? "keytool.exe" : "keytool");
        List<String> command = new ArrayList<>();
        command.add(executable.toString());
        command.addAll(Arrays.asList(args));
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true)
                .redirectOutput(directory.resolve("keytool.log").toFile());
        builder.environment().put("MERCTU_TEST_STORE_PASSWORD", PASSWORD);
        Process process = builder.start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "keytool timed out");
            assertEquals(0, process.exitValue(), "keytool failed to prepare temporary TLS stores");
        } finally {
            if (process.isAlive()) process.destroyForcibly();
        }
    }

    private static Properties store(String name, String file) {
        Properties properties = new Properties();
        properties.setProperty("javax.net.ssl." + name, directory.resolve(file).toString());
        properties.setProperty("javax.net.ssl." + name + "Password", PASSWORD);
        properties.setProperty("javax.net.ssl." + name + "Type", "PKCS12");
        return properties;
    }

    private static Config config(boolean server, boolean tls, int port, String host) throws IOException {
        Path file = Files.createTempFile(directory, "transport-", ".properties");
        Files.writeString(file, "server.tls=" + tls + "\nserver.port=" + port + "\nserver.host=" + host + "\n");
        return server ? new ServerConfig(file.toString()) : new ClientConfig(file.toString());
    }

    private TransportSockets client(int port, String host, boolean trusted) throws IOException {
        return TransportSockets.create(config(false, true, port, host),
                trusted ? store("trustStore", "client.p12") : new Properties());
    }

    @Test void trustedCertificateHandshakesAndExchangesApplicationJson() throws Exception {
        try (RunningServer server = new RunningServer(true)) {
            TransportSockets client = client(server.port, "localhost", true);
            try (Socket socket = client.connect()) {
                SSLSocket ssl = assertInstanceOf(SSLSocket.class, socket);
                assertNotEquals("SSL_NULL_WITH_NULL_NULL", ssl.getSession().getCipherSuite());
                assertEquals(TransportSockets.READ_TIMEOUT_MS, ssl.getSoTimeout());
            }
            assertJsonReply(client);
        }
    }

    @Test void untrustedCertificateIsRejectedAndListenerStillAcceptsTrustedClient() throws Exception {
        try (RunningServer server = new RunningServer(true)) {
            assertThrows(SSLHandshakeException.class, () -> client(server.port, "localhost", false).connect());
            assertJsonReply(client(server.port, "localhost", true));
        }
    }

    @Test void hostnameMismatchIsRejectedEvenWhenCertificateIsTrusted() throws Exception {
        try (RunningServer server = new RunningServer(true)) {
            // The generated certificate has only DNS:localhost, deliberately no IP SAN.
            assertThrows(SSLHandshakeException.class, () -> client(server.port, "127.0.0.1", true).connect());
            assertJsonReply(client(server.port, "localhost", true));
        }
    }

    @Test void plaintextStillExchangesTheSameApplicationJson() throws Exception {
        try (RunningServer server = new RunningServer(false)) {
            TransportSockets client = TransportSockets.create(
                    config(false, false, server.port, "localhost"), new Properties());
            try (Socket socket = client.connect()) { assertFalse(socket instanceof SSLSocket); }
            SvcFacade facade = new SvcFacade(client);
            facade.configure((ClientConfig) config(false, false, server.port, "localhost"));
            assertJsonReply(facade);
        }
    }

    @Test void wrongStorePasswordsFailBeforeBindingOrConnecting() throws Exception {
        for (String role : new String[] {"server", "client"}) {
            Properties properties = role.equals("server") ? store("keyStore", "server.p12") : store("trustStore", "client.p12");
            String name = role.equals("server") ? "keyStore" : "trustStore";
            properties.setProperty("javax.net.ssl." + name + "Password", "wrong-password");
            IOException error = assertThrows(IOException.class,
                    () -> TransportSockets.create(config(role.equals("server"), true, 53641, "localhost"), properties));
            assertFalse(error.getMessage().contains(PASSWORD));
            assertFalse(error.getMessage().contains("wrong-password"));
            assertNull(error.getCause());
        }
    }

    @Test void shutdownClosesClientStalledBeforeTlsHandshake() throws Exception {
        try (RunningServer server = new RunningServer(true);
                Socket stalled = new Socket("localhost", server.port)) {
            stalled.setSoTimeout(3000);
            server.server.close();
            // Depending on whether accept raced with close, the peer sees EOF or reset.
            try { assertEquals(-1, stalled.getInputStream().read()); } catch (java.net.SocketException expected) { }
        }
    }

    private void assertJsonReply(TransportSockets sockets) {
        assertJsonReply(new SvcFacade(sockets));
    }

    private void assertJsonReply(SvcFacade facade) {
        // This existing validation path returns before accessing any DAO.
        ClientRequest request = new ClientRequest(ServerRequestType.CREATE_ACCOUNT);
        request.setData("{\"userId\":\"local-test@example.com\",\"password\":\"\"}");
        TutorReply reply = facade.tutorRequest(request);
        assertEquals(":ERR", reply.getStatus());
        assertEquals("A password is required to create an account.", reply.getData());
    }

    private static final class RunningServer implements AutoCloseable {
        private final MercServer server;
        private final Thread thread;
        private final int port;

        private RunningServer(boolean tls) throws Exception {
            TransportSockets sockets = TransportSockets.create(
                    config(true, tls, tls ? 53641 : 53640, "localhost"),
                    tls ? store("keyStore", "server.p12") : new Properties());
            try (java.net.ServerSocket probe = sockets.newServerSocket()) {
                assertEquals(tls, probe instanceof SSLServerSocket);
            }
            server = new MercServer(sockets);
            port = server.startListening(0);
            thread = new Thread(server, "local-transport-test");
            thread.setDaemon(true);
            thread.start();
        }

        @Override public void close() throws InterruptedException {
            server.close();
            thread.join(3000);
            assertFalse(thread.isAlive(), "Server did not stop after its sockets were closed");
        }
    }
}
