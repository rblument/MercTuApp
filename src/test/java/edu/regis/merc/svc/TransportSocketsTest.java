package edu.regis.merc.svc;

import edu.regis.merc.ClientConfig;
import edu.regis.merc.ServerConfig;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import javax.net.ssl.SSLSocket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class TransportSocketsTest {
    @TempDir Path directory;

    private String tlsConfig() throws IOException {
        Path file = directory.resolve("tls.properties");
        Files.writeString(file, "server.tls=true\n");
        return file.toString();
    }

    @Test void plaintextIgnoresUnusedTlsStores() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("javax.net.ssl.keyStore", "missing");
        TransportSockets sockets = TransportSockets.create(new ClientConfig(), properties);
        try (Socket socket = sockets.newClientSocket()) { assertFalse(socket instanceof SSLSocket); }
    }

    @Test void clientUsesPlatformTrustAndHostnameVerification() throws Exception {
        TransportSockets sockets = TransportSockets.create(new ClientConfig(tlsConfig()), new Properties());
        try (Socket socket = sockets.newClientSocket()) {
            assertEquals("HTTPS", assertInstanceOf(SSLSocket.class, socket)
                    .getSSLParameters().getEndpointIdentificationAlgorithm());
        }
        assertThrows(IOException.class, sockets::listen);
    }

    @Test void serverRequiresTlsIdentity() throws Exception {
        ServerConfig config = new ServerConfig(tlsConfig());
        IOException error = assertThrows(IOException.class, () -> TransportSockets.create(config, new Properties()));
        assertTrue(error.getMessage().contains("keyStore"));
    }

    @Test void missingStoreErrorsDoNotExposePathsOrPasswords() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("javax.net.ssl.trustStore", directory.resolve("sensitive-path").toString());
        properties.setProperty("javax.net.ssl.trustStorePassword", "sensitive-password");
        ClientConfig config = new ClientConfig(tlsConfig());
        IOException error = assertThrows(IOException.class, () -> TransportSockets.create(config, properties));
        assertFalse(error.getMessage().contains("sensitive"));
        assertNull(error.getCause());
    }

    @Test void plaintextServerAndReadTimeoutRemainSupported() throws Exception {
        TransportSockets sockets = TransportSockets.create(new ServerConfig(), new Properties());
        assertThrows(IOException.class, sockets::newClientSocket);
        try (java.net.ServerSocket listener = sockets.newServerSocket()) {
            assertFalse(listener instanceof javax.net.ssl.SSLServerSocket);
        }
        try (Socket socket = new Socket()) {
            TransportSockets.prepare(socket);
            assertEquals(TransportSockets.READ_TIMEOUT_MS, socket.getSoTimeout());
        }
    }
    @Test void emptyStoresAreRejected() throws Exception {
        Path path = directory.resolve("empty.p12");
        java.security.KeyStore store = java.security.KeyStore.getInstance("PKCS12");
        store.load(null, "test-password".toCharArray());
        try (java.io.OutputStream output = Files.newOutputStream(path)) {
            store.store(output, "test-password".toCharArray());
        }
        for (String name : new String[] {"keyStore", "trustStore"}) {
            Properties properties = new Properties();
            properties.setProperty("javax.net.ssl." + name, path.toString());
            properties.setProperty("javax.net.ssl." + name + "Password", "test-password");
            properties.setProperty("javax.net.ssl." + name + "Type", "PKCS12");
            edu.regis.merc.Config config = name.equals("keyStore")
                    ? new ServerConfig(tlsConfig()) : new ClientConfig(tlsConfig());
            assertThrows(IOException.class, () -> TransportSockets.create(config, properties));
        }
    }

}
