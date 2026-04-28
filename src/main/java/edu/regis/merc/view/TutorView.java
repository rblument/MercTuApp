package edu.regis.merc.view;

import java.awt.*;
import javax.swing.*;

public class TutorView extends GPanel {
    private JTextArea instructionsArea;

    public TutorView() {
        setBorder(BorderFactory.createTitledBorder("Merc Tutor"));
        initializeComponents();
        layoutComponents();
    }

    private void initializeComponents() {
        instructionsArea = new JTextArea();
        instructionsArea.setEditable(false);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setOpaque(false);
        instructionsArea.setFont(new Font("Monospace", Font.PLAIN, 14));
    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(instructionsArea);
        scrollPane.setBorder(null);

        addc(scrollPane, 0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                5, 5, 5, 5);

    }

    public void setInstructions(String text) {
        instructionsArea.setText(text);
        instructionsArea.setCaretPosition(0);
    }

}
