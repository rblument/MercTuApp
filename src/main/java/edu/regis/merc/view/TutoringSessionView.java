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
package edu.regis.merc.view;

import edu.regis.merc.model.PendingStep;
import edu.regis.merc.model.PendingTask;
import edu.regis.merc.model.StepSubType;
import edu.regis.merc.model.TutoringSession;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/**
 * Displays a tutoring session that allows a student to practice.
 *
 * The "Do One" view that allows either a student to select a task to practice 
 * or the tutor selects a task based on the current student model.
 * 
 * Various aspects of the tutoring session are displayed in the child components
 * of this view.
 *
 * @author rickb
 */
public class TutoringSessionView extends GPanel {
    /**
     * Universal button for returning to the dashboard.
     */
    private JButton dashboardButton;
    
    /*
     * Universal button for logging off. 
     */    
    private JButton logoutButton;
    /**
     * The tutoring session model displayed in this view.
     */
    
    private TutoringSession model;

    /**
     * The scroll pane for the step selector view
     */
    private JScrollPane scrollPane;

    /**
     * The container for the StepView
     */
    private JPanel stepViewContainer;

    /**
     * The Split Pane to split step and main view panes
     */
    private JSplitPane splitPane;

    /**
     * Add panel for the dashboard button
     */
    private JPanel dashboardPanel;

    /**
     * Initialize this view including creating and laying out its child components.
     */
    public TutoringSessionView() {
        initializeComponents();
        layoutComponents();
    }


    /**
     * Return the model currently displayed in this view.
     *
     * @return a TutoringSession
     */
    public TutoringSession getModel() {
        return model;
    }

    /**
     * Display the given model in this view.
     *
     * @param model a TutoringSession.
     */
    public void setModel(TutoringSession model) {
        this.model = model;
        
        //PendingTask pTask = model.currentTask();
        //PendingStep pStep = pTask.getCurrentStep();
    }
    


    /**
     * Setup components that will be used across all views
     */
    private void initializeComponents() {

    }

    /**
     * Layout the components used for all views
     */
    private void layoutComponents() {
        JLabel testLabel = new JLabel("Tutoring Session View");
        
 	addc(testLabel, 0,0, 2,1, 1.0,0.0,
	     GridBagConstraints.NORTHWEST,  GridBagConstraints.HORIZONTAL,
	     5,5,5,5);	
    }

    /**
     * Toggles the given button on/off
     * Ensures the GUI updates to display the button's new state
     **/
    public void toggleButton(JButton button) {
        button.setEnabled(!button.isEnabled());
        button.repaint();
        button.revalidate();
    }

    /**
     * A class called by logout Button to logout
     */
    public void logout() {
        SplashFrame.instance().logout();
    }
}

