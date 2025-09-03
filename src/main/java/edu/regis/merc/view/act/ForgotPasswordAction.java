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

import edu.regis.merc.view.SplashFrame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.SHORT_DESCRIPTION;

/**
 * An MVC controller handling a user GUI gesture that they forgot their password, 
 * which switches to the forgot password panel in the GUI
 * (Modeled after NewUserAction)
 * 
 * @author mandyroskelley
 */
public class ForgotPasswordAction extends MercGuiAction {
    /**
     * The single instance of this forgot password action action.
     */
    private static final ForgotPasswordAction SINGLETON;
    
    /**
     * Create the singleton for this action, which occurs when this class
     * is loaded by the Java class loaded, as a result of the class being 
     * referenced by executing ForgotPasswordAction.instance() in the 
     * initializeComponents() method of the SplashPanel class.
     */
    static {
        SINGLETON = new ForgotPasswordAction();
    }

    /**
     * Return the singleton instance of this forgot password action.
     * 
     * @return 
     */
    public static ForgotPasswordAction instance() {
	return SINGLETON;
    }

    /**
     * Initialize this forgot password action.
     */
    private ForgotPasswordAction() {
        super("Forgot Password");
        putValue(SHORT_DESCRIPTION, "Request to reset password");
        putValue(MNEMONIC_KEY, KeyEvent.VK_F);
       // putValue(ACCELERATOR_KEY, getAcceleratorKeyStroke());
    }
    
    /**
     * Handle the user's request to answer their security question by displaying the forgot
     * password panel.
     * 
     * @param evt ignored
     */
    @Override
    public void actionPerformed(ActionEvent evt) {      
        SplashFrame.instance().selectForgotPassword();
    }
}
