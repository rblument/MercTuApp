package edu.regis.merc.model;

import edu.regis.merc.err.MuParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class MuEvaluatorTest {

    // Covers the seeded add function, whose right hand side is "(x + y)".
    @Test
    void parsedOperationEvaluates() throws MuParseException {
        MuExpression expr = new MuParser().parse("(x + y)");

        assertEquals(5, new MuEvaluator().eval(expr, Map.of("x", 2, "y", 3)));
    }

    @Test
    void applicationIsNotEvaluatedYet() throws MuParseException {
        MuExpression expr = new MuParser().parse("S(Z(0))");

        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> new MuEvaluator().eval(expr, Map.of()));

        assertTrue(ex.getMessage().contains("S(Z(0))"));
    }

    // eval reads the fields, so a change to how an expression prints can't
    // change its value.
    @Test
    void evaluationDoesNotDependOnToString() {
        MuExpression number = new MuExpression(7) {
            @Override
            public String toString() {
                return "seven";
            }
        };
        MuExpression variable = new MuExpression("x") {
            @Override
            public String toString() {
                return "not x";
            }
        };

        MuEvaluator evaluator = new MuEvaluator();

        assertEquals(7, evaluator.eval(number, Map.of()));
        assertEquals(2, evaluator.eval(variable, Map.of("x", 2)));
    }

    @Test
    void unboundVariableIsReportedByName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new MuEvaluator().eval(new MuExpression("y"), Map.of("x", 1)));

        assertEquals("Unbound variable: y", ex.getMessage());
    }

    @Test
    void missingOperandIsReported() {
        MuExpression expr = new MuExpression(OpKind.ADD, new MuExpression("x"), null);

        assertThrows(IllegalArgumentException.class,
                () -> new MuEvaluator().eval(expr, Map.of("x", 1)));
    }
}
