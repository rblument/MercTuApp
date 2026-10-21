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

import edu.regis.merc.MercApp;
import edu.regis.merc.model.LCExpression;
import edu.regis.merc.model.PendingStep;
import edu.regis.merc.model.PendingTask;
import edu.regis.merc.model.RepresentationKind;
import edu.regis.merc.model.TutoringSession;

import edu.regis.merc.svc.ClientRequest;
import edu.regis.merc.svc.ServerRequestType;
import edu.regis.merc.svc.StudentPayload;
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
    private JButton closeButton;

    /**
     * Converts request payloads to JSon.
     */
    private final Gson gson = new Gson();

    /**
     * The representation the student most recently selected a component in, or
     * null if nothing is selected.
     *
     * Held here rather than read back from the panels at submit time. The
     * panels release each other on selection, so recording the selection as it
     * happens means there is only ever one.
     */
    private RepresentationKind selectedRepresentation = null;

    /**
     * The id of the selected component, unique only within
     * selectedRepresentation.
     */
    private int selectedComponentId = -1;

    /**
     * Initialize this view including creating and laying out its child components.
     */
    private final java.util.function.Function<ClientRequest, TutorReply> logoutRequests;
    private final Runnable loggedOut;
    private final java.util.function.Consumer<String> logoutError;

    public TutoringSessionView() {
        this(request -> SvcFacade.instance().tutorRequest(request), () -> SplashFrame.instance().logout(),
                message -> JOptionPane.showMessageDialog(null, message, "Logout", JOptionPane.ERROR_MESSAGE));
    }

    TutoringSessionView(java.util.function.Function<ClientRequest, TutorReply> requests, Runnable loggedOut,
            java.util.function.Consumer<String> error) {
        this.logoutRequests = requests;
        this.loggedOut = loggedOut;
        this.logoutError = error;
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

        // Re-rendering the panels clears their own selections, so the
        // selection recorded here has to go too. Otherwise a selection made
        // for the previous step would still be submitted against this one.
        selectedRepresentation = null;
        selectedComponentId = -1;

        // update the lambda view
        if (model.getProblem() != null) {
            lambdaCalcView.setModel(model.getProblem().getExpression());

            muRecView.setModel(model.getProblem().getMuFunction());
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

        // A step is answered with one component in one representation, so
        // selecting in either panel releases the other and records what was
        // chosen. clearSelection does not notify a listener, so this does not
        // recurse. A third selectable panel has to be registered here too,
        // and until it is its selections are simply never reported.
        lambdaCalcView.setSelectionListener((representation, componentId) -> {
            muRecView.clearSelection();
            recordSelection(representation, componentId);
        });

        muRecView.setSelectionListener((representation, componentId) -> {
            lambdaCalcView.clearSelection();
            recordSelection(representation, componentId);
        });
    }

    /**
     * Remember the component a student selected, so that submitting does not
     * have to ask each panel in turn which one holds a selection.
     *
     * @param representation the panel the selection was made in
     * @param componentId the id of the component within that representation
     */
    private void recordSelection(RepresentationKind representation, int componentId) {
        this.selectedRepresentation = representation;
        this.selectedComponentId = componentId;
    }

    private void initMercTutorPanel() {

        tutorView = new TutorView();

        mercTutorPanel = new JPanel(new BorderLayout());
        mercTutorPanel.add(tutorView, BorderLayout.CENTER);

        hintButton = new JButton("Hint");
        submitButton = new JButton("Submit");
        showAnswerButton = new JButton("Show Answer");
        closeButton = new JButton("Close");
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        Dimension p1 = hintButton.getPreferredSize();
        Dimension p2 = submitButton.getPreferredSize();
        Dimension p3 = showAnswerButton.getPreferredSize();
        int w = Math.max(Math.max(p1.width, p2.width), p3.width) + 10;
        int h = Math.max(Math.max(p1.height, p2.height), p3.height);
        Dimension uniform = new Dimension(w, h);

        for (JButton b : new JButton[] { hintButton, submitButton, showAnswerButton, logoutButton, closeButton }) {
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
        buttonColumn.add(Box.createVerticalStrut(12));
        buttonColumn.add(logoutButton);
        buttonColumn.add(Box.createVerticalStrut(12));
        buttonColumn.add(closeButton);
        buttonColumn.add(Box.createVerticalGlue());

        mercTutorPanel.add(buttonColumn, BorderLayout.EAST);
        closeButton.addActionListener(e -> MercApp.shutdown());

        submitButton.addActionListener(e -> {
            if (selectedRepresentation != null) {
                System.out.println("Submitting answer for ID: " + selectedComponentId
                        + " in " + selectedRepresentation.name());

                ClientRequest request = new ClientRequest(ServerRequestType.COMPLETED_STEP);
                request.setUserId(model.getStudent().getAccount().getUserId());
                request.setSecurityToken(model.getSecurityToken());

                int currentStepId = model.currentTask().currentStep().getStep().getId();

                StudentPayload payload = new StudentPayload(currentStepId);
                payload.setRepresentation(selectedRepresentation);
                payload.setSelectedComponentId(selectedComponentId);
                request.setData(gson.toJson(payload));

                TutorReply reply = SvcFacade.instance().tutorRequest(request);

                if ("Success".equals(reply.getStatus())) {
                    JOptionPane.showMessageDialog(this, "Correct! Moving to the next step.", "Correct!",
                            JOptionPane.INFORMATION_MESSAGE);
                    model.currentTask().currentStep().setIsCompleted(true);
                    model.currentTask().advanceStep();
                    setModel(model);
                } else if ("TaskComplete".equals(reply.getStatus())) {
                    // The last step of the task. There is nothing to advance
                    // to, so mark it done and leave the view where it is
                    // rather than re-rendering the same question.
                    model.currentTask().currentStep().setIsCompleted(true);
                    setModel(model);
                    JOptionPane.showMessageDialog(this, "Correct! You have completed this task.", "Task Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                } else if ("Incorrect".equals(reply.getStatus())) {
                    JOptionPane.showMessageDialog(this, reply.getData(), "Incorrect", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "System Error: " + reply.getData(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
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
            StudentPayload payload = new StudentPayload(currentStepId);
            payload.setHintIndex(currentHintIndex);
            String jsonPayload = gson.toJson(payload);

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
            String jsonPayload = gson.toJson(new StudentPayload(currentStepId));

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
        if (model == null || !logoutButton.isEnabled()) return;
        TutoringSession current = model;
        ClientRequest request = new ClientRequest(ServerRequestType.LOGOUT);
        request.setUserId(current.getStudent().getAccount().getUserId());
        request.setSecurityToken(current.getSecurityToken());
        setLogoutBusy(true);
        SwingWorker<TutorReply, Void> worker = new SwingWorker<>() {
            @Override protected TutorReply doInBackground() { return logoutRequests.apply(request); }
            @Override protected void done() {
                if (model != current) return;
                try {
                    TutorReply reply = get();
                    if (reply != null && "LoggedOut".equals(reply.getStatus())) {
                        current.setSecurityToken("");
                        model = null;
                        loggedOut.run();
                    } else {
                        logoutError.accept("Logout was not confirmed. Please try again; your session has not been cleared.");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    logoutError.accept("Logout was interrupted. Please try again.");
                } catch (java.util.concurrent.ExecutionException | RuntimeException ex) {
                    logoutError.accept("Unable to log out. Check the server connection and try again.");
                } finally {
                    setLogoutBusy(false);
                }
            }
        };
        // A request-owned thread exits after completion; SwingWorker.execute() leaves an idle shared pool under exec:java.
        Thread thread = new Thread(worker, "merc-logout-request");
        thread.setDaemon(true);
        thread.start();
    }

    private void setLogoutBusy(boolean busy) {
        logoutButton.setEnabled(!busy);
        submitButton.setEnabled(!busy);
        hintButton.setEnabled(!busy);
        showAnswerButton.setEnabled(!busy);
    }
}
