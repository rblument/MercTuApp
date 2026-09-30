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
package edu.regis.merc.view.act;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.regis.merc.model.Account;
import edu.regis.merc.model.Student;
import edu.regis.merc.util.StudentDeserializer;
import edu.regis.merc.model.Timeout;
import edu.regis.merc.util.TimeoutDeserializer;
import edu.regis.merc.model.TutoringSession;
import edu.regis.merc.svc.ClientRequest;
import edu.regis.merc.svc.ServerRequestType;
import edu.regis.merc.svc.SvcFacade;
import edu.regis.merc.svc.TutorReply;
import edu.regis.merc.view.MainFrame;
import edu.regis.merc.view.SplashFrame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.SHORT_DESCRIPTION;
import javax.swing.JOptionPane;

// imports added to build a deserializer 
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonDeserializationContext;
import java.lang.reflect.Type;
import edu.regis.merc.model.LCExpression;
import edu.regis.merc.model.LCVariable;
import edu.regis.merc.model.LCAbstraction;
import edu.regis.merc.model.LCApplication;

/**
 * An (MVC) controller handling a GUI gesture representing a user's request to
 * login to the tutor via the WelcomePanel.
 *
 * If successful, a trial will be started or resumed for the student via launch
 * session.
 *
 * @author rickb
 */
public class SignInAction extends MercGuiAction {

    /**
     * Exceptions occurring in this class are also logged to this logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SignInAction.class.getName());

    /**
     * The single instance of this sign-in action.
     */
    private static final SignInAction SINGLETON;

    // Timeout after user inactivity
    // private final InactivityManager inactivityManager = new InactivityManager();

    /**
     * Create the singleton for this action, which occurs when this class is
     * loaded by the Java class loaded, as a result of the class being
     * referenced by executing SignInAction.instance() in the
     * initializeComponents() method of the SplashPanel class.
     */
    static {
        SINGLETON = new SignInAction();
    }

    /**
     * Return the singleton instance of this sign-in action.
     *
     * @return
     */
    public static SignInAction instance() {
        return SINGLETON;
    }

    /**
     * Initialize action with the "Sign In" text and set its text.
     */
    private SignInAction() {
        super("Sign In");
        putValue(SHORT_DESCRIPTION, "Sign-in to the tutor");
        putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        // putValue(ACCELERATOR_KEY, getAcceleratorKeyStroke());
    }

    /** Creates the parser for the complete session returned by sign-in. */
    static Gson createSessionGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Student.class, new StudentDeserializer())
                .registerTypeAdapter(Timeout.class, new TimeoutDeserializer())
                .registerTypeAdapter(LCExpression.class, new JsonDeserializer<LCExpression>() {
                    @Override
                    public LCExpression deserialize(
                            JsonElement json,
                            Type typeOfT,
                            JsonDeserializationContext context)
                            throws JsonParseException {

                        JsonObject jsonObject = json.getAsJsonObject();

                        // rule 1: if it has a 'name' field it's a var
                        if (jsonObject.has("name")
                                && !jsonObject.get("name").isJsonNull()) {
                            return context.deserialize(
                                    jsonObject, LCVariable.class);
                        }
                        // rule 2: if it has 'function' or 'arg' fields, it's an app
                        else if (jsonObject.has("function")
                                || jsonObject.has("arg")) {
                            return context.deserialize(
                                    jsonObject, LCApplication.class);
                        }
                        // rule 3: otherwise it's an Abstraction
                        else {
                            return context.deserialize(
                                    jsonObject, LCAbstraction.class);
                        }
                    }
                })
                .create();
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Gson gson = createSessionGson();

        Account account = SplashFrame.instance().getAccount();
        String logUserId = sanitizeLogValue(account.getUserId());

        ClientRequest request
                = new ClientRequest(ServerRequestType.SIGN_IN);

        request.setData(gson.toJson(account));

        TutorReply reply;
        try {
            reply = SvcFacade.instance().tutorRequest(request);
        } catch (JsonParseException ex) {
            showSignInError("The server returned an invalid response. Please try again.");
            return;
        }

        if (reply == null || reply.getStatus() == null) {
            showSignInError("No valid response was received from the server. Please try again.");
            return;
        }

        switch (reply.getStatus()) {
            case "Authenticated":
                LOGGER.log(Level.INFO, "Sign-in successful; userId={0}", logUserId);

                TutoringSession session;
                try {
                    session = gson.fromJson(reply.getData(), TutoringSession.class);
                } catch (JsonParseException ex) {
                    showSignInError("Your tutoring session could not be loaded. Please try again.");
                    return;
                }
                if (session == null) {
                    showSignInError("Your tutoring session could not be loaded. Please try again.");
                    return;
                }

                // Initialize main frame instance.
                // This is used after selecting a mode from the dashboard.
                MainFrame frame = MainFrame.instance();

                frame.setModel(session);

                SplashFrame.instance().setVisible(false);

                frame.setVisible(true);

                // Start tracking user inactivity
                // inactivityManager.startTracking();
                break;

            case "InvalidPassword":
            case "UnknownUser":
                LOGGER.log(
                        Level.WARNING,
                        "Sign-in failed: invalid credentials; userId={0}", logUserId);

                SplashFrame.instance().invalidPass();
                break;

            default:
                LOGGER.log(
                        Level.WARNING,
                        "Unexpected sign-in response status: {0}; userId={1}",
                        new Object[] { sanitizeLogValue(reply.getStatus()), logUserId });
                showSignInError("Unable to sign in. Please check that the tutoring server is running and try again.");
                break;
        }
    }

    private static String sanitizeLogValue(String value) {
        return value == null ? "(missing)" : value.replaceAll("[\\p{Cntrl}\\p{Zl}\\p{Zp}]", "_");
    }

    private void showSignInError(String message) {
        JOptionPane.showMessageDialog(SplashFrame.instance(), message,
                "Sign In Error", JOptionPane.ERROR_MESSAGE);
    }
}
