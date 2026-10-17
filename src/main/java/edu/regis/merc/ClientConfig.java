package edu.regis.merc;

import java.io.IOException;

public class ClientConfig extends Config {
    public static final String DEFAULT_RESOURCE = "/client.properties";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 53640;

    public ClientConfig() throws IOException {
        this(null);
    }

    public ClientConfig(String externalFile) throws IOException {
        load(DEFAULT_RESOURCE, externalFile);
    }

    public String getHost() {
        return getHost("server.host", DEFAULT_HOST);
    }

    public int getPort() {
        return getPort("server.port", DEFAULT_PORT);
    }
}
