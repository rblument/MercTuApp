package edu.regis.merc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class Config {
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
            }
        }
    }

    private static final int MAX_PORT = 65535;

    /**
     * Return the port stored under the given key.
     *
     * @param key the property key
     * @param defaultValue the port to use when the key is missing or blank
     * @return a port number from 1 to 65535
     * @throws IllegalArgumentException if the value is not a valid port number
     */
    protected int getPort(String key, int defaultValue) {
        String value = props.getProperty(key);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            // Properties keeps trailing spaces, so "53640 " must still parse.
            int port = Integer.parseInt(value.trim());

            if (port >= 1 && port <= MAX_PORT) {
                return port;
            }
        } catch (NumberFormatException e) {
            // Reported below, along with out-of-range values.
        }

        throw new IllegalArgumentException("Invalid " + key + " '" + value
                + "': expected a port number from 1 to " + MAX_PORT);
    }

    /**
     * Return the host name stored under the given key.
     *
     * @param key the property key
     * @param defaultValue the host to use when the key is missing or blank
     * @return the host name, without surrounding whitespace
     */
    protected String getHost(String key, String defaultValue) {
        String value = props.getProperty(key);

        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }
}
