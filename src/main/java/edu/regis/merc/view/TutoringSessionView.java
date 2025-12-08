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

import edu.regis.merc.model.PendingTask;
import edu.regis.merc.model.TutoringSession;

import java.awt.*;
import javax.swing.*;

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
    private TuringMachineView turingMachineView;
    private MuRecView muRecView;
    private LambdaCalcView lambdaCalcView;

    // Horizontal split (MuRec | LambdaCalc)
    private JSplitPane bottomSplit;

    // Vertical split (Mu/Lambda over Merc Tutor panel)
    private JSplitPane bottomVerticalSplit;

    // Main vertical split (TuringMachine over bottomVerticalSplit)
    private JSplitPane mainSplit;

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

    // New Merc Tutor panel + buttons
    private JPanel mercTutorPanel;
    private JButton hintButton;
    private JButton submitButton;

    /**
     * Initialize this view including creating and laying out its child components.
     */
    public TutoringSessionView() {
        initCoreViews();
        initMercTutorPanel();
        initSplits();
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
        
        PendingTask pTask = model.currentTask();
        
       // turingMachineView.setModel(pTask.getTask().getProblem().getTuringMachine());
       turingMachineView.setModel(model);
        
        //PendingStep pStep = pTask.getCurrentStep();
    }

    private void initCoreViews() {
        turingMachineView = new TuringMachineView();
        muRecView = new MuRecView();
        lambdaCalcView = new LambdaCalcView();
    }

    private void initMercTutorPanel() {
        mercTutorPanel = new JPanel(new BorderLayout());
        mercTutorPanel.setBorder(BorderFactory.createTitledBorder("Merc Tutor"));

        JLabel placeholder = new JLabel(" Placeholder content (to be implemented) ");
        JPanel placeholderHolder = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        placeholderHolder.add(placeholder);
        mercTutorPanel.add(placeholderHolder, BorderLayout.NORTH);

        hintButton = new JButton("Hint");
        submitButton = new JButton("Submit");

        Dimension p1 = hintButton.getPreferredSize();
        Dimension p2 = submitButton.getPreferredSize();
        int w = Math.max(p1.width, p2.width) + 10;
        int h = Math.max(p1.height, p2.height);
        Dimension uniform = new Dimension(w, h);

        for (JButton b : new JButton[]{hintButton, submitButton}) {
            b.setPreferredSize(uniform);
            b.setMinimumSize(uniform);
            b.setMaximumSize(uniform);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        JPanel buttonColumn = new JPanel();
        buttonColumn.setLayout(new BoxLayout(buttonColumn, BoxLayout.Y_AXIS));
        buttonColumn.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 12));
        buttonColumn.add(Box.createVerticalGlue());
        buttonColumn.add(hintButton);
        buttonColumn.add(Box.createVerticalStrut(12));
        buttonColumn.add(submitButton);
        buttonColumn.add(Box.createVerticalGlue());

        mercTutorPanel.add(buttonColumn, BorderLayout.EAST);
    }

    private void initSplits() {
        bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, muRecView, lambdaCalcView);
        bottomSplit.setResizeWeight(0.5);
        bottomSplit.setContinuousLayout(true);

        bottomVerticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bottomSplit, mercTutorPanel);
        bottomVerticalSplit.setResizeWeight(0.5);
        bottomVerticalSplit.setContinuousLayout(true);

        mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, turingMachineView, bottomVerticalSplit);
        mainSplit.setResizeWeight(0.5);
        mainSplit.setContinuousLayout(true);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        add(mainSplit, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            mainSplit.setDividerLocation(0.5);
            bottomVerticalSplit.setDividerLocation(0.5);
            bottomSplit.setDividerLocation(0.5);
        });
    }

    // Placeholder for future model wiring
    public void refreshSession() {
        revalidate();
        repaint();
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

