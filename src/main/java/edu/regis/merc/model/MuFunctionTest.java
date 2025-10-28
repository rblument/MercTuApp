// Kristin Ingram


package edu.regis.merc.model;

// test for if everything is linked correctly 

public class MuFunctionTest {
    public static void main(String[] args) {
        // ---------------------------------------------------
        // 1. Simple Example: add(x, y) = (x + y)
        // ---------------------------------------------------
        LeftHandSide lhs1 = new LeftHandSide("add");
        lhs1.addParameter("x");
        lhs1.addParameter("y");

        MuExpression rhs1 = new MuExpression(OpKind.ADD, "x", "y");
        MuFunction f1 = new MuFunction(1, lhs1, rhs1);

        System.out.println("Example 1: " + f1);

        // ---------------------------------------------------
        // 2. Multiply Example: multiplyByFive(x) = (x * 5)
        // ---------------------------------------------------
        LeftHandSide lhs2 = new LeftHandSide("multiplyByFive");
        lhs2.addParameter("x");

        MuExpression eLeft2 = new MuExpression("x");
        MuExpression eRight2 = new MuExpression(5);
        MuExpression rhs2 = new MuExpression(OpKind.MUL, eLeft2, eRight2);
        MuFunction f2 = new MuFunction(2, lhs2, rhs2);

        System.out.println("Example 2: " + f2);

        // ---------------------------------------------------
        // 3. Nested Example: addAndDouble(x, y) = ((x + y) * 2)
        // ---------------------------------------------------
        LeftHandSide lhs3 = new LeftHandSide("addAndDouble");
        lhs3.addParameter("x");
        lhs3.addParameter("y");

        MuExpression innerAdd = new MuExpression(OpKind.ADD, "x", "y");
        MuExpression rhs3 = new MuExpression(OpKind.MUL, innerAdd, new MuExpression(2));
        MuFunction f3 = new MuFunction(3, lhs3, rhs3);

        System.out.println("Example 3: " + f3);

        // ---------------------------------------------------
        // 4. Subtract Example: subtractTen(x) = (x - 10)
        // ---------------------------------------------------
        LeftHandSide lhs4 = new LeftHandSide("subtractTen");
        lhs4.addParameter("x");

        MuExpression rhs4 = new MuExpression(OpKind.SUB, "x", new MuExpression(10));
        MuFunction f4 = new MuFunction(4, lhs4, rhs4);

        System.out.println("Example 4: " + f4);

        // ---------------------------------------------------
        // 5. Divide Example: half(x) = (x / 2)
        // ---------------------------------------------------
        LeftHandSide lhs5 = new LeftHandSide("half");
        lhs5.addParameter("x");

        MuExpression rhs5 = new MuExpression(OpKind.DIV, "x", new MuExpression(2));
        MuFunction f5 = new MuFunction(5, lhs5, rhs5);

        System.out.println("Example 5: " + f5);
    }
}
