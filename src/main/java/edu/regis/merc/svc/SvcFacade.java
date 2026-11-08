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
import java.net.InetSocketAddress;
import java.util.Properties;
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

    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 10000;
    private final String host;
    private final int port;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;

    /**
     * Utility reference to Gson for converting between Java and JSon objects.
     */
    private Gson gson;
    private final Function<String, String> transport;
    private final boolean debug;

    /**
     * Host and port of the tutor server. MercApp sets this at startup from
     * the loaded configuration; otherwise the bundled client.properties is
     * loaded on first use.
     */
    private volatile ClientConfig clientConfig;

    /**
     * Initialize this facade with its Gson convertor.
     */
    private SvcFacade() {
        this(null, 0, System.getProperties(), null, IS_DEBUG);
    }

    /** Allows testing the protocol and logging without a live socket server. */
    SvcFacade(Function<String, String> transport, boolean debug) {
        this(null, 0, System.getProperties(), transport, debug);
    }

    /** Local socket tests use ephemeral endpoints without changing the singleton. */
    SvcFacade(String host, int port, Properties settings) {
        this(host, port, settings, null, IS_DEBUG);
    }

    private SvcFacade(String host, int port, Properties settings, Function<String, String> transport, boolean debug) {
        gson = new Gson();
        this.transport = transport == null ? this::send : transport;
        this.debug = debug;
        this.host = host;
        this.port = port;
        connectTimeoutMillis = timeout(settings, "edu.regis.merc.client.connectTimeoutMillis", DEFAULT_CONNECT_TIMEOUT_MS);
        readTimeoutMillis = timeout(settings, "edu.regis.merc.client.readTimeoutMillis", DEFAULT_READ_TIMEOUT_MS);
    }

    private static int timeout(Properties settings, String key, int fallback) {
        try {
            int value = Integer.parseInt(settings.getProperty(key, Integer.toString(fallback)));
            return value > 0 ? value : fallback;
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    Socket newSocket() throws IOException { return new Socket(); }

    /**
     * Send all later requests to the server named in the given configuration,
     * such as one loaded from a --config file.
     *
     * @param clientConfig the server host and port to connect to
     */
    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = java.util.Objects.requireNonNull(clientConfig);
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
     * Communication uses the configured server host and port.
     *
     * @param request a JSon encoded ClientRequest object
     * @return a JSon encoded TutorReply from the tutor
     */
    private String send(String request) {
        try (Socket client = newSocket()) {
            ClientConfig config = clientConfig;
            String destinationHost = host;
            int destinationPort = port;
            if (config == null && destinationHost == null) {
                config = new ClientConfig();
                clientConfig = config;
            }
            if (config != null) {
                destinationHost = config.getHost();
                destinationPort = config.getPort();
            }
            client.connect(new InetSocketAddress(destinationHost, destinationPort), connectTimeoutMillis);
            client.setSoTimeout(readTimeoutMillis);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter out = new PrintWriter(client.getOutputStream())) {
                out.println(request);
                out.flush();
                if (out.checkError()) throw new IOException("Request write failed.");
                String reply = in.readLine();
                if (reply == null) throw new IOException("Connection closed before reply.");
                return reply;
            }
        } catch (IOException ex) {
            // Do not log the request, server payload, or exception text containing connection details.
            LOGGER.warning("Server request could not be completed. Check the connection and retry.");
        }
        return "{'status':':ERR','data':'The server request could not be confirmed. Please try again.'}";
    }
}
