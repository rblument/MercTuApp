package edu.regis.merc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.regis.merc.model.LeftHandSide;
import edu.regis.merc.model.MuExpression;
import edu.regis.merc.model.MuFunction;
import edu.regis.merc.model.OpKind;
import org.junit.jupiter.api.Test;

public class MuTest {

    @Test
    public void zeroFunction() {
        LeftHandSide lhsZ = new LeftHandSide("Z");
        lhsZ.addParameter("x");
        MuExpression rhsZ = new MuExpression(0);
        MuFunction fZ = new MuFunction(1, lhsZ, rhsZ);

        assertEquals("Z([x]) = 0", fZ.toString(), "The zero function should be zero.");
    }

    @Test
    public void successorFunction() {
        LeftHandSide lhsS = new LeftHandSide("S");
        lhsS.addParameter("x");

        MuExpression rhsS = new MuExpression(OpKind.ADD, new MuExpression("x"), new MuExpression(1));
        MuFunction fS = new MuFunction(2, lhsS, rhsS);

        assertEquals("S([x]) = (x + 1)", fS.toString(), "The successor function should add 1");
    }

    @Test
    public void nestedAdd() {
        LeftHandSide lhsAD = new LeftHandSide("addDouble");
        lhsAD.addParameter("x");
        lhsAD.addParameter("y");

        MuExpression add = new MuExpression(OpKind.ADD, new MuExpression("x"), new MuExpression("y"));
        MuExpression rhsAD = new MuExpression(OpKind.MUL, add, new MuExpression(2));
        MuFunction fAD = new MuFunction(3, lhsAD, rhsAD);

        assertEquals("addDouble([x, y]) = ((x + y) * 2)", fAD.toString(), "A nested function should be correctly represented");
    }
}
        
