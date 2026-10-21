/*
 * MERC^T: Multiple External Representations of Computation Tutor
 *
 * (C) Richard Blumenthal, All rights reserved
 *
 * Unauthorized use, duplication or distribution without the authors'
 * permission is strictly prohibited.
 *
 * Unless required by applicable law or agreed to in writing, this
 * software is distributed on an "AS IS" basis without warranties
 * or conditions of any kind, either expressed or implied.
 */
package edu.regis.merc.view;

import com.google.gson.Gson;
import edu.regis.merc.model.Account;
import edu.regis.merc.model.RecoveryRequest;
import edu.regis.merc.model.SecurityQuestionPrompt;
import edu.regis.merc.svc.ClientRequest;
import edu.regis.merc.svc.ServerRequestType;
import edu.regis.merc.svc.SvcFacade;
import edu.regis.merc.svc.TutorReply;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Two-stage recovery: identify the account, then answer its stored question. */
public class ForgotPasswordPanel extends GPanel {
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final JTextField userId = new JTextField(25);
    private final JPasswordField secAnswer = new JPasswordField(25);
    private final JTextArea questionText = new JTextArea(2, 35);
    private final JButton lookupBut = new JButton("Continue");
    private final JButton verifyBut = new JButton("Verify answer");
    private final JButton backBut = new JButton("Back");
    private final JTextArea msg = new JTextArea("Enter your account email to retrieve your security question.", 2, 30);

    private final Gson gson = new Gson();
    private final Function<ClientRequest, TutorReply> requests;
    private final BiConsumer<String, String> verified;
    private String resolvedUserId;
    private boolean busy;
    // Each email change or navigation invalidates any earlier asynchronous reply.
    private long generation;

    public ForgotPasswordPanel() {
        this(request -> SvcFacade.instance().tutorRequest(request), (email, token) -> {
            SplashFrame frame = SplashFrame.instance();
            frame.initializeResetPassword(email, token);
            frame.selectResetPassword(email);
        }, () -> SplashFrame.instance().selectSplash());
    }

    ForgotPasswordPanel(Function<ClientRequest, TutorReply> requests,
            BiConsumer<String, String> verified, Runnable back) {
        this.requests = requests;
        this.verified = verified;
        layoutComponents();

        userId.getDocument().addDocumentListener(listener(() -> {
            invalidateQuestion();
            msg.setText("Enter your account email, then select Continue.");
        }));
        secAnswer.getDocument().addDocumentListener(listener(this::updateButtons));
        lookupBut.addActionListener(e -> lookupQuestion());
        verifyBut.addActionListener(e -> verifyAnswer());
        userId.addActionListener(e -> lookupBut.doClick());
        secAnswer.addActionListener(e -> verifyBut.doClick());
        backBut.addActionListener(e -> {
            clearFields();
            back.run();
        });
        updateButtons();
    }

