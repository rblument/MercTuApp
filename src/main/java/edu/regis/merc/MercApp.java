/*
 * MERC^T: Multiple External Representations of Computation Tutor
 *
 *  (C) Richard Blumenthal, All rights reserved
 *
 *  Unauthorized use, duplication or distribution without the authors'
 *  permission is strictly prohibited.
 *
 *  Unless required by applicable law or agreed to in writing, this
 *  software is distributed on an "AS IS" basis without warranties
 *  or conditions of any kind, either expressed or implied.
 */
package edu.regis.merc;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import edu.regis.merc.svc.MercServer;
import edu.regis.merc.svc.SvcFacade;
import edu.regis.merc.util.ResourceMgr;
import edu.regis.merc.view.MainFrame;
import edu.regis.merc.view.SplashFrame;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * A standalone implementation of the MERC intelligent tutoring application.
 *
 * Command-line arguments:
 *   --server    Start server only
 *   --client    Start client only
 *   --config=<file>  External configuration file (applies to both modes)
 *
 * If no arguments are provided, both server and client are started.
 * --server and --client cannot be combined, and any other argument, a config
 * file that cannot be read, or an invalid port stops startup with an error.
 *
 * @author rickb
 */
public class MercApp {

    private MercApp() {}

    /**
     * Property file located on the CLASSPATH, which is used to configure the LOGGER.
     */
    private static final String LOGGER_PROPERTIES = "/Logging.properties";

    /**
     * Events of interest occurring in this class are logged to this logger.
     */
    private static final Logger LOGGER = Logger.getLogger(MercApp.class.getName());

    /**
     * The Merc tutoring server used by this application.
     */
    private static MercServer mercServer;

    /**
     * Thread running the Merc tutoring server.
     */
    private static Thread mercServerThread;

    /**
     * Prevents shutdown from being initiated more than once.
     */
    private static boolean shuttingDown = false;

    /**
     * Ensures the server is stopped only once, whether by shutdown() or by the
     * JVM shutdown hook.
     */
    private static final AtomicBoolean SERVER_STOPPED = new AtomicBoolean(false);

    private static final int THREAD_WAIT_MILLIS = 4000;

    /**
     * How long to wait for the server to start accepting connections.
     */
    private static final int SERVER_START_TIMEOUT_SECONDS = 10;

    private static final String USAGE =
            "Usage: MercApp [--server | --client] [--config=<file>]";

    /**
     * The mode this application was started in, or null before main() runs.
     */
    private static ExecutionMode runningMode;

    /**
     * Configuration instances (created during parseArgs for use during startup).
     */
    private static ServerConfig serverConfig;
    private static ClientConfig clientConfig;

    /**
     * Parse command line arguments to determine execution mode, then load the
     * server and client configuration.
     *
     * @param args the command line arguments
     * @return an ExecutionMode enum value
     * @throws IllegalArgumentException if an argument is unknown, --server and
     *         --client are combined, or a configured port is invalid
     * @throws IOException if the --config file cannot be read
     */
    private static ExecutionMode parseArgs(String[] args) throws IOException {
        ExecutionMode mode = ExecutionMode.BOTH;
        String configPath = null;

        for (String arg : args) {
            if ("--server".equals(arg) || "--client".equals(arg)) {
                ExecutionMode requested = "--server".equals(arg)
                        ? ExecutionMode.SERVER : ExecutionMode.CLIENT;

                // mode is still BOTH, the default, until a mode flag is seen,
                // so this only rejects --server combined with --client.
                if (mode != ExecutionMode.BOTH && mode != requested) {
                    throw new IllegalArgumentException(
                            "--server and --client cannot be used together");
                }

                mode = requested;
            } else if (arg.startsWith("--config=")) {
                configPath = arg.substring("--config=".length());

                if (configPath.isBlank()) {
                    throw new IllegalArgumentException("--config needs a file path");
                }
            } else {
                throw new IllegalArgumentException("Unknown startup option. Use --server, --client, or --config=<file>; "
                        + "enable TLS with server.tls=true in the configuration file.");
            }
        }

        // Load only after reading every argument, so the result does not depend
        // on argument order. A --config file that cannot be read stops startup
        // instead of silently falling back to the defaults.
        serverConfig = new ServerConfig(configPath);
        clientConfig = new ClientConfig(configPath);

        // Read the ports now so an invalid value stops startup instead of
        // failing later, on the first connection.
        serverConfig.getPort();
        clientConfig.getPort();

        return mode;
    }

