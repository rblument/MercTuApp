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

import edu.regis.merc.model.LCExpression;
import edu.regis.merc.model.PendingStep;
import edu.regis.merc.model.PendingTask;
import edu.regis.merc.model.TutoringSession;

import edu.regis.merc.svc.ClientRequest;
import edu.regis.merc.svc.ServerRequestType;
import edu.regis.merc.svc.SvcFacade;
import edu.regis.merc.svc.TutorReply;

import java.awt.*;
import javax.swing.*;

import com.google.gson.Gson;

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
    private TutorView tutorView;
    private TuringMachineView turingMachineView;
    private MuRecView muRecView;
    private LambdaCalcView lambdaCalcView;

    private JButton showAnswerButton;

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

        // update the lambda view
        if (model.getProblem() != null) {
            lambdaCalcView.setModel(model.getProblem().getExpression());
        }

        // update the tutor view with instructions from session
        tutorView.setInstructions(model.getCurrentInstructions());

        // update turing machien view
        turingMachineView.setModel(model);
    }

    private void initCoreViews() {
        turingMachineView = new TuringMachineView();
        muRecView = new MuRecView();
        lambdaCalcView = new LambdaCalcView();
    }

    private void initMercTutorPanel() {

        tutorView = new TutorView();

        mercTutorPanel = new JPanel(new BorderLayout());
        mercTutorPanel.add(tutorView, BorderLayout.CENTER);

        hintButton = new JButton("Hint");
        submitButton = new JButton("Submit");
        showAnswerButton = new JButton("Show Answer");

        Dimension p1 = hintButton.getPreferredSize();
        Dimension p2 = submitButton.getPreferredSize();
        Dimension p3 = showAnswerButton.getPreferredSize();
        int w = Math.max(Math.max(p1.width, p2.width), p3.width) + 10;
        int h = Math.max(Math.max(p1.height, p2.height), p3.height);
        Dimension uniform = new Dimension(w, h);

        for (JButton b : new JButton[] { hintButton, submitButton, showAnswerButton }) {
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
        buttonColumn.add(showAnswerButton);
        buttonColumn.add(Box.createVerticalStrut(12));
        buttonColumn.add(submitButton);
        buttonColumn.add(Box.createVerticalGlue());

        mercTutorPanel.add(buttonColumn, BorderLayout.EAST);

        submitButton.addActionListener(e -> {
            LCExpression selectedNode = lambdaCalcView.getSelectedExpression();

            if (selectedNode != null) {
                System.out.println("Submitting answer for ID: " + selectedNode.getId());

                ClientRequest request = new ClientRequest(ServerRequestType.COMPLETED_STEP);

                request.setUserId(model.getStudent().getAccount().getUserId());
                request.setSecurityToken((model.getSecurityToken()));

                int currentStepId = model.currentTask().currentStep().getStep().getId();
                String jsonPayload = "{\"stepId\": " + currentStepId + ", \"selectedComponentId\": "
                        + selectedNode.getId() + "}";

                // String jsonPayload = "{\"selectedComponentId\": " + selectedNode.getId() +
                // "}";
                request.setData(jsonPayload);

                TutorReply reply = SvcFacade.instance().tutorRequest(request);

                if ("Success".equals(reply.getStatus())) {
                    JOptionPane.showMessageDialog(this,
                            "Correct! Moving to the next step.",
                            "Correct!",
                            JOptionPane.INFORMATION_MESSAGE);

                    model.currentTask().currentStep().setIsCompleted(true);

                    model.currentTask().advanceStep();
                    setModel(model);
                    // Gson gson = new Gson();
                    // TutoringSession updatedSession = gson.fromJson(reply.getData(),
                    // TutoringSession.class);

                    // setModel(updatedSession);
                } else if ("Incorrect".equals(reply.getStatus())) {
                    JOptionPane.showMessageDialog(this,
                            reply.getData(),
                            "Incorrect",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "System Error: " + reply.getData(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

                System.out.println("Server Evaluation: " + reply.getStatus());
                System.out.println("Server Data: " + reply.getData());

            } else {
                JOptionPane.showMessageDialog(this, "Please select a component first.");
            }
        });

        hintButton.addActionListener(e -> {
            if (model == null || model.currentTask() == null || model.currentTask().currentStep() == null) {
                JOptionPane.showMessageDialog(this, "No active step to get a hint for.");
                return;
            }

            PendingStep pStep = model.currentTask().currentStep();
            int currentStepId = pStep.getStep().getId();
            int currentHintIndex = pStep.getCurrentHintIndex();

            // send the step ID and the hint index
            String jsonPayload = "{\"stepId\": " + currentStepId + ", \"hintIndex\": " + currentHintIndex + "}";

            ClientRequest request = new ClientRequest(ServerRequestType.REQUEST_HINT);
            request.setUserId(model.getStudent().getAccount().getUserId());
            request.setSecurityToken(model.getSecurityToken());
            request.setData(jsonPayload);

            // send it to the server
            TutorReply reply = SvcFacade.instance().tutorRequest(request);

            // display the hint and advance
            if ("Hint".equals(reply.getStatus())) {

                // append the hint to the instructions
                String baseInstructions = model.getCurrentInstructions();
                tutorView.setInstructions(baseInstructions + "\n\nHINT: " + reply.getData());

                // advance the local hint index so the next click gets the next hint
                pStep.setCurrentHintIndex(currentHintIndex + 1);

            } else {
                JOptionPane.showMessageDialog(this, reply.getData(), "Hint Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        showAnswerButton.addActionListener(e -> {
            if (model == null || model.currentTask() == null || model.currentTask().currentStep() == null) {
                return;
            }

            int currentStepId = model.currentTask().currentStep().getStep().getId();
            String jsonPayload = "{\"stepId\": " + currentStepId + "}";

            ClientRequest request = new ClientRequest(ServerRequestType.MODEL_REQUEST);

            request.setUserId(model.getStudent().getAccount().getUserId());
            request.setSecurityToken(model.getSecurityToken());
            request.setData(jsonPayload);

            TutorReply reply = SvcFacade.instance().tutorRequest(request);

            if ("Model".equals(reply.getStatus())) {
                JOptionPane.showMessageDialog(this,
                        "The correct component ID is: " + reply.getData() + ".\nLet's move to the next step.",
                        "Answer Revealed",
                        JOptionPane.INFORMATION_MESSAGE);

                model.currentTask().currentStep().setIsCompleted(true);
                model.currentTask().advanceStep();
                setModel(model);

            } else {
                JOptionPane.showMessageDialog(this, reply.getData(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
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
