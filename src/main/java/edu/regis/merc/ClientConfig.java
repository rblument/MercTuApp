package edu.regis.merc;

import java.io.IOException;

public class ClientConfig extends Config {
    public static final String DEFAULT_RESOURCE = "/client.properties";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 53640;
    public static final int TLS_PORT = 53641;

    public ClientConfig() throws IOException {
        this(null);
    }

    public ClientConfig(String externalFile) throws IOException {
        load(DEFAULT_RESOURCE, externalFile);
        getPort();
        getHost();
    }

    public String getHost() {
        String host = getHost("server.host", DEFAULT_HOST).trim();
        if (host.isBlank() || host.chars().anyMatch(Character::isWhitespace) || host.contains("/") || host.contains("\\")) {
            throw new IllegalArgumentException("server.host must be a hostname or IP address.");
        }
        return host;
    }

    public int getPort() {
        return getPort("server.port", isTls() ? TLS_PORT : DEFAULT_PORT);
    }
}
