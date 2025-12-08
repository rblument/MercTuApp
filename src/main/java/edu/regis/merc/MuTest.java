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
        MuExpression rhsZ = new MuExpression(0);
        MuFunction fZ = new MuFunction(1, lhsZ, rhsZ);

        System.out.println("Test 1: Zero Function");
        System.out.println("Expected: Z([x]) = 0");
        System.out.println("Actual:   " + fZ);
        System.out.println("Status:   " + (fZ.toString().equals("Z([x]) = 0") ? "PASS" : "FAIL"));
        System.out.println();

        

        // Test 2: Successor function S(x) = x + 1
        LeftHandSide lhsS = new LeftHandSide("S");
        lhsS.addParameter("x");

        MuExpression rhsS = new MuExpression(OpKind.ADD, new MuExpression("x"), new MuExpression(1));
        MuFunction fS = new MuFunction(2, lhsS, rhsS);

        System.out.println("Test 2: Successor Function");
        System.out.println("Expected: S([x]) = (x + 1)");
        System.out.println("Actual:   " + fS);
        System.out.println("Status:   " + (fS.toString().equals("S([x]) = (x + 1)") ? "PASS" : "FAIL"));
        System.out.println();



        // Test 3: Nested add: addDouble(x, y) = (x + y) * 2
        LeftHandSide lhsAD = new LeftHandSide("addDouble");
        lhsAD.addParameter("x");
        lhsAD.addParameter("y");

        MuExpression add = new MuExpression(OpKind.ADD, new MuExpression("x"), new MuExpression("y"));
        MuExpression rhsAD = new MuExpression(OpKind.MUL, add, new MuExpression(2));
        MuFunction fAD = new MuFunction(3, lhsAD, rhsAD);

        System.out.println("Test 3: AddDouble Function");
        System.out.println("Expected: addDouble([x, y]) = ((x + y) * 2)");
        System.out.println("Actual:   " + fAD);
        System.out.println("Status:   " + (fAD.toString().equals("addDouble([x, y]) = ((x + y) * 2)") ? "PASS" : "FAIL"));
        System.out.println();
    }
}
        
