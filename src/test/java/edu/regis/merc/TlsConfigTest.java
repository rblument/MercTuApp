package edu.regis.merc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

@org.junit.jupiter.api.parallel.ResourceLock("console-and-loggers")
class TlsConfigTest {
    @TempDir Path directory;

    private String file(String text) throws IOException {
        Path file = Files.createTempFile(directory, "config-", ".properties");
        Files.writeString(file, text);
        return file.toString();
    }

    @Test void defaultsPreservePlaintextAndTlsSelects53641() throws Exception {
        assertFalse(new ClientConfig().isTls());
        assertEquals(53640, new ClientConfig().getPort());
        assertEquals(53640, new ServerConfig().getPort());
        String tls = file("server.tls=true\n");
        assertTrue(new ClientConfig(tls).isTls());
        assertEquals(53641, new ClientConfig(tls).getPort());
        assertEquals(53641, new ServerConfig(tls).getPort());
    }

    @Test void externalDestinationAndExplicitPortOverrideTlsDefault() throws Exception {
        String file = file("server.tls=true\nserver.host=remote.example\nserver.port=12345\n");
        assertEquals("remote.example", new ClientConfig(file).getHost());
        assertEquals(12345, new ClientConfig(file).getPort());
        assertEquals(12345, new ServerConfig(file).getPort());
    }

    @Test void invalidTlsPortsAndHostsFailEarly() throws Exception {
        for (String text : new String[] {"server.tls=secret", "server.port=0", "server.port=65536",
                "server.port=secret", "server.host=", "server.host=bad host"}) {
            String path = file(text);
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new ClientConfig(path));
            assertFalse(error.getMessage().contains("secret"));
        }
    }

    @Test void missingExternalConfigCannotFallBackToPlaintext() {
        assertThrows(IOException.class, () -> new ClientConfig(directory.resolve("missing").toString()));
        assertThrows(IOException.class, () -> new ServerConfig(directory.resolve("missing").toString()));
    }
    @Test void existingStartupModesLoadExternalTlsConfigInEitherArgumentOrder() throws Exception {
        java.lang.reflect.Method parse = MercApp.class.getDeclaredMethod("parseArgs", String[].class);
        parse.setAccessible(true);
        String path = file("server.tls=true\nserver.host=remote.example\n");
        for (String mode : new String[] {"--client", "--server"}) {
            for (String[] args : new String[][] {{mode, "--config=" + path}, {"--config=" + path, mode}}) {
                Object result = parse.invoke(null, (Object) args);
                assertEquals(mode.equals("--client") ? "CLIENT" : "SERVER", result.toString());
                java.lang.reflect.Field field = MercApp.class.getDeclaredField(mode.equals("--client") ? "clientConfig" : "serverConfig");
                field.setAccessible(true);
                Config config = (Config) field.get(null);
                assertTrue(config.isTls());
                assertEquals(53641, config.getPort());
            }
        }
    }

}
