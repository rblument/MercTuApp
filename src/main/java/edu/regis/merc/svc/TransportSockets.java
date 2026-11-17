package edu.regis.merc.svc;

import edu.regis.merc.Config;
import edu.regis.merc.ClientConfig;
import edu.regis.merc.ServerConfig;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/** Validates JSSE stores at startup and owns the selected socket factories. */
public final class TransportSockets {
    public static final int CONNECT_TIMEOUT_MS = 10000;
    public static final int HANDSHAKE_TIMEOUT_MS = 10000;
    public static final int READ_TIMEOUT_MS = 15000;
    private final String host;
    private final int port;
    private final SocketFactory clients;
    private final ServerSocketFactory servers;

    private TransportSockets(Config config, SocketFactory clients, ServerSocketFactory servers) {
        this.host = config instanceof ClientConfig client ? client.getHost() : "localhost";
        this.port = config.getPort();
        this.clients = clients;
        this.servers = servers;
    }

    public static TransportSockets create(Config config) throws IOException {
        return create(config, System.getProperties());
    }

    static TransportSockets create(Config config, Properties properties) throws IOException {
        if (!config.isTls()) {
            return new TransportSockets(config, config instanceof ClientConfig ? SocketFactory.getDefault() : null,
                    config instanceof ServerConfig ? ServerSocketFactory.getDefault() : null);
        }
        SocketFactory clients = null;
        ServerSocketFactory servers = null;
        if (config instanceof ServerConfig) {
            try {
                KeyStore store = loadStore(properties, "keyStore", true);
                char[] password = properties.getProperty("javax.net.ssl.keyStorePassword", "").toCharArray();
                try {
                    boolean identity = false;
                    Enumeration<String> aliases = store.aliases();
                    while (aliases.hasMoreElements()) {
                        String alias = aliases.nextElement();
                        if (store.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
                            java.security.cert.Certificate[] chain = store.getCertificateChain(alias);
                            if (chain == null || chain.length == 0) continue;
                            for (java.security.cert.Certificate cert : chain) {
                                ((X509Certificate) cert).checkValidity();
                            }
                            identity = true;
                        }
                    }
                    if (!identity) throw new GeneralSecurityException();
                    KeyManagerFactory keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    keys.init(store, password);
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(keys.getKeyManagers(), null, null);
                    servers = context.getServerSocketFactory();
                } finally {
                    Arrays.fill(password, '\0');
                }
            } catch (GeneralSecurityException | RuntimeException ex) {
                throw new IOException("Cannot initialize server TLS identity; check keyStore type, password, private key, and certificate validity.");
            }
        }
        if (config instanceof ClientConfig) {
            try {
                KeyStore trust = loadStore(properties, "trustStore", false);
                TrustManagerFactory trusts = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trusts.init(trust);
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trusts.getTrustManagers(), null);
                clients = context.getSocketFactory();
            } catch (GeneralSecurityException | RuntimeException ex) {
                throw new IOException("Cannot initialize client TLS trust; check trustStore type, password, and trusted certificates.");
            }
        }
        return new TransportSockets(config, clients, servers);
    }

    private static KeyStore loadStore(Properties properties, String name, boolean required) throws IOException {
        String prefix = "javax.net.ssl." + name;
        String file = properties.getProperty(prefix);
        if (file == null && !required) return null; // JSSE platform trust roots.
        if (file == null || file.isBlank()) throw new IOException("TLS requires " + prefix + " to name a readable store.");
        char[] password = properties.getProperty(prefix + "Password", "").toCharArray();
        try (InputStream input = Files.newInputStream(Path.of(file))) {
            KeyStore store = KeyStore.getInstance(properties.getProperty(prefix + "Type", KeyStore.getDefaultType()));
            store.load(input, password);
            if (store.size() == 0) throw new GeneralSecurityException();
            return store;
        } catch (GeneralSecurityException | IOException | RuntimeException ex) {
            throw new IOException("Cannot load " + prefix + "; check the store file, type, password, and entries.");
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    Socket newClientSocket() throws IOException {
        if (clients == null) throw new IOException("Client transport is disabled in server mode.");
        Socket socket = clients.createSocket();
        if (socket instanceof SSLSocket ssl) {
            SSLParameters parameters = ssl.getSSLParameters();
            parameters.setEndpointIdentificationAlgorithm("HTTPS");
            ssl.setSSLParameters(parameters);
        }
        return socket;
    }

    Socket connect() throws IOException {
        Socket socket = newClientSocket();
        try {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            prepare(socket);
            return socket;
        } catch (IOException | RuntimeException ex) {
            try { socket.close(); } catch (IOException ignored) { /* Preserve the connection failure. */ }
            throw ex;
        }
    }

    ServerSocket newServerSocket() throws IOException {
        if (servers == null) throw new IOException("Server transport is disabled in client mode.");
        return servers.createServerSocket();
    }

    ServerSocket listen() throws IOException {
        return listen(port);
    }

    // Allows local tests to request an ephemeral port without relaxing startup validation.
    ServerSocket listen(int port) throws IOException {
        ServerSocket listener = newServerSocket();
        try {
            listener.bind(new InetSocketAddress(port));
            return listener;
        } catch (IOException | RuntimeException ex) {
            try { listener.close(); } catch (IOException ignored) { /* Preserve the bind failure. */ }
            throw ex;
        }
    }

    static void prepare(Socket socket) throws IOException {
        socket.setSoTimeout(HANDSHAKE_TIMEOUT_MS);
        if (socket instanceof SSLSocket ssl) ssl.startHandshake();
        socket.setSoTimeout(READ_TIMEOUT_MS);
    }
}
