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
package edu.regis.merc.svc;

import com.google.gson.Gson;
import edu.regis.merc.ServerConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A socket-based server providing client access to the Merc tutor.
 *
 * Protocol msg ::= &lt;cmd> &lt;argData> <br>
 * &lt;cmd> ::= :CreateStudentAccount | :LaunchSession | :SignIn | <br>
 * :RequestHint | :CompletedStep | :CompletedTask <br>
 * The &lt;argData> for each command is documented in the TutorSvc interface.
 *
 * @author Rickb
 */
public class MercServer implements Runnable {

    /**
     * Handler for logging messages.
     */
    private static final Logger LOGGER = Logger.getLogger(MercServer.class.getName());

    /**
     * The socket listening for connections from the client.
     */
    private volatile ServerSocket server;

    /**
     * The currently connected client socket, if one exists.
     */
    private volatile Socket activeClient;

    /**
     * Indicates whether the server should continue accepting connections.
     */
    private volatile boolean running = true;

    /** Socket factories selected and validated during startup. */
    private final TransportSockets sockets;

    public MercServer() throws IOException {
        this(new ServerConfig());
    }

    public MercServer(ServerConfig config) throws IOException {
        this(TransportSockets.create(config));
    }

    MercServer(TransportSockets sockets) {
        this.sockets = java.util.Objects.requireNonNull(sockets);
    }

    /** Bind before launching the server thread so startup errors reach the caller. */
    public synchronized void startListening() throws IOException {
        if (!running) throw new IOException("Server has been closed.");
        if (server == null) server = sockets.listen();
    }

    /** Bind an ephemeral port for local transport tests. */
    synchronized int startListening(int port) throws IOException {
        if (!running) throw new IOException("Server has been closed.");
        if (server == null) server = sockets.listen(port);
        return server.getLocalPort();
    }

    /**
     * Create a server socket that waits for connection requests from a client,
     * which are handled by spawning a new MercTuConnection, with an associated
     * new Merc tutor, that handles all subsequent communication between the
     * client and sever.
     */
    @Override
    public void run() {
        try {
            startListening();
            try (ServerSocket listener = server) {

                while (running) {
                    try {
                        activeClient = listener.accept();
                        // Shutdown may have raced with accept before activeClient was assigned.
                        if (!running) {
                            activeClient.close();
                            break;
                        }
                        new MercTuConnection(activeClient).run();

                    } catch (SocketException e) {
                        // Closing either socket during application shutdown can
                        // cause a SocketException. This is expected when the
                        // application is shutting down.
                        if (running) {
                            LOGGER.log(Level.SEVERE, "MercServer.run()", e);
                        }

                        break;

                    } finally {
                        activeClient = null;
                    }
                }

            }
        } catch (IOException e) {
            if (running) {
                LOGGER.severe("Unable to start or accept connections on the configured server port.");
            }

        } finally {
            server = null;
            activeClient = null;

            LOGGER.info("Merc server stopped.");
        }
    }

    /**
     * Stop accepting new client connections and allow the server thread
     * to terminate normally.
     */
    public synchronized void close() {
        running = false;

        Socket currentClient = activeClient;

        if (currentClient != null && !currentClient.isClosed()) {
            try {
                currentClient.close();

            } catch (IOException e) {
                LOGGER.log(
                        Level.WARNING,
                        "Unable to close active MERC client socket.",
                        e);
            }
        }

        ServerSocket currentServer = server;

        if (currentServer != null && !currentServer.isClosed()) {
            try {
                currentServer.close();

            } catch (IOException e) {
                LOGGER.log(
                        Level.WARNING,
                        "Unable to close MERC server socket.",
                        e);
            }
        }
    }

    /**
     * A connection to a client which handles Merc tutoring requests to the
     * server (tutor).
     */
    private class MercTuConnection implements Runnable {

        /**
         * The socket connection with the client
         */
        private final Socket client;

        /**
         * The Merc tutor associated with this connection.
         */
        private final TutorSvc tutor;

        /**
         * Initialize this connection by creating a new Merc tutor that is
         * communicating with the client associated with the given socket.
         *
         * @param client an established socket connection to a client
         */
        public MercTuConnection(Socket client) {
            this.client = client;

            tutor = new MercTutor();
        }

        /**
         * Read a JSon encoded request from the client to the ShaTu tutor.
         */
        @Override
        public void run() {
            Gson gson = new Gson();

            try (Socket connection = client) {
                TransportSockets.prepare(connection);
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        PrintWriter out = new PrintWriter(connection.getOutputStream(), true)) {
                    String msg = in.readLine();
                    if (msg == null) return;
                    ClientRequest request = gson.fromJson(msg, ClientRequest.class);
                    TutorReply reply = tutor.request(request);
                    out.println(gson.toJson(reply));
                }
            } catch (IOException | RuntimeException e) {
                if (running) {
                    LOGGER.warning("Client connection failed or sent an invalid request; connection closed.");
                }
            }
        }
    }
}
