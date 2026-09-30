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
import edu.regis.merc.model.Account;
import edu.regis.merc.svc.ClientRequest;
import edu.regis.merc.svc.ServerRequestType;
import edu.regis.merc.svc.SvcFacade;
import edu.regis.merc.svc.TutorReply;
import edu.regis.merc.view.SplashFrame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.SHORT_DESCRIPTION;
import javax.swing.JOptionPane;

/**
 * An MVC controller handling a user GUI gesture requesting to reset their
 * password within the ResetPasswordPanel. (Modeled after NewUserAction)
 *
 * @author mandyroskelley
 */
public class ResetPasswordAction extends MercGuiAction {

    /**
     * Handler for logging messages.
     */
    private static final Logger LOGGER =
            Logger.getLogger(ResetPasswordAction.class.getName());

    /**
     * The single instance of this reset password action.
     */
    private static final ResetPasswordAction SINGLETON;

    /**
     * Create the singleton for this action, which occurs when this class
     * is loaded by the Java class loaded, as a result of the class being
     * referenced by executing ResetPassword.instance() in the
     * initializeComponents() method of the NewAccountPanel class.
     */
    static {
        SINGLETON = new ResetPasswordAction();
    }

    /**
     * Return the singleton instance of this reset password action.
     *
     * @return
     */
    public static ResetPasswordAction instance() {
        return SINGLETON;
    }

    /**
     * Initialize this reset password action with the "Reset Password" text.
     */
    private ResetPasswordAction() {
        super("Reset Password");

        putValue(SHORT_DESCRIPTION, "Reset password for user");
        putValue(MNEMONIC_KEY, KeyEvent.VK_A);
    }

    /**
     * Handle the user's request to reset their password. After password is
     * reset, user will be forwarded to the SplashFrame, where they can log in.
     *
     * @param evt ignored
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        Gson gson = new Gson();

        SplashFrame frame = SplashFrame.instance();

        Account account = frame.getAccount();

        String token = frame.getResetPasswordSecurityToken();
        if (token == null || token.isBlank()) {
            JOptionPane.showMessageDialog(frame,
                    "Please verify your security answer again before resetting your password.",
                    "Password Reset", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ClientRequest request =
                new ClientRequest(ServerRequestType.RESET_PASSWORD);

        request.setUserId(account.getUserId());
        request.setSecurityToken(token);

        // Required for session tracking
        request.setData(gson.toJson(account));

        TutorReply reply = SvcFacade.instance().tutorRequest(request);

        String msg;
        String status = reply == null ? null : reply.getStatus();

        if (status == null) {
            LOGGER.log(
                    Level.WARNING,
                    "Password reset failed: invalid server response");

            msg = "Server response was invalid. Please try again or contact support.";

            JOptionPane.showMessageDialog(
                    null,
                    msg,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        switch (status) {
            case "PasswordReset":
                LOGGER.log(Level.INFO, "Password reset successful");

                frame.clearResetPassword();
                frame.clearForgotPassword();

                msg = "Password successfully reset.\n\n"
                        + "You can now sign in with your new password.";

                JOptionPane.showMessageDialog(
                        SplashFrame.instance(),
                        msg);

                frame.selectSplash();
                break;

            case "IllegalUserId":
                LOGGER.log(
                        Level.WARNING,
                        "Password reset failed: user ID not found");

                msg = "User ID does not exist: " + account.getUserId();

                JOptionPane.showMessageDialog(
                        null,
                        msg,
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);

                break;

            default:
                LOGGER.log(
                        Level.WARNING,
                        "Password reset failed: unexpected server status {0}",
                        status);

                msg = "An unexpected error occurred. "
                        + "Server responded with status: "
                        + status;

                JOptionPane.showMessageDialog(
                        null,
                        msg,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                break;
        }
    }
}
