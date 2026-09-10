package edu.regis.merc.view;

import edu.regis.merc.model.MuFunction;
import edu.regis.merc.model.MuExpression;
import edu.regis.merc.model.LeftHandSide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This view demosntrates human-centered design by transforming a static
 * mathematical string into a non-mouse interactive interface.
 * 
 * Design pattern: uses a recursive composite pattern to walk the MuExpression
 * tree. Note that 'StaticLabels' (parentheses/equals) are intentionalyl
 * non-interactive to guide the student toward functional computing
 * requirements.
 */
public class MuRecView extends JPanel {

    private MuFunction model;
    private JPanel equationPanel;
    private InteractiveLabel selectedComponent = null;

    public MuRecView() {
        setBorder(BorderFactory.createTitledBorder("Mu Recursive"));
        setLayout(new BorderLayout());

        equationPanel = new JPanel();
        equationPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        add(new JScrollPane(equationPanel), BorderLayout.CENTER);
    }

    /**
     * Updates the view with a new MuFunction and redraws the screen.
     */
    public void setModel(MuFunction model) {
        this.model = model;
        renderEquation();
    }

    /**
     * Returns the ID of the currently selected component, or -1 if nothing is
     * selected
     */
    public int getSelectedComponentId() {
        if (selectedComponent != null) {
            return selectedComponent.getComponentId();
        }
        return -1;
    }

    /**
     * Clears the current panel and rebuilds the interactive equation piece by piece
     */
    private void renderEquation() {
        equationPanel.removeAll();

        if (model == null) {
            equationPanel.add(new JLabel("No Mu-Recursive funciton loaded."));
        } else {

            LeftHandSide lhs = model.getLhs();
            if (lhs != null) {
                equationPanel.add(createClickableLabel(lhs.getName(), 100));
                equationPanel.add(createStaticLabel("("));

                String params = lhs.getParameters().toString().replace("[", "").replace("]", "");
                equationPanel.add(createClickableLabel(params, 101));
                equationPanel.add(createStaticLabel(")"));
            }

            equationPanel.add(createStaticLabel(" = "));
            renderExpression(model.getRhs());
        }

        equationPanel.revalidate();
        equationPanel.repaint();
    }

    /**
     * a recursive method that reads the MuExpression tree and turns it into UI
     * components
     * 
     * @param expr
     */
    private void renderExpression(MuExpression expr) {
        if (expr == null) {
            return;
        }

        switch (expr.getKind()) {
            case NUMBER:
            case VARIABLE:
                equationPanel.add(createClickableLabel(expr.toString(), expr.hashCode()));
                break;

            case OPERATION:
                equationPanel.add(createStaticLabel("("));

                Object left = expr.getLeftOperand();
                if (left instanceof MuExpression) {
                    renderExpression((MuExpression) left);
                } else if (left != null) {
                    equationPanel.add(createClickableLabel(left.toString(), left.hashCode()));
                }

                equationPanel.add(createStaticLabel(" " + expr.getOpKind().toString() + " "));

                Object right = expr.getRightOperand();
                if (right instanceof MuExpression) {
                    renderExpression((MuExpression) right);
                } else if (right != null) {
                    equationPanel.add(createClickableLabel(right.toString(), right.hashCode()));
                }

                equationPanel.add(createStaticLabel(")"));
                break;
        }
    }

    private InteractiveLabel createClickableLabel(String text, int componentId) {
        InteractiveLabel label = new InteractiveLabel(text, componentId);

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // deselect the previously clicked component
                if (selectedComponent != null) {
                    selectedComponent.setSelected(false);
                }

                // select the new one
                selectedComponent = label;
                selectedComponent.setSelected(true);

                System.out.println("Selected Mu-Recursive Component ID: " + componentId + " (Text: " + text + ")");
            }
        });

        return label;
    }

    private JLabel createStaticLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Monospaced", Font.BOLD, 24));
        return label;
    }
}
