// Kristin Ingram
package edu.regis.merc.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MuFunctionTest {

    @Test
    public void addFunction() {
        LeftHandSide lhs1 = new LeftHandSide("add");
        lhs1.addParameter("x");
        lhs1.addParameter("y");

        MuExpression rhs1 = new MuExpression(OpKind.ADD, "x", "y");
        MuFunction f1 = new MuFunction(1, lhs1, rhs1);

        assertEquals("add([x, y]) = (x + y)", f1.toString());
    }

    @Test
    public void multiplyByFive() {
        LeftHandSide lhs2 = new LeftHandSide("multiplyByFive");
        lhs2.addParameter("x");

        MuExpression eLeft2 = new MuExpression("x");
        MuExpression eRight2 = new MuExpression(5);
        MuExpression rhs2 = new MuExpression(OpKind.MUL, eLeft2, eRight2);
        MuFunction f2 = new MuFunction(2, lhs2, rhs2);

        assertEquals("multiplyByFive([x]) = (x * 5)", f2.toString());
    }

    @Test
    public void addAndDouble() {
        LeftHandSide lhs3 = new LeftHandSide("addAndDouble");
        lhs3.addParameter("x");
        lhs3.addParameter("y");

        MuExpression innerAdd = new MuExpression(OpKind.ADD, "x", "y");
        MuExpression rhs3 = new MuExpression(OpKind.MUL, innerAdd, new MuExpression(2));
        MuFunction f3 = new MuFunction(3, lhs3, rhs3);

        assertEquals("addAndDouble([x, y]) = ((x + y) * 2)", f3.toString());
    }

    @Test
    public void subtract() {
        LeftHandSide lhs4 = new LeftHandSide("subtractTen");
        lhs4.addParameter("x");

        MuExpression rhs4 = new MuExpression(OpKind.SUB, "x", new MuExpression(10));
        MuFunction f4 = new MuFunction(4, lhs4, rhs4);

        assertEquals("subtractTen([x]) = (x - 10)", f4.toString());
    }

    @Test
    public void divide() {
        LeftHandSide lhs5 = new LeftHandSide("half");
        lhs5.addParameter("x");

        MuExpression rhs5 = new MuExpression(OpKind.DIV, "x", new MuExpression(2));
        MuFunction f5 = new MuFunction(5, lhs5, rhs5);

        assertEquals("half([x]) = (x / 2)", f5.toString());
    }
}
