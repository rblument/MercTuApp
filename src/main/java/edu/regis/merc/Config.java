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

    protected int getPort(String key, int defaultValue) {
        String value = props.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    protected String getHost(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
