package edu.regis.merc.model;

import com.google.gson.Gson;
import edu.regis.merc.view.act.SignInAction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class MuExpressionTest {

    private final Gson gson = SignInAction.createSessionGson();

    @Test
    void applicationRendersInTheCustomersNotation() {

        MuExpression one = buildOne();
        MuExpression two = new MuExpression("S", List.of(one));

        MuExpression projection = new MuExpression("P", List.of(new MuExpression("x1"),
             new MuExpression("x2"), new MuExpression("x3")));

        assertEquals("S(Z(0))", one.toString());
        assertEquals("S(S(Z(0)))", two.toString());
        assertEquals("P(x1, x2, x3)", projection.toString());
    }

    @Test
    void nestedExpressionSurvivesRoundTrip() {
        MuExpression op = new MuExpression(OpKind.ADD, new MuExpression("x"), new MuExpression("y"));

        MuExpression opResult = gson.fromJson(gson.toJson(op), MuExpression.class);

        MuExpression one = buildOne();

        MuExpression oneResult = gson.fromJson(gson.toJson(one), MuExpression.class);

        assertEquals("(x + y)", opResult.toString());
        assertEquals(Kind.APPLICATION, oneResult.getKind());
        assertEquals("S", oneResult.getFunctionName());
        assertEquals(1, oneResult.getArguments().size());
    }

    @Test
    void gettersReturnEachKindsFields() {
        MuExpression one = buildOne();

        assertEquals(7, new MuExpression(7).getNumberValue());
        assertEquals("x", new MuExpression("x").getVariableName());
        assertEquals("S", one.getFunctionName());
        assertEquals("Z(0)", one.getArguments().get(0).toString());
    }

    @Test
    void argumentsAreReadOnly() {
        MuExpression one = buildOne();

        assertThrows(UnsupportedOperationException.class,
                () -> one.getArguments().add(new MuExpression(1)));
    }

    @Test
    void nonApplicationHasNoArguments() {
        assertTrue(new MuExpression("x").getArguments().isEmpty());
    }

    // The JSON written while the operands were typed Object has the same shape,
    // so typing them MuExpression still reads it, and reads the operands as
    // MuExpression instead of LinkedTreeMap.
    @Test
    void jsonWrittenWithObjectOperandsStillReads() {
        String objectOperandJson = "{\"kind\":\"OPERATION\",\"opKind\":\"ADD\","
                + "\"leftOperand\":{\"kind\":\"VARIABLE\",\"variableName\":\"x\",\"numberValue\":0},"
                + "\"rightOperand\":{\"kind\":\"VARIABLE\",\"variableName\":\"y\",\"numberValue\":0},"
                + "\"numberValue\":0}";

        MuExpression expr = gson.fromJson(objectOperandJson, MuExpression.class);

        assertEquals(Kind.OPERATION, expr.getKind());
        assertEquals(Kind.VARIABLE, expr.getLeftOperand().getKind());
        assertEquals("(x + y)", expr.toString());
    }

    // Term Builders

    private MuExpression buildOne() {
        MuExpression zero = new MuExpression(0);
        MuExpression zeroFunc = new MuExpression("Z", List.of(zero));

        return new MuExpression("S", List.of(zeroFunc));
    }
}
