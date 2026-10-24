package edu.regis.merc.svc;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SvcFacadeTimeoutTest {
    private static final String CONNECT = "edu.regis.merc.client.connectTimeoutMillis";
    private static final String READ = "edu.regis.merc.client.readTimeoutMillis";

    private static final class RecordingSocket extends Socket {
        private int connectTimeout;
        private int readTimeout;
        private boolean failConnect;
        private boolean closed;

        @Override public void connect(SocketAddress address, int timeout) throws IOException {
            connectTimeout = timeout;
            if (failConnect) throw new SocketTimeoutException("Test connection timeout");
        }
        @Override public void setSoTimeout(int timeout) { readTimeout = timeout; }
        @Override public InputStream getInputStream() {
            return new ByteArrayInputStream("{\"status\":\"LoggedOut\"}\n".getBytes(StandardCharsets.UTF_8));
        }
        @Override public OutputStream getOutputStream() { return new ByteArrayOutputStream(); }
        @Override public void close() { closed = true; }
    }

    private SvcFacade facade(RecordingSocket socket, Properties properties) {
        return new SvcFacade("localhost", 12345, properties) {
            @Override Socket newSocket() { return socket; }
        };
    }

    @Test void configuredTimeoutsReachSocketAndConnectionIsClosed() {
        Properties properties = new Properties();
        properties.setProperty(CONNECT, "123");
        properties.setProperty(READ, "456");
        RecordingSocket socket = new RecordingSocket();
        TutorReply reply = facade(socket, properties).tutorRequest(new ClientRequest(ServerRequestType.LOGOUT));
        assertEquals("LoggedOut", reply.getStatus());
        assertEquals(123, socket.connectTimeout);
        assertEquals(456, socket.readTimeout);
        assertTrue(socket.closed);
    }

    @Test void invalidOrMissingTimeoutsCannotDisableBounds() {
        for (String value : new String[] {"0", "-1", "invalid", "999999999999999"}) {
            Properties properties = new Properties();
            properties.setProperty(CONNECT, value);
            properties.setProperty(READ, value);
            RecordingSocket socket = new RecordingSocket();
            facade(socket, properties).tutorRequest(new ClientRequest(ServerRequestType.LOGOUT));
            assertEquals(5000, socket.connectTimeout);
            assertEquals(10000, socket.readTimeout);
        }
        RecordingSocket socket = new RecordingSocket();
        facade(socket, new Properties()).tutorRequest(new ClientRequest(ServerRequestType.LOGOUT));
        assertEquals(5000, socket.connectTimeout);
        assertEquals(10000, socket.readTimeout);
    }

    @Test void connectionTimeoutReturnsFailureAndReleasesSocket() {
        RecordingSocket socket = new RecordingSocket();
        socket.failConnect = true;
        TutorReply reply = facade(socket, new Properties()).tutorRequest(new ClientRequest(ServerRequestType.LOGOUT));
        assertEquals(":ERR", reply.getStatus());
        assertTrue(socket.closed);
        assertTrue(socket.connectTimeout > 0);
    }

    @Test void stalledServerCannotHoldReadIndefinitely() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        try (ServerSocket listener = new ServerSocket(0)) {
            Thread peer = new Thread(() -> {
                try (Socket socket = listener.accept()) {
                    new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
                    release.await(4, TimeUnit.SECONDS); // Deliberately never send a reply.
                } catch (IOException | InterruptedException ignored) { }
            }, "stalled-server-test");
            peer.setDaemon(true);
            peer.start();
            try {
                Properties properties = new Properties();
                properties.setProperty(CONNECT, "500");
                properties.setProperty(READ, "100");
                long start = System.nanoTime();
                TutorReply reply = new SvcFacade("localhost", listener.getLocalPort(), properties)
                        .tutorRequest(new ClientRequest(ServerRequestType.LOGOUT));
                assertEquals(":ERR", reply.getStatus());
                assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) < 2000,
                        "Client did not respect its read timeout");
            } finally {
                release.countDown();
                listener.close();
                peer.join(1000);
            }
            assertFalse(peer.isAlive());
        }
    }
}
