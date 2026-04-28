package edu.regis.merc.view;

import edu.regis.merc.model.LCAbstraction;
import edu.regis.merc.model.LCApplication;
import edu.regis.merc.model.LCExpression;
import edu.regis.merc.model.LCVariable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LambdaCalcView extends GPanel {

    private LCExpression model;
    private JPanel expressionContainer;
    private JLabel selectedLabel = null; // used to track the piece currently selected

    public LambdaCalcView() {

        setBorder(BorderFactory.createTitledBorder("Lambda Calculus"));
        initializeComponents();
        layoutComponents();
    }

    public LCExpression getModel() {
        return model;
    }

    public void setModel(LCExpression model) {
        this.model = model;
        selectedLabel = null; // reset selection when a new problem loads
        expressionContainer.removeAll(); // Clear old equation

        if (model != null) {
            buildInteractiveEquation(model, expressionContainer);
        } else {
            expressionContainer.add(new JLabel("No Equation Data"));
        }

        // Force a refresh with the new components
        expressionContainer.revalidate();
        expressionContainer.repaint();
        this.revalidate();
        this.repaint();
    }

    private void initializeComponents() {
        expressionContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        expressionContainer.setBackground(Color.WHITE);

    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());

        // Put the GridBagLayout and addc method back!
        addc(expressionContainer, 0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                0, 0, 0, 0);
    }

    private void buildInteractiveEquation(LCExpression expr, JPanel parent) {

        System.out.println("Trying to draw class: " + expr.getClass().getSimpleName());

        if (expr instanceof LCVariable) {
            System.out.println("Matched: variable");
            LCVariable var = (LCVariable) expr;
            parent.add(createClickableLabel(var.getName()));

        } else if (expr instanceof LCAbstraction) {
            System.out.println("Matched: Abstraction");
            LCAbstraction abs = (LCAbstraction) expr;
            // Lambda symbol todo: make enums or global variables for symbol IDs
            parent.add(createClickableLabel("\u03BB"));

            // Add the parameters
            for (LCVariable param : abs.getParameters()) {
                buildInteractiveEquation(param, parent);
            }

            parent.add(createClickableLabel("."));

            // add the body parts
            for (LCExpression bodyPart : abs.getBody()) {
                buildInteractiveEquation(bodyPart, parent);
            }

        } else if (expr instanceof LCApplication) {
            System.out.println("Matched: Application");
            LCApplication app = (LCApplication) expr;
            parent.add(createClickableLabel("("));
            buildInteractiveEquation(app.getFunction(), parent);
            parent.add(createClickableLabel(" ")); // space between func and arg
            buildInteractiveEquation(app.getArg(), parent);
            parent.add(createClickableLabel(")"));
        }
    }

    private JLabel createClickableLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Monospaced", Font.BOLD, 18));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR)); // make it look like it's clickable
        label.setOpaque(true);
        label.setBackground(Color.WHITE);

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {

                if (label != selectedLabel) {
                    label.setBackground(Color.YELLOW); // highlight on hover
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (label != selectedLabel) {
                    label.setBackground(Color.WHITE); // removes highlight

                } else {
                    label.setBackground(new Color(173, 216, 230));
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // resets the previous selection's color
                if (selectedLabel != null) {
                    selectedLabel.setBackground(Color.WHITE);
                }

                // set the new selection
                selectedLabel = label;
                label.setBackground(new Color(173, 216, 230));
                System.out.println("Student clicked on: " + text);
                // todo: tell the tutor the student clicked on this component
            }
        });

        return label;
    }

    public JLabel getSelectedLabel() {
        return selectedLabel;
    }
}
