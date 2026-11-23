package edu.regis.merc.view;

import edu.regis.merc.model.MuFunction;
import edu.regis.merc.model.MuParser;
import edu.regis.merc.model.RepresentationKind;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks which parts of a mu function MuRecView makes selectable, in what
 * order, and with which ids. Seed data names a step's expected answer by
 * these ids, so a change in drawing order is a change in grading.
 */
class MuRecViewTest {

    @Test
    void eachParameterIsItsOwnComponent() throws Exception {
        MuRecView view = render("P(x1, x2, x3)", "x2");
        onEdt(() -> {
            assertEquals("P ( x1 , x2 , x3 ) = x2", text(view));
            assertEquals(List.of("P", "x1", "x2", "x3", "x2"), clickableText(view));
            assertEquals(List.of(2000, 2001, 2002, 2003, 2004), ids(view));
        });
    }

    @Test
    void applicationShowsTheFunctionAndEachArgument() throws Exception {
        MuRecView view = render("one", "S(Z(0))");
        onEdt(() -> {
            assertEquals("one = S ( Z ( 0 ) )", text(view));
            assertEquals(List.of("one", "S", "Z", "0"), clickableText(view));
            assertEquals(List.of(2000, 2001, 2002, 2003), ids(view));
        });
    }

    @Test
    void applicationArgumentsAreSeparatedAndNested() throws Exception {
        MuRecView view = render("h(x, y)", "f(x, g(y))");
        onEdt(() -> {
            assertEquals("h ( x , y ) = f ( x , g ( y ) )", text(view));
            assertEquals(List.of("h", "x", "y", "f", "x", "g", "y"), clickableText(view));
        });
    }

    @Test
    void operationShowsBothOperands() throws Exception {
        MuRecView view = render("add(x, y)", "(x + y)");
        onEdt(() -> {
            assertEquals("add ( x , y ) = ( x + y )", text(view));
            assertEquals(List.of("add", "x", "y", "x", "y"), clickableText(view));
        });
    }

    @Test
    void clickingAParameterReportsItsId() throws Exception {
        MuRecView view = render("P(x1, x2, x3)", "x2");
        AtomicReference<String> reported = new AtomicReference<>();
        onEdt(() -> {
            view.setSelectionListener((representation, id) -> reported.set(representation + " " + id));
            click(clickable(view).get(2));
            assertEquals(RepresentationKind.MU_RECURSIVE + " 2002", reported.get());
            assertEquals(2002, view.getSelectedComponentId());
        });
    }

    @Test
    void redrawingKeepsTheSameIds() throws Exception {
        MuRecView view = render("P(x1, x2, x3)", "x2");
        AtomicReference<List<Integer>> first = new AtomicReference<>();
        onEdt(() -> first.set(ids(view)));
        MuFunction same = function("P(x1, x2, x3)", "x2");
        onEdt(() -> {
            view.setModel(same);
            assertEquals(first.get(), ids(view));
        });
    }

    private static MuFunction function(String lhs, String rhs) throws Exception {
        MuParser parser = new MuParser();
        return new MuFunction(1, parser.parseLeftHandSide(lhs), parser.parse(rhs));
    }

    private static MuRecView render(String lhs, String rhs) throws Exception {
        MuFunction function = function(lhs, rhs);
        AtomicReference<MuRecView> result = new AtomicReference<>();
        onEdt(() -> {
            MuRecView view = new MuRecView();
            view.setModel(function);
            result.set(view);
        });
        return result.get();
    }

    /** Every label in the equation, selectable or not, in drawing order. */
    private static List<JLabel> labels(Container root) {
        List<JLabel> found = new ArrayList<>();
        for (Component child : root.getComponents()) {
            if (child instanceof JLabel label) {
                found.add(label);
            } else if (child instanceof Container inner) {
                found.addAll(labels(inner));
            }
        }
        return found;
    }

    /** The equation as drawn, with single spaces between labels. */
    private static String text(MuRecView view) {
        List<String> parts = new ArrayList<>();
        for (JLabel label : labels(view)) {
            parts.add(label.getText());
        }
        return String.join(" ", parts).trim().replaceAll("\\s+", " ");
    }

    private static List<InteractiveLabel> clickable(MuRecView view) {
        List<InteractiveLabel> found = new ArrayList<>();
        for (JLabel label : labels(view)) {
            if (label instanceof InteractiveLabel interactive) {
                found.add(interactive);
            }
        }
        return found;
    }

    private static List<String> clickableText(MuRecView view) {
        return clickable(view).stream().map(JLabel::getText).toList();
    }

    private static List<Integer> ids(MuRecView view) {
        return clickable(view).stream().map(InteractiveLabel::getComponentId).toList();
    }

    private static void click(InteractiveLabel label) {
        MouseEvent event = new MouseEvent(label, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, 0, 0, 1, false,
                MouseEvent.BUTTON1);
        for (MouseListener listener : label.getMouseListeners()) {
            listener.mouseClicked(event);
        }
    }

    /** Run on the event thread, reporting a failed assertion as itself. */
    private static void onEdt(Runnable action) throws Exception {
        try {
            SwingUtilities.invokeAndWait(action);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof Error error) {
                throw error;
            }
            throw ex;
        }
    }
}
