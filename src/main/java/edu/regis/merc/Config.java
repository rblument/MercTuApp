package edu.regis.merc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class Config {
    private static final int MAX_PORT = 65535;
    protected final Properties props = new Properties();

    protected void load(String defaultResource, String externalFile) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(defaultResource)) {
            if (is != null) {
                props.load(is);
            }
        }

        if (externalFile != null) {
            try (InputStream is = new java.io.FileInputStream(externalFile)) {
                props.load(is);
            } catch (IOException ex) {
                throw new IOException("Cannot load external configuration; check that the file exists and is readable.");
            }
        }
        for (String key : props.stringPropertyNames()) {
            if (!key.equals("server.host") && !key.equals("server.port") && !key.equals("server.tls")) {
                throw new IllegalArgumentException("Unknown transport configuration key. Use server.host, server.port, or server.tls; "
                        + "pass JSSE store settings as JVM properties.");
            }
        }
    }

    /** TLS is opt-in; key and trust stores use standard JSSE JVM properties. */
    public boolean isTls() {
        String value = props.getProperty("server.tls", "false").trim();
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("server.tls must be true or false.");
        }
        return Boolean.parseBoolean(value);
    }

    public abstract int getPort();

    protected int getPort(String key, int defaultValue) {
        String value = props.getProperty(key);
        try {
            int port = value != null ? Integer.parseInt(value.trim()) : defaultValue;
            if (port < 1 || port > MAX_PORT) throw new NumberFormatException();
            return port;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("server.port must be between 1 and 65535.");
        }
    }

    protected String getHost(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