    private void layoutComponents() {
        Color navy = new Color(0, 43, 73);
        Color gold = new Color(241, 196, 0);
        setBackground(navy);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setPreferredSize(new Dimension(850, 540));

        GPanel header = new GPanel();
        header.setBackground(gold);
        JLabel heading = new JLabel("MERC — Account recovery");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 20f));
        heading.setForeground(navy);
        header.addc(heading, 0, 0, 1, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, 14, 16, 14, 16);
        addc(header, 0, 0, 2, 1, 1, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, 0, 0, 12, 0);

        GPanel overview = new GPanel();
        overview.setBackground(gold);
        overview.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JLabel title = new JLabel("Forgot your password?");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(navy);
        overview.addc(title, 0, 0, 1, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, 0, 0, 14, 0);
        JTextArea guidance = new JTextArea("Enter your account email to see the security question you chose.\n\n"
                + "Answer that question to continue to password reset.\n\n"
                + "You no longer need to select or remember which question you used.", 10, 22);
        configureText(guidance, gold, navy);
        overview.addc(guidance, 0, 1, 1, 1, 1, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, 0, 0, 0, 0);
        addc(overview, 0, 1, 1, 1, 0.35, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, 0, 0, 0, 12);

        GPanel fields = new GPanel();
        fields.setBackground(gold);
        fields.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        questionText.setEditable(false);
        configureText(questionText, gold, navy);
        questionText.setFont(questionText.getFont().deriveFont(Font.BOLD));
        configureText(msg, gold, navy);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setLabelFor(userId);
        fields.addc(emailLabel, 0, 0, 2, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, 0, 0, 5, 0);
        fields.addc(userId, 0, 1, 1, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, 0, 0, 0, 8);
        fields.addc(lookupBut, 1, 1, 1, 1, 0, 0, GridBagConstraints.EAST,
                GridBagConstraints.NONE, 0, 0, 0, 0);
        JLabel questionLabel = new JLabel("Your security question:");
        questionLabel.setLabelFor(questionText);
        fields.addc(questionLabel, 0, 2, 2, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, 22, 0, 6, 0);
        fields.addc(questionText, 0, 3, 2, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, 0, 0, 0, 0);
        JLabel answerLabel = new JLabel("Answer:");
        answerLabel.setLabelFor(secAnswer);
        fields.addc(answerLabel, 0, 4, 2, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, 16, 0, 5, 0);
        fields.addc(secAnswer, 0, 5, 2, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, 0, 0, 12, 0);
        fields.addc(msg, 0, 6, 2, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, 0, 0, 12, 0);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(backBut);
        buttons.add(verifyBut);
        fields.addc(buttons, 0, 7, 2, 1, 1, 0, GridBagConstraints.EAST,
                GridBagConstraints.HORIZONTAL, 0, 0, 0, 0);
        addc(fields, 1, 1, 1, 1, 0.65, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, 0, 0, 0, 0);
        addc(Box.createVerticalGlue(), 0, 2, 2, 1, 1, 1, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, 0, 0, 0, 0);
    }

    private static void configureText(JTextArea text, Color background, Color foreground) {
        text.setEditable(false);
        text.setFocusable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(UIManager.getFont("Label.font"));
        text.setBackground(background);
        text.setForeground(foreground);
    }

    private static DocumentListener listener(Runnable change) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { change.run(); }
            @Override public void removeUpdate(DocumentEvent e) { change.run(); }
            @Override public void changedUpdate(DocumentEvent e) { change.run(); }
        };
    }

    private void invalidateQuestion() {
        generation++;
        resolvedUserId = null;
        busy = false;
        questionText.setText("");
        secAnswer.setText("");
        updateButtons();
    }

    private void updateButtons() {
        boolean validEmail = VALID_EMAIL_ADDRESS_REGEX.matcher(userId.getText().trim()).matches();
        lookupBut.setEnabled(!busy && validEmail);
        boolean ready = resolvedUserId != null && resolvedUserId.equals(userId.getText().trim());
        secAnswer.setEnabled(ready && !busy);
        verifyBut.setEnabled(ready && !busy && secAnswer.getDocument().getLength() > 0);
        if (isShowing() && getRootPane() != null) {
            getRootPane().setDefaultButton(ready ? verifyBut : lookupBut);
        }
    }

    public void lookupQuestion() {
        if (!lookupBut.isEnabled()) return;
        invalidateQuestion();
        String email = userId.getText().trim();
        ClientRequest request = new ClientRequest(ServerRequestType.GET_SECURITY_QUESTION);
        request.setData(gson.toJson(new RecoveryRequest(email, null)));
        submit(request, reply -> {
            if ("SecurityQuestion".equals(reply.getStatus())) {
                SecurityQuestionPrompt question = gson.fromJson(reply.getData(), SecurityQuestionPrompt.class);
                if (question == null || question.getText() == null || question.getText().isBlank()) {
                    throw new IllegalArgumentException("Missing question");
                }
                resolvedUserId = email;
                questionText.setText(question.getText());
                msg.setText("Enter the answer you provided when you created your account.");
                updateButtons();
                secAnswer.requestFocusInWindow();
            } else {
                msg.setText("Unable to load the question. Check your email or try again later.");
            }
        });
    }

    public void verifyAnswer() {
        if (!verifyBut.isEnabled()) return;
        String email = resolvedUserId;
        ClientRequest request = new ClientRequest(ServerRequestType.VERIFY_USER);
        request.setData(gson.toJson(new RecoveryRequest(email,
                encryptSHA256(new String(secAnswer.getPassword())))));
        submit(request, reply -> {
            if ("Verified".equals(reply.getStatus())) {
                String token = gson.fromJson(reply.getData(), String.class);
                if (token == null || token.isBlank()) throw new IllegalArgumentException("Missing token");
                clearFields();
                verified.accept(email, token);
            } else {
                secAnswer.setText("");
                msg.setText("InvalidAnswer".equals(reply.getStatus())
                        ? "The answer did not match. Please try again."
                        : "Unable to verify your answer. Please try again later.");
            }
        });
    }

    private void submit(ClientRequest request, java.util.function.Consumer<TutorReply> receive) {
        long requestGeneration = ++generation;
        busy = true;
        updateButtons();
        msg.setText("Please wait...");
        SwingWorker<TutorReply, Void> worker = new SwingWorker<>() {
            @Override protected TutorReply doInBackground() { return requests.apply(request); }
            @Override protected void done() {
                if (generation != requestGeneration) return;
                busy = false;
                try {
                    TutorReply reply = get();
                    if (reply == null || reply.getStatus() == null) {
                        throw new IllegalArgumentException("Missing reply");
                    }
                    receive.accept(reply);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    msg.setText("Request interrupted. Please try again.");
                } catch (ExecutionException | RuntimeException ex) {
                    // Exception text can contain a response payload; do not log it.
                    msg.setText("Unable to complete the request. Please try again.");
                } finally {
                    updateButtons();
                }
            }
        };
        // SwingWorker's shared pool can linger under Maven exec:java. This
        // request-owned thread exits as soon as the network operation finishes.
        Thread thread = new Thread(worker, "merc-recovery-request");
        thread.setDaemon(true);
        thread.start();
    }

    public Account getModel() {
        Account account = new Account(userId.getText().trim());
        account.setSecurityAnswer(encryptSHA256(new String(secAnswer.getPassword())));
        return account;
    }

    public void setModel(Account account) {
        clearFields();
        userId.setText(account.getUserId());
    }

    public JTextField getUserIdComp() { return userId; }
    JPasswordField getAnswerComp() { return secAnswer; }
    JTextArea getQuestionComp() { return questionText; }
    JButton getLookupButton() { return lookupBut; }
    JButton getVerifyButton() { return verifyBut; }
    JButton getBackButton() { return backBut; }
    JTextArea getMessageComp() { return msg; }
    public void updateFocus() {
        updateButtons();
        userId.requestFocusInWindow();
    }

    public void clearFields() {
        invalidateQuestion();
        userId.setText("");
        msg.setText("Enter your account email to retrieve your security question.");
    }

    /** Preserves the SHA-256/UTF-8 answer format used by existing accounts. */
    public static String encryptSHA256(String answer) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(answer.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
