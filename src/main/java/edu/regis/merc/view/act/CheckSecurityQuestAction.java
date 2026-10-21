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

/**
 * An MVC controller handling a user GUI gesture requesting to verify the user,
 * which switches to the reset password panel in the GUI
 * (Modeled after NewUserAction)
 *
 * @author mandyroskelley
 */

import edu.regis.merc.view.SplashFrame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.SHORT_DESCRIPTION;

public class CheckSecurityQuestAction extends MercGuiAction {
    /**
     * The single instance of this check security question action.
     *
     * Create the singleton for this action, which occurs when this class
     * is loaded by the Java class loaded, as a result of the class being
     * referenced by executing CheckSecurityQuestAction.instance() in the
     * initializeComponents() method of the SplashPanel class.
     */
    private static final CheckSecurityQuestAction SINGLETON = new CheckSecurityQuestAction();

    /**
     * Return the singleton instance of this check security question action.
     *
     * @return
     */
    public static CheckSecurityQuestAction instance() {
        return SINGLETON;
    }

    /**
     * Initialize this check security question action.
     */
    public CheckSecurityQuestAction() {
        super("Verify answer");
        putValue(SHORT_DESCRIPTION, "Verify the answer to the stored security question");
        putValue(MNEMONIC_KEY, KeyEvent.VK_U);
    }

    /**
     * Handle the user's request to verify their userID, security question and answer
     * match by displaying the reset password panel.
     *
     * @param evt ignored
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        SplashFrame.instance().verifyRecoveryAnswer();
    }
}
