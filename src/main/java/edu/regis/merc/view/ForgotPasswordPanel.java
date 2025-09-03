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

import edu.regis.merc.model.Account;
import edu.regis.merc.view.act.CheckSecurityQuestAction;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Forgot Password screen that allows the user to reset their password.
 *
 * @author mandyroskelley
 */
public class ForgotPasswordPanel extends GPanel{
    
    /**
     * A regex pattern used to validate user email ids (e.g. "rick@regis.edu").
     */
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX
            = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    /**
     * The account being created and displayed in this panel.
     */
    private Account model;

    /**
     * The editable fields appearing in this dialog.
     */
    protected HintTextField userId;
    protected JPasswordField secAnswer;
    protected JComboBox securityQuestions;

    protected JLabel strength;
    protected JLabel msg;

    protected JButton verifyBut;
    protected JButton backBut;

    public ForgotPasswordPanel() {
        super();

        model = new Account();

        initComponents();
        layoutPanel();

        enableButtons(userId.getDocument());
    }

    /**
     * Update and return the model with view's information.
     *
     * @return
     */
    public Account getModel() {
        
        updateModel();
        System.out.println("ForgotPasswordPanel: " + model.getUserId());
	return model;
    }

    /**
     * Display the given model in the view, but the MD5 encrypted password is
     * not displayed.
     *
     * @param model
     */
    public void setModel(Account model) {
        this.model = model;

        updateDisplay();
    }

    public JTextField getUserIdComp() {
        return userId;
    }

    public void updateFocus() {
        userId.requestFocusInWindow();
    }
    
    /**
     * Set all of the text fields in this view to the empty string.
     */
    public void clearFields() {
        userId.setText("");
        secAnswer.setText("");
        securityQuestions.setSelectedIndex(0);
    }

    /**
     * Update our model with the current values displayed in this view
     */
    private void updateModel() {
        model.setUserId(userId.getText());
        model.setSecurityAnswer(encryptSHA256(new String(secAnswer.getPassword())));
        model.setSecurityQuestion(securityQuestions.getSelectedIndex());
    }

    /**
     * Update this view with the current values in our model (except the
     * passwords).
     */
    private void updateDisplay() {
        userId.setText(model.getUserId());
        secAnswer.setText("");
        securityQuestions.setSelectedIndex(0);
    }
    
    private void initComponents() {
        LoginDocumentListener docListener = new LoginDocumentListener();

        userId = new HintTextField("userId@university.edu", 10);
        userId.setIsEmailAddr(true);
        userId.getDocument().addDocumentListener(docListener);

        
        String s1[] = {"What city were you born in?", "What is your mother's maiden name?"};
        securityQuestions = new JComboBox(s1);
        
        secAnswer = new JPasswordField(20);
        secAnswer.getDocument().addDocumentListener(docListener);

        verifyBut = new JButton(CheckSecurityQuestAction.instance());

        verifyBut.setEnabled(false);
        MainFrame.instance().getRootPane().setDefaultButton(verifyBut);

        backBut = new JButton("Back");
        backBut.setEnabled(true);
        
        strength = new JLabel("(Strength: very poor)");
        strength.setForeground(new Color(173,7,1));
        strength.setFont(new Font("Dialog", Font.PLAIN, 10));
    }

    private void layoutPanel() {
        setBackground(new Color(0, 43, 73));

        setPreferredSize(new Dimension(300, 400));

        addc(createHeader(), 0, 0, 2, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                5, 5, 5, 5);

        addc(createOverview(), 0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                5, 5, 5, 5);

        addc(createVerify(), 1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                10, 5, 5, 5);

        JLabel copyright = new JLabel("(C) 2019-2024 Johanna and Richard Blumenthal. All Rights Reserved");
        copyright.setFont(new Font("Dialog", Font.PLAIN, 10));
        copyright.setForeground(new Color(241,196,0));
        addc(copyright, 0, 2, 2, 1, 1.0, 1.0,
                GridBagConstraints.NORTH, GridBagConstraints.CENTER,
                5, 5, 5, 5);

        setSize(490, 400);
    }

    private GPanel createHeader() {
        GPanel panel = new GPanel();
        panel.setBackground(new Color(241,196,0));

        JLabel ccis = new JLabel("Regis University Department of Computer and Cyber Sciences");
        ccis.setFont(new Font("Dialog", Font.PLAIN, 20));
        ccis.setForeground(new Color(0, 43, 73));

        panel.addc(ccis, 0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                5, 5, 5, 5);

        return panel;
    }

