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
 * @author rickb
 */
public class MercApp {
    /**
     * Property file located on the CLASSPATH, which is used to configure the LOGGER.
     */
    private static final String LOGGER_PROPERTIES = "/Logging.properties";

    /**
     * Events of interest occurring in this class are logged to this logger.
     */
    private static final Logger LOGGER = Logger.getLogger(MercApp.class.getName());

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
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        LOGGER.info("MercApp Initializing...:");

        // Initialize the logging properties from the logging properties files
        try {
            final InputStream strm = MercApp.class.getResourceAsStream(LOGGER_PROPERTIES);

            LogManager.getLogManager().readConfiguration(strm);

            LOGGER.info("Message logging initialization completed.");
        } catch (IOException e) {

            LOGGER.severe("Error loading ./logging.properties");
            LOGGER.severe(e.getMessage());
        }

        // Initializes the application properties from, which also sets the locale.
        ResourceMgr.instance();

        LOGGER.info("Merc properties initialization completed.");

        System.out.println("Finished initializing");

        // Create the server and then initialize the GUI client (see ntoes).
        try {
            LOGGER.info(" Starting Merc Server (Tutoring Service)...");
            // ToDo: Separate the initialization of client and server
            // Start the socket server for the Merc tutor.
            (new Thread(new MercServer())).start();

            // ToDo: This puts the main client UI thread to sleep to give the 
            // server a chance to finish starting. This won't be required once 
            // we separate the server into its own application that executes
            // on a different host from the GUI client since the server should 
            // "always" be running.
            Thread.sleep(4000);

            LOGGER.info(" Server is running.");

            LOGGER.info(" Starting Client GUI...");

            // Force the creation of the MainFrame singleton, which is not
            // made visible to the user until after they sign-in.
            MainFrame.instance();

            // Force the creation of the SplashFrame, which is displayed and
            // allows the user to sign-in or create a new student account.
            // If sign-in is successful the MainFrame is displayed.
            SplashFrame.instance();

            LOGGER.info("Merc Initialization successful.");

        } catch (InterruptedException ex) {
            Logger.getLogger(MercApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //test comment from Sophie Holland for sprint 1! (1/25/2026)
}
