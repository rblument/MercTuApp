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
import edu.regis.merc.util.ResourceMgr;
import edu.regis.merc.view.MainFrame;
import edu.regis.merc.view.SplashFrame;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * A standalone implementation of the MERC intelligent tutoring application.
 *
 * Command-line arguments:
 *   --server    Start server only
 *   --client    Start client only
 *   --config=<file>  External configuration file (applies to both modes)
 *
 * If no arguments are provided, both server and client are started.
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

    private static final int THREAD_WAIT_MILLIS = 4000;

    /**
     * Parse command line arguments to determine execution mode and config file.
     *
     * @param args the command line arguments
     * @return an ExecutionMode enum value
     */
    private static ExecutionMode parseArgs(String[] args) {
        ExecutionMode mode = ExecutionMode.BOTH;
        String configPath = null;

        for (String arg : args) {
            if ("--server".equals(arg)) {
                mode = ExecutionMode.SERVER;
            } else if ("--client".equals(arg)) {
                mode = ExecutionMode.CLIENT;
            } else if (arg.startsWith("--config=")) {
                configPath = arg.substring(9);
            }
        }

        if (configPath != null) {
            try {
                if (mode == ExecutionMode.SERVER) {
                    new ServerConfig(configPath);
                } else if (mode == ExecutionMode.CLIENT) {
                    new ClientConfig(configPath);
                } else {
                    new ServerConfig(configPath);
                    new ClientConfig(configPath);
                }
            } catch (IOException e) {
                LOGGER.severe("Error loading config file: " + e.getMessage());
            }
        }

        return mode;
    }

    /**
     * Start the server, optionally in a separate thread.
     *
     * @param inBackground true to start server in a separate thread, false to run in current thread
     */
    private static void startServer(boolean inBackground) {
        LOGGER.info(" Starting Merc Server (Tutoring Service)...");

        mercServer = new MercServer();

        if (inBackground) {
            mercServerThread = new Thread(mercServer, "merc-server");
            mercServerThread.start();
        } else {
            mercServer.run();
        }

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    if (mercServer != null) {
                        mercServer.close();
                    }
                }, "merc-server-shutdown"));

        LOGGER.info(" Server is running.");
    }

    /**
     * Start the client GUI.
     */
    private static void startClient() {
        LOGGER.info(" Starting Client GUI...");

        MainFrame.instance();

        SplashFrame.instance();

        LOGGER.info("Merc Initialization successful.");
    }

    /**
     * Shutdown the server if it was started.
     */
    private static void shutdownServer() {
        if (mercServer != null) {
            mercServer.close();
        }

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
    }

    /**
     * Shutdown the client GUI components.
     */
    private static void shutdownClient() {
        AbandonedConnectionCleanupThread.checkedShutdown();

        MainFrame.instance().dispose();
        SplashFrame.instance().dispose();

        LOGGER.info("MERC application windows closed.");
    }

    /**
     * Gracefully shut down based on execution mode.
     *
     * @param mode the execution mode
     */
    public static synchronized void shutdown(ExecutionMode mode) {
        if (shuttingDown) {
            return;
        }

        shuttingDown = true;

        LOGGER.info("MERC shutdown initiated.");

        if (mode == ExecutionMode.SERVER || mode == ExecutionMode.BOTH) {
            shutdownServer();
        }

        if (mode == ExecutionMode.CLIENT || mode == ExecutionMode.BOTH) {
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

        ExecutionMode mode = parseArgs(args);

        boolean startServerInBackground = (mode == ExecutionMode.BOTH);

        if (mode == ExecutionMode.SERVER || mode == ExecutionMode.BOTH) {
            startServer(startServerInBackground);
        }

        if (mode == ExecutionMode.CLIENT || mode == ExecutionMode.BOTH) {
            startClient();
        }
    }

    /**
     * Execution mode for the application.
     */
    private enum ExecutionMode {
        SERVER, CLIENT, BOTH
    }

    /**
     * Gracefully shut down the Merc server, MySQL cleanup thread, and
     * application windows.
     */
    public static synchronized void shutdown() {
        shutdown(ExecutionMode.BOTH);
    }
}
