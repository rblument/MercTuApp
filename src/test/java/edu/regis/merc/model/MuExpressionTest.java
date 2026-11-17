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
    }

    // Term Builders

    private MuExpression buildOne() {
        MuExpression zero = new MuExpression(0);
        MuExpression zeroFunc = new MuExpression("Z", List.of(zero));

        return new MuExpression("S", List.of(zeroFunc));
    }
    
}
