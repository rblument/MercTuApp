package edu.regis.merc;

import java.io.IOException;

public class ServerConfig extends Config {
    public static final String DEFAULT_RESOURCE = "/server.properties";
    public static final int DEFAULT_PORT = 53640;

    public ServerConfig() throws IOException {
        this(null);
    }

    public ServerConfig(String externalFile) throws IOException {
        load(DEFAULT_RESOURCE, externalFile);
    }

    public int getPort() {
        return getPort("server.port", DEFAULT_PORT);
    }
}
