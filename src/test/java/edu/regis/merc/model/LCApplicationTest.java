package edu.regis.merc.model;

import com.google.gson.Gson;
import edu.regis.merc.view.act.SignInAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the lambda calculus application node.
 *
 * LCApplication used to declare its own function and arg fields, shadowing the
 * ones it inherits from LCExpression. Gson rejects duplicate field names in a
 * class hierarchy, so serializing one threw and the tutor could not send a
 * session containing an application to the client. The function was also typed
 * LCAbstraction, which cannot hold the variable or nested application that
 * Church numerals and the successor function put in function position.
 */
class LCApplicationTest {

    private final Gson gson = SignInAction.createSessionGson();

    @Test
    @DisplayName("an application can be serialized")
    void applicationSerializes() {
        // Shadowed fields made this throw IllegalArgumentException: "declares
        // multiple JSON fields named function".
        assertDoesNotThrow(() -> gson.toJson(churchOne()));
    }

    @Test
    @DisplayName("Church numeral one survives a round trip")
    void churchNumeralOneRoundTrips() {
        LCAbstraction one = churchOne();

        assertEquals("\\f. \\x. f x", one.toString());

        LCExpression back = gson.fromJson(gson.toJson(one), LCExpression.class);

        assertEquals(one.toString(), back.toString());
        assertInstanceOf(LCAbstraction.class, back);
    }

    @Test
    @DisplayName("successor keeps a variable and an application in function position")
    void successorKeepsFunctionPositionTypes() {
        LCExpression back = gson.fromJson(gson.toJson(successor()), LCExpression.class);

        // \n. \f. \x. f (n f x) -- walk down to the body of the innermost
        // abstraction.
        LCAbstraction absN = assertInstanceOf(LCAbstraction.class, back);
        assertEquals("n", absN.getParameters().get(0).getName());

        LCAbstraction absF = assertInstanceOf(LCAbstraction.class, absN.getBody().get(0));
        LCAbstraction absX = assertInstanceOf(LCAbstraction.class, absF.getBody().get(0));

        // f (n f x): a variable sits in function position, which the old
        // LCAbstraction-typed field could not hold.
        LCApplication outer = assertInstanceOf(LCApplication.class, absX.getBody().get(0));
        LCVariable outerFn = assertInstanceOf(LCVariable.class, outer.getFunction());
        assertEquals("f", outerFn.getName());

        // n f x associates left, so (n f) is an application in function
        // position -- the other shape the old type rejected.
        LCApplication nfx = assertInstanceOf(LCApplication.class, outer.getArg());
        LCApplication nf = assertInstanceOf(LCApplication.class, nfx.getFunction());

        assertEquals("n", assertInstanceOf(LCVariable.class, nf.getFunction()).getName());
        assertEquals("f", assertInstanceOf(LCVariable.class, nf.getArg()).getName());
        assertEquals("x", assertInstanceOf(LCVariable.class, nfx.getArg()).getName());
    }

    // ---------------------------------------------------------------
    // term builders
    // ---------------------------------------------------------------

    /**
     * Church numeral one, \f. \x. f x.
     */
    private static LCAbstraction churchOne() {
        LCVariable f = new LCVariable("f");
        LCVariable x = new LCVariable("x");

        return abs(f, abs(x, app(f, x)));
    }

    /**
     * The successor function, \n. \f. \x. f (n f x).
     */
    private static LCAbstraction successor() {
        LCVariable n = new LCVariable("n");
        LCVariable f = new LCVariable("f");
        LCVariable x = new LCVariable("x");

        LCApplication body = app(f, app(app(n, f), x));

        return abs(n, abs(f, abs(x, body)));
    }

    private static LCApplication app(LCExpression function, LCExpression arg) {
        LCApplication application = new LCApplication();
        application.setFunction(function);
        application.setArg(arg);
        return application;
    }

    private static LCAbstraction abs(LCVariable parameter, LCExpression body) {
        LCAbstraction abstraction = new LCAbstraction();
        abstraction.addParameter(parameter);
        abstraction.addBodyPart(body);
        return abstraction;
    }
}
