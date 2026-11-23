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
}
