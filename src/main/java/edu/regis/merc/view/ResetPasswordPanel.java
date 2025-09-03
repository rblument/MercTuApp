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
import edu.regis.merc.view.act.ResetPasswordAction;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * Forgot Password screen that allows the user to reset their password.
 *
 * @author mandyroskelley
 */
public class ResetPasswordPanel extends GPanel{
    
    private String securityToken;
    
    public void setSecurityToken(String token) {
        this.securityToken = token;
    }

    public String getSecurityToken() {
        return securityToken;
    }
    
    /**
     * Events of interest occurring in this class are logged to this logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordPanel.class.getName());

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
    protected JTextField userId;
    protected JPasswordField pass1;
    protected JPasswordField pass2;
    
    public String userEmail = "";

    protected JLabel strength;
    protected JLabel msg;

    protected JButton resetBut;
    protected JButton backBut;

    public ResetPasswordPanel(String email) {
        super();

        model = new Account();
        userEmail = email;
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
        pass1.setText("");
        pass2.setText("");
    }

    /**
     * Update our model with the current values displayed in this view
     */
    private void updateModel() {
        model.setUserId(userEmail);
        model.setPassword(encryptSHA256(new String(pass1.getPassword())));
    }

    /**
     * Update this view with the current values in our model (except the
     * passwords).
     */
    private void updateDisplay() {
        model.setUserId(userEmail);
        userId.setText(model.getUserId());
        pass1.setText("");
        pass2.setText("");
    }
    
    private void initComponents() {
        
        LoginDocumentListener docListener = new LoginDocumentListener();
        
        userId = new JTextField(userEmail, 10);
        userId.setEditable(false);
        userId.getDocument().addDocumentListener(docListener);

        pass1 = new JPasswordField(20);
        pass1.getDocument().addDocumentListener(docListener);

        pass2 = new JPasswordField(20);
        pass2.getDocument().addDocumentListener(docListener);

        resetBut = new JButton(ResetPasswordAction.instance());

        resetBut.setEnabled(false);
        MainFrame.instance().getRootPane().setDefaultButton(resetBut);

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

        addc(createReset(), 1, 1, 1, 1, 0.0, 0.0,
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

    private GPanel createReset() {
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

        label = new JLabel("Create a New Password:");
        label.setLabelFor(pass1);

        panel.addc(label, 0, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                15, 5, 5, 5);

        label = new JLabel("(do not use your existing university password!)");
        label.setFont(new Font("Dialog", Font.PLAIN, 10));
        label.setForeground(new Color(75,66,66));

        panel.addc(label, 1, 4, 2, 1, 0.0, 0.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                0, 5, 5, 5);

        panel.addc(pass1, 0, 5, 2, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                0, 5, 5, 5);

        panel.addc(strength, 0, 6, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                0, 5, 5, 5);

        label = new JLabel("(try 6 characters, mixed case, and special chars)");
        label.setFont(new Font("Dialog", Font.PLAIN, 10));
        label.setForeground(new Color(75,66,66));
        panel.addc(label, 1, 6, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                0, 5, 5, 5);

        label = new JLabel("Confirm your password:");
        label.setLabelFor(pass1);

        panel.addc(label, 0, 7, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                5, 5, 5, 5);

        panel.addc(pass2, 0, 8, 2, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                0, 5, 5, 5);

        panel.addc(resetBut, 1, 9, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                10, 5, 5, 5);

        msg = new JLabel("");
        msg.setLabelFor(resetBut);
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

    private void checkStrength() {
        int points = 0;
        char[] text = pass1.getPassword();

        if (text.length >= 6) {
            points++;
        }

        boolean isSpecial = false;
        boolean isUpper = false;
        boolean isLower = false;
        for (int i = 0; i < text.length; i++) {
            char ch = text[i];

            if (Character.isLowerCase(ch)) {
                isLower = true;
            } else if (Character.isUpperCase(ch)) {
                isUpper = true;
            } else if (Character.isDigit(ch)) {
                isSpecial = true;
            } else if (!Character.isLetterOrDigit(ch)) {
                isSpecial = true;
            }
        }
        if (isSpecial) {
            points++;
        }
        if (isLower && isUpper) {
            points++;
        }
        switch (points) {
            case 0:
                strength.setText("(Strength: Very poor)");
                strength.setForeground(new Color(173,7,1));
                break;
            case 1:
                strength.setText("(Strength: Poor)");
                strength.setForeground(new Color(173,7,1));
                break;
            case 2:
                strength.setText("(Strength: Moderate)");
                strength.setForeground(new Color(255,255,0));
                break;
            default:
                strength.setText("(Strength: Good)");
                strength.setForeground(new Color(0,128,0));
                break;
        }
    }

    private boolean samePasswords() {
        char[] text1 = pass1.getPassword();
        char[] text2 = pass2.getPassword();

        if (text1.length == text2.length) {
            if (text1.length == 0) {
                return false;
            } else {
                for (int i = 0; i < text1.length; i++) {
                    if (text1[i] != text2[i]) {
                        return false;
                    }
                }
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * If the userId or password fields are empty, disable the OK 'Login'
     * button.
     */
    private void enableButtons(Document e) {
        boolean isPass1Valid = pass1.getDocument().getLength() == 0;
        if (isPass1Valid) {
            pass1.setBorder(BorderFactory.createLineBorder(new Color(173,7,1)));
        } else {
            pass1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
        boolean isSamePass = samePasswords();
        if (isSamePass) {
            pass2.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        } else {
            pass2.setBorder(BorderFactory.createLineBorder(new Color(173,7,1)));
        }
        if (isSamePass) {
            resetBut.setEnabled(true);
            msg.setText("");

        } else {
            resetBut.setEnabled(false);
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
            Component comp = SplashFrame.instance().getFocusOwner();
            if (comp == pass1) {
                checkStrength();
            }
            enableButtons(e.getDocument());
        }

        /**
         * As text was removed from the userId or password field, check whether
         * we need to enable or disable the LoginDialog's buttons.
         */
        @Override
        public void removeUpdate(DocumentEvent e) {
            Component comp = MainFrame.instance().getFocusOwner();
            if (comp == pass1) {
                checkStrength();
            }
            enableButtons(e.getDocument());
        }

        /**
         * As text was changed in the userId or password field, check whether we
         * need to enable or disable the LoginDialog's buttons.
         */
        @Override
        public void changedUpdate(DocumentEvent e) {
            Component comp = MainFrame.instance().getFocusOwner();
            if (comp == pass1) {
                checkStrength();
            }
            enableButtons(e.getDocument());
        }
    }

    /**
     * Encrypt the given password using MD5
     */
    private String encryptMD5(String password) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            byte[] data = password.getBytes();

            m.update(data, 0, data.length);

            BigInteger i = new BigInteger(1, m.digest());

            return String.format("%1$032X", i).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.severe(e.toString());
        }
        return "";
    }

    /**
     * Encrypt the given password using SHA-256
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
