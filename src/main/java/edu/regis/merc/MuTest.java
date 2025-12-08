package edu.regis.merc;

import edu.regis.merc.model.LeftHandSide;
import edu.regis.merc.model.MuExpression;
import edu.regis.merc.model.MuFunction;
import edu.regis.merc.model.OpKind;

public class MuTest {

    public static void main(String[] args) {

        // Test 1: Zero function Z(x) = 0

        LeftHandSide lhsZ = new LeftHandSide("Z");
        lhsZ.addParameter("x");

        MuExpression rhsZ = new MuExpression(0);   // constant 0

        MuFunction z = new MuFunction(100, lhsZ, rhsZ);

        System.out.println("Zero Function: " + z);


        // Test 2: Successor function S(x) = x + 1

        LeftHandSide lhsS = new LeftHandSide("S");
        lhsS.addParameter("x");

        MuExpression rhsS = new MuExpression(OpKind.ADD, "x", new MuExpression(1));

        MuFunction s = new MuFunction(101, lhsS, rhsS);

        System.out.println("Successor Function: " + s);


        // Test 3: Nested add: addDouble(x, y) = (x + y) * 2

        LeftHandSide lhsAddDouble = new LeftHandSide("addDouble");
        lhsAddDouble.addParameter("x");
        lhsAddDouble.addParameter("y");

        MuExpression innerAdd = new MuExpression(OpKind.ADD, "x", "y");
        MuExpression rhsAddDouble = new MuExpression(OpKind.MUL, innerAdd, new MuExpression(2));

        MuFunction addDouble = new MuFunction(102, lhsAddDouble, rhsAddDouble);

        System.out.println("AddDouble Function: " + addDouble);
    }
}