    /**
     * Start the server on its own thread and wait until it is accepting
     * connections. Exits the application if the server cannot start, for
     * example because another process is already using the port.
     */
    private static void startServer() throws IOException {
        LOGGER.info(" Starting Merc Server (Tutoring Service) on port "
                + serverConfig.getPort() + "...");

        mercServer = new MercServer(serverConfig);

        // Register before starting the server so Ctrl+C or SIGTERM always
        // closes it, including in server-only mode.
        Runtime.getRuntime().addShutdownHook(
                new Thread(MercApp::shutdownServer, "merc-server-shutdown"));

        mercServerThread = new Thread(mercServer, "merc-server");
        mercServerThread.start();

        boolean listening;

        try {
            listening = mercServer.awaitListening(
                    SERVER_START_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            listening = false;
        }

        if (!listening) {
            LOGGER.severe("Merc server could not listen on port "
                    + serverConfig.getPort() + "; exiting.");
            System.exit(1);
        }

        LOGGER.info(" Server is running.");
    }

    /**
     * Start the client GUI.
     */
    private static void startClient() {
        LOGGER.info(" Starting Client GUI, using the server at "
                + clientConfig.getHost() + ":" + clientConfig.getPort() + "...");

        // Create and display Swing components on the event dispatch thread.
        SwingUtilities.invokeLater(() -> {
            // The main tutoring window stays hidden until sign-in succeeds.
            MainFrame.instance();
            // The splash window provides sign-in and account recovery.
            SplashFrame.instance();

            LOGGER.info("Merc Initialization successful.");
        });
    }

    /**
     * Stop the server and the MySQL cleanup thread, if this process started
     * the server. Safe to call more than once, including from the shutdown hook.
     */
    private static void shutdownServer() {
        if (mercServer == null || !SERVER_STOPPED.compareAndSet(false, true)) {
            return;
        }

        mercServer.close();

        if (mercServerThread != null
                && mercServerThread != Thread.currentThread()) {

            try {
                mercServerThread.join(THREAD_WAIT_MILLIS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                LOGGER.log(
                        Level.WARNING,
                        "Interrupted while waiting for MERC server to stop.",
                        e);
            }
        }

        // Stop the MySQL Connector/J cleanup thread. Only the server uses MySQL.
        AbandonedConnectionCleanupThread.checkedShutdown();
    }

    /**
     * Shutdown the client GUI components.
     */
    private static void shutdownClient() {
        MainFrame.instance().dispose();
        SplashFrame.instance().dispose();

        LOGGER.info("MERC application windows closed.");
    }

    /**
     * Gracefully shut down whatever this process started: the Merc server and
     * MySQL cleanup thread in server or both modes, and the application
     * windows in client or both modes.
     */
    public static synchronized void shutdown() {
        if (shuttingDown) {
            return;
        }

        shuttingDown = true;

        LOGGER.info("MERC shutdown initiated.");

        // Only server and both modes start the server.
        if (runningMode == ExecutionMode.SERVER || runningMode == ExecutionMode.BOTH) {
            shutdownServer();
        }

        // Only client and both modes create the windows. Server-only mode
        // skips this so it doesn't create windows just to dispose of them.
        if (runningMode == ExecutionMode.CLIENT || runningMode == ExecutionMode.BOTH) {
            shutdownClient();
        }
    }

    /**
     * Configure the LOGGER with the properties found in the LOGGER_PROPERTIES
     * file found on the CLASSPATH.ch
     */
    static {
        final InputStream strm
                = MercApp.class.getResourceAsStream(LOGGER_PROPERTIES);

        try {
            LogManager.getLogManager().readConfiguration(strm);
        } catch (IOException e) {

            LOGGER.severe("Error loading ./logging.properties");
            LOGGER.severe(e.getMessage());
        }
    }

    /**
     * Main entry point for the ShaTut application, which will display the UI.
     * Launch the MercTu desktop application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LOGGER.info("MercApp Initializing...:");

        try {
            final InputStream strm = MercApp.class.getResourceAsStream(LOGGER_PROPERTIES);

            LogManager.getLogManager().readConfiguration(strm);

            LOGGER.info("Message logging initialization completed.");
        } catch (IOException e) {
            LOGGER.severe("Error loading ./logging.properties");
            LOGGER.severe(e.getMessage());
        }

        ResourceMgr.instance();

        LOGGER.info("Merc properties initialization completed.");

        System.out.println("Finished initializing");

        try {
            runningMode = parseArgs(args);
            // Validate TLS trust and install the resolved destination before starting any local listener or GUI.
            if (runningMode != ExecutionMode.SERVER) SvcFacade.instance().setClientConfig(clientConfig);

            if (runningMode == ExecutionMode.SERVER || runningMode == ExecutionMode.BOTH) {
                startServer();
            }

            if (runningMode == ExecutionMode.CLIENT || runningMode == ExecutionMode.BOTH) {
                startClient();
            }
        } catch (IllegalArgumentException e) {
            LOGGER.severe(e.getMessage());
            System.err.println(USAGE);
            shutdownServer();
            System.exit(1);
        } catch (IOException e) {
            LOGGER.severe("Merc startup failed: " + e.getMessage());
            shutdownServer();
            System.exit(1);
        }

        // In server-only mode main() returns here, and the merc-server thread
        // keeps the application running until Ctrl+C or SIGTERM stops it.
    }

    /**
     * Execution mode for the application.
     */
    private enum ExecutionMode {
        SERVER, CLIENT, BOTH
    }
}
