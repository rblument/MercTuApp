package edu.regis.merc.model;

import edu.regis.merc.err.MuParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class MuParserTest {

    // Covers the seeded Z function, whose right hand side is "0".
    @Test
    void digitsBecomeANumberNode() throws MuParseException {
        MuExpression zero = new MuParser().parse("0");
        MuExpression many = new MuParser().parse("42");

        assertEquals(Kind.NUMBER, zero.getKind());
        assertEquals("0", zero.toString());
        assertEquals("42", many.toString());
    }

    // Covers the seeded P function, whose right hand side is "x2".
    @Test
    void variableNameKeepsItsDigits() throws MuParseException {
        MuExpression expr = new MuParser().parse("x2");

        assertEquals(Kind.VARIABLE, expr.getKind());
        assertEquals("x2", expr.toString());
    }

    // Covers the seeded add function, whose right hand side is "(x + y)".
    @Test
    void infixOperationParsesIntoAnOperationNode() throws MuParseException {
        MuExpression expr = new MuParser().parse("(x + y)");

        assertEquals(Kind.OPERATION, expr.getKind());
        assertEquals(OpKind.ADD, expr.getOpKind());
        assertEquals("(x + y)", expr.toString());
    }

    @Test
    void applicationParsesNestedCalls() throws MuParseException {
        MuExpression expr = new MuParser().parse("S(Z(0))");

        assertEquals(Kind.APPLICATION, expr.getKind());
        assertEquals("S(Z(0))", expr.toString());
    }

    @Test
    void applicationParsesItsArgumentList() throws MuParseException {
        MuExpression expr = new MuParser().parse("P(x1, x2, x3)");

        assertEquals(Kind.APPLICATION, expr.getKind());
        assertEquals("P(x1, x2, x3)", expr.toString());
    }

    @Test
    void operationNestsInsideAnApplication() throws MuParseException {
        MuExpression expr = new MuParser().parse("S((x + y))");

        assertEquals("S((x + y))", expr.toString());
    }

    @Test
    void surroundingWhitespaceIsIgnored() throws MuParseException {
        assertEquals("0", new MuParser().parse(" 0 ").toString());
        assertEquals("(x + y)", new MuParser().parse("  (x  +  y)  ").toString());
        assertEquals("P(x1, x2, x3)", new MuParser().parse("P( x1 , x2 , x3 )").toString());
    }

    @Test
    void trailingInputIsRejected() {
        assertThrows(MuParseException.class, () -> new MuParser().parse("x y"));
    }

    @Test
    void unterminatedArgumentListIsRejected() {
        assertThrows(MuParseException.class, () -> new MuParser().parse("S(Z(0)"));
    }

    @Test
    void emptyArgumentListIsRejected() {
        assertThrows(MuParseException.class, () -> new MuParser().parse("f()"));
    }

    @Test
    void operationNeedsAnOperator() {
        assertThrows(MuParseException.class, () -> new MuParser().parse("(x)"));
    }

    @Test
    void unknownCharacterIsRejected() {
        assertThrows(MuParseException.class, () -> new MuParser().parse("+"));
    }

    @Test
    void nameCannotStartWithADigit() {
        assertThrows(MuParseException.class, () -> new MuParser().parse("2x"));
    }

    @Test
    void nullAndEmptyAreRejected() {
        assertThrows(MuParseException.class, () -> new MuParser().parse(null));
        assertThrows(MuParseException.class, () -> new MuParser().parse(""));
    }
}
