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
import edu.regis.merc.ClientConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Function;

/**
 * A Facade that standardizes requests from the GUI Client to the MERC tutor
 * server via a socket connection with requests and replies encoded as JSon
 * encoded object strings
 *
 * Conceptually, this facade is part of the Swing-based GUI client.
 *
 * This facade exists to facilitate a subsequent port from the Swing-based GUI
 * to a Restful HTML client. As such, it is currently a logical part of the
 * Swing-based GUI client, but for a Web-Browser client, it would be implemented
 * as part of the Server Socket Connection, which would forward the HTTML JSon
 * HttpReqeust to the tutor using POJO message passing (i.e. method invocation).
 *
 * Currently, the following request types are supported:
 * {"request": "CreateStudentAccount",
 *  "data": {"userId":"userId@regis.edu",
 *           "password":"sha256Password",
 *           "firstName":"Rick",
 *           "lastName":"Justice"
 *          }
 * }
 *
 * {"request": "SignIn",
 *  "data":  {"userId": "user email addr"}
 * }
 *
 * @author rickb
 */
public class SvcFacade {
    /**
     * Enables request/reply status messages, never their JSON payloads.
     */
    private static final boolean IS_DEBUG = false;

    /**
     * The single instance of the tutor facade.
     */
    private static final SvcFacade SINGLETON;

    /**
     * Create the singleton for this facade, which occurs when this class
     * is loaded by the Java class loaded, as a result of the class being
     * referenced be the SvcFacade.instance() method occurring initially
     * in the SignInAction class.
     */
    static {
        SINGLETON = new SvcFacade();
    }

    /**
     * Return the singleton implementing the TutorFacade interface.
     *
     * @return a TutorFacade
     */
    public static SvcFacade instance() {
        return SINGLETON;
    }

    /**
     * Handler for logging messages.
     */
    private static final Logger LOGGER = Logger.getLogger(SvcFacade.class.getName());

    private volatile TransportSockets sockets;

    /** Use the resolved external client configuration, including TLS, before GUI startup. */
    public void configure(ClientConfig config) throws IOException {
        configure(TransportSockets.create(config));
    }

    /** Install startup transport before the GUI can issue requests. */
    void configure(TransportSockets sockets) {
        this.sockets = java.util.Objects.requireNonNull(sockets);
    }

    /**
     * Utility reference to Gson for converting between Java and JSon objects.
     */
    private Gson gson;
    private final Function<String, String> transport;
    private final boolean debug;

    /**
     * Initialize this facade with its Gson convertor.
     */
    private SvcFacade() {
        gson = new Gson();
        transport = this::send;
        debug = IS_DEBUG;
    }

    /** A configured client, also usable by local socket tests without changing the singleton. */
    SvcFacade(TransportSockets sockets) {
        this();
        configure(sockets);
    }

    /** Allows testing the protocol and logging without a live socket server. */
    SvcFacade(Function<String, String> transport, boolean debug) {
        gson = new Gson();
        this.transport = java.util.Objects.requireNonNull(transport);
        this.debug = debug;
    }

    /**
     * Encodes the given client request as a JSon object and sends it to the
     * tutor returning the tutor's reply.
     *
     * @param request the ClientRequest being sent to the tutor.
     * @return the TutorReply from the tutor.
     */
    public TutorReply tutorRequest(ClientRequest request) {
        String jsonRequest = gson.toJson(request);

        if (debug)
            LOGGER.log(Level.INFO, "Sending tutor request");

        String jsonReply = transport.apply(jsonRequest);

        if (debug)
            LOGGER.log(Level.INFO, "Tutor reply received");

        return gson.fromJson(jsonReply, TutorReply.class);
    }

    /**
     * Send the given request to the ShaTu server and return the reply.
     *
     * Communication uses the host, port, and transport selected at startup.
     *
     * @param request a JSon encoded ClientRequest object
     * @return a JSon encoded TutorReply from the tutor
     */
    private String send(String request) {
        try {
            TransportSockets selected = sockets == null ? TransportSockets.create(new ClientConfig()) : sockets;
            return send(request, selected);
        } catch (IOException | IllegalArgumentException ex) {
            LOGGER.severe("Client configuration failed; check transport settings and JSSE stores.");
            return connectionError();
        }
    }

    private String send(String request, TransportSockets selected) {
        try (Socket client = selected.connect();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter out = new PrintWriter(client.getOutputStream())) {
            out.println(request);
            out.flush();
            if (out.checkError()) throw new IOException("Request write failed.");
            return in.readLine();
        } catch (UnknownHostException e) {
            LOGGER.severe("Cannot resolve the configured server host.");
        } catch (IOException e) {
            LOGGER.severe("Server connection failed; check the host, port, TLS trust, and certificate hostname.");
        }
        return connectionError();
    }

    private String connectionError() {
        return "{'status':':ERR','data':'A non-recoverable error occurred in the socket connection (see logs)'}";
    }
}
