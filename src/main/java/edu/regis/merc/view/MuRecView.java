package edu.regis.merc.view;

import edu.regis.merc.model.MuFunction;
import edu.regis.merc.model.MuExpression;
import edu.regis.merc.model.LeftHandSide;
import edu.regis.merc.model.RepresentationKind;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

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
     * Notified when the student selects a component here. See
     * setSelectionListener.
     */
    private ComponentSelectionListener selectionListener = null;

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
     * Register the listener notified whenever the student selects a component
     * here.
     *
     * The three representation panels hold their selections independently, so
     * whichever view owns them uses this to record what was chosen and release
     * the other panels. The listener is not notified by
     * {@link #clearSelection()}, so panels that release each other do not
     * recurse.
     *
     * @param selectionListener the listener, or null to remove it
     */
    public void setSelectionListener(ComponentSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
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
     * {@link #FIRST_COMPONENT_ID}: the function name, then each parameter,
     * then each clickable node of the right-hand side as the tree is walked.
     * For P(x1, x2, x3) = x2 that is P 2000, x1 2001, x2 2002, x3 2003, and
     * the right-hand x2 2004. Adding or removing a clickable node shifts the
     * id of every node drawn after it, so check seed data that names a mu
     * component id before changing how an equation is drawn.
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

                // Each parameter is its own component, so a student can be
                // asked to pick out the i-th one. A function with no
                // parameters is drawn as a bare name.
                List<String> params = lhs.getParameters();
                if (params != null && !params.isEmpty()) {
                    renderArgumentList(params, param -> equationPanel.add(createClickableLabel(param)));
                }
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
                renderExpression(expr.getLeftOperand());
                equationPanel.add(createStaticLabel(" " + expr.getOpKind() + " "));
                renderExpression(expr.getRightOperand());
                equationPanel.add(createStaticLabel(")"));
                break;

            case APPLICATION:
                // S(Z(0)) is drawn as S, Z and 0, each selectable, so a
                // student can pick out the function being applied as well
                // as each argument.
                equationPanel.add(createClickableLabel(expr.getFunctionName()));
                renderArgumentList(expr.getArguments(), this::renderExpression);
                break;

            default:
                equationPanel.add(createClickableLabel(expr.toString()));
                break;
        }
    }

    /**
     * Draw a parenthesized, comma separated list. The parentheses and commas
     * are not selectable; each item is drawn by the given renderer.
     *
     * @param items the parameters or arguments, in order
     * @param renderItem draws one item
     */
    private <T> void renderArgumentList(List<T> items, Consumer<T> renderItem) {
        equationPanel.add(createStaticLabel("("));
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                equationPanel.add(createStaticLabel(","));
            }
            renderItem.accept(items.get(i));
        }
        equationPanel.add(createStaticLabel(")"));
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

                if (selectionListener != null) {
                    selectionListener.componentSelected(
                            RepresentationKind.MU_RECURSIVE, componentId);
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