    private GPanel createOverview() {
        GPanel panel = new GPanel();
        panel.setBackground(new Color(241,196,0));

        panel.setSize(300, 400);
        panel.setPreferredSize(new Dimension(300, 400));

        JLabel logo = new JLabel("ShaTu");
        logo.setFont(new Font("Dialog", Font.PLAIN, 20));
        logo.setForeground(new Color(0, 43, 73));

        panel.addc(logo, 0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                5, 5, 5, 5);

        JLabel name = new JLabel("A See_1, Do_1, Teach_1 Intelligent Tutoring System.");
        name.setFont(new Font("Dialog", Font.PLAIN, 14));
        panel.addc(name, 0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                0, 5, 5, 5);

        JTextArea descr = new JTextArea();
        descr.setEditable(false);
        descr.setLineWrap(true);
        descr.setWrapStyleWord(true);
        descr.setBackground(new Color(241,196,0));
        descr.setFont(new Font("Dialog", Font.PLAIN, 12));
	descr.append("ShaTu provides individualized tutoring practice focused ");
	descr.append("on understanding the SHA-256 digest algorithm and the");
        descr.append("underlying computer science concepts upon which it is ");
        descr.append("based.\n\n");
        descr.append("Please sign in or use 'New User' to create a student account.");

        descr.append("\n\n");
        descr.append("Use your university email address as your user id, but ");
        descr.append("DO NOT use your existing university password. Instead,");
        descr.append("use a different password for the ShaTu tutor.");
        panel.addc(descr, 0, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                15, 5, 5, 5);

        JLabel loginMsg = new JLabel("To use the tutor, you must sign in.");
        panel.addc(loginMsg, 0, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                5, 5, 5, 5);

        panel.addc(new JLabel(" "), 0, 4, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                5, 5, 5, 5);

        return panel;
    }

    private GPanel createVerify() {
        GPanel panel = new GPanel();
        panel.setBackground(new Color(241,196,0));

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 5));

       
        JLabel label = new JLabel("User Id:");
        label.setLabelFor(userId);

        panel.addc(label, 0, 2, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                5, 5, 5, 5);

        panel.addc(userId, 0, 3, 2, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                0, 5, 5, 5);

        label = new JLabel("Choose Security Question:");
        label.setLabelFor(securityQuestions);

        panel.addc(label, 0, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                15, 5, 5, 5);
        
        panel.addc(securityQuestions, 0, 5, 2, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                0, 5, 5, 5);
        
        label = new JLabel("Answer:");
        label.setLabelFor(secAnswer);

        panel.addc(label, 0, 6, 2, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                15, 5, 5, 5);
        
        panel.addc(secAnswer, 0, 7, 2, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                0, 5, 5, 5);

        panel.addc(verifyBut, 1, 9, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                10, 5, 5, 5);

        msg = new JLabel("");
        msg.setLabelFor(verifyBut);
        msg.setFont(new Font("Dialog", Font.PLAIN, 10));
        msg.setForeground(new Color(173,7,1));

        panel.addc(msg, 0, 12, 2, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                5, 5, 5, 5);
        
        panel.addc(backBut, 0, 9, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                10, 5, 5, 5);

        msg = new JLabel("");
        msg.setLabelFor(backBut);
        msg.setFont(new Font("Dialog", Font.PLAIN, 10));
        msg.setForeground(new Color(173,7,1));

        panel.addc(msg, 0, 12, 2, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                5, 5, 5, 5);

        return panel;
    }

    

    /**
     * If the userId or security answer fields are empty, disable the OK 'Login'
     * button.
     */
    private void enableButtons(Document e)  {

        boolean isValidUserId = !userId.isDefaultValue();
        if (isValidUserId) {
            Document userIdDoc = userId.getDocument();
            try {
                String email = userIdDoc.getText(0, userIdDoc.getLength());

                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
                isValidUserId = matcher.find();

                if (isValidUserId) {
                    userId.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                } else {
                    userId.setBorder(BorderFactory.createLineBorder(new Color(173,7,1)));
                }
            } catch (BadLocationException er) {
                // Cannot happen since 0 to length
            }
        } else {
            userId.setBorder(BorderFactory.createLineBorder(new Color(173,7,1)));
        }
        
        boolean isValidAnswer = false;
            Document secAnswerDoc = secAnswer.getDocument();
            
            String answer = "";
        try {
            answer = secAnswerDoc.getText(0, secAnswerDoc.getLength());
            if (answer.isEmpty()) {
                isValidAnswer = false;
            }
            else {
                isValidAnswer = true;
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(ForgotPasswordPanel.class.getName()).log(Level.SEVERE, null, ex);
        }  
        if (isValidUserId && isValidAnswer) {
            verifyBut.setEnabled(true);
            msg.setText("");

        } else {
            verifyBut.setEnabled(false);
            msg.setText("(* Please fix problems highlighted in red.)");
        }
    }

    /**
     * Listens to changes made to the LoginDialog's userId and password fields
     * in order to appropriate enable the buttons in the dialog.
     */
    public class LoginDocumentListener implements DocumentListener {
        /**
         * As text was insert into the userId or password field, check whether
         * we need to enable or disable the LoginDialog's buttons.
         */
        @Override
        public void insertUpdate(DocumentEvent e) {
            
            enableButtons(e.getDocument());
        }

        /**
         * As text was removed from the userId or answer field, check whether
         * we need to enable or disable the LoginDialog's buttons.
         */
        @Override
        public void removeUpdate(DocumentEvent e) {
            
            enableButtons(e.getDocument());
        }

        /**
         * As text was changed in the userId or answer field, check whether we
         * need to enable or disable the LoginDialog's buttons.
         */
        @Override
        public void changedUpdate(DocumentEvent e) {
            
            enableButtons(e.getDocument());
        }
    }

    /**
     * Encrypt the given security answer using SHA-256
     *
     * @param base
     * @return
     */
    public static String encryptSHA256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);

                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
