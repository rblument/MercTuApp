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

    /**
     * The id given to the first selectable component of an equation. Chosen so
     * that a mu component id read in a log or in seed data is recognisable as
     * one, rather than being confused with a database key.
     */
    private static final int FIRST_COMPONENT_ID = 2000;

    private MuFunction model;
    private JPanel equationPanel;
    private InteractiveLabel selectedComponent = null;

    /**
     * The id the next selectable component will be given. Reset at the start of
     * every render so ids depend only on the equation, not on how many times
     * it has been drawn.
     */
    private int nextComponentId = FIRST_COMPONENT_ID;

    /**
     * Run when the student selects a component here. See setOnSelection.
     */
    private Runnable onSelection = null;

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
     * Register a callback run whenever the student selects a component here.
     *
     * The three representation panels hold their selections independently, so
     * whichever view owns them uses this to clear the others and keep exactly
     * one component selected across the whole problem. The callback is not run
     * by {@link #clearSelection()}, so wiring two views to each other does not
     * recurse.
     *
     * @param onSelection a Runnable, or null to remove the callback
     */
    public void setOnSelection(Runnable onSelection) {
        this.onSelection = onSelection;
    }

    /**
     * Discard the current selection, without running the selection callback.
     */
    public void clearSelection() {
        if (selectedComponent != null) {
            selectedComponent.setSelected(false);
            selectedComponent = null;
        }
    }

    /**
     * Clears the current panel and rebuilds the interactive equation piece by piece
     *
     * Component ids are assigned in render order, starting at
     * {@link #FIRST_COMPONENT_ID}: the function name, then the parameter list,
     * then each clickable node of the right-hand side as the tree is walked.
     * A mu function always renders the same way, so the same component keeps
     * the same id across runs, which is what lets seed data name one as a
     * step's expected answer.
     *
     * These ids are only unique within this representation. The lambda
     * calculus view numbers its components from LC_EXPRESSION primary keys and
     * the two ranges overlap, so an answer from this panel is only meaningful
     * when it travels with
     * {@link edu.regis.merc.model.RepresentationKind#MU_RECURSIVE}.
     */
    private void renderEquation() {
        equationPanel.removeAll();
        clearSelection();
        nextComponentId = FIRST_COMPONENT_ID;

        if (model == null) {
            equationPanel.add(new JLabel("No Mu-Recursive funciton loaded."));
        } else {

            LeftHandSide lhs = model.getLhs();
            if (lhs != null) {
                equationPanel.add(createClickableLabel(lhs.getName()));
                equationPanel.add(createStaticLabel("("));

                String params = lhs.getParameters().toString().replace("[", "").replace("]", "");
                equationPanel.add(createClickableLabel(params));
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
                equationPanel.add(createClickableLabel(expr.toString()));
                break;

            case OPERATION:
                equationPanel.add(createStaticLabel("("));

                Object left = expr.getLeftOperand();
                if (left instanceof MuExpression) {
                    renderExpression((MuExpression) left);
                } else if (left != null) {
                    equationPanel.add(createClickableLabel(left.toString()));
                }

                equationPanel.add(createStaticLabel(" " + expr.getOpKind().toString() + " "));

                Object right = expr.getRightOperand();
                if (right instanceof MuExpression) {
                    renderExpression((MuExpression) right);
                } else if (right != null) {
                    equationPanel.add(createClickableLabel(right.toString()));
                }

                equationPanel.add(createStaticLabel(")"));
                break;
        }
    }

    /**
     * Create a selectable label and give it the next id in render order.
     *
     * @param text the label's text
     * @return an InteractiveLabel carrying its component id
     */
    private InteractiveLabel createClickableLabel(String text) {
        int componentId = nextComponentId++;

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

                if (onSelection != null) {
                    onSelection.run();
                }
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
