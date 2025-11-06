/*
 * MERC^T: Multiple External Representations of Computation Tutor
 * 
 *  (C) Richard Blumenthal, All rights reserved
 * 
 *  Unauthorized use, duplication or distribution without the authors'
 *  permission is strictly prohibited.
 * 
 *  Unless required by applicable law or agreed to in writing, this
 *  software is distributed on an "AS IS" basis without warranties
 *  or conditions of any kind, either expressed or implied.
 */
package edu.regis.merc.test;

import edu.regis.merc.model.LCExpression;
import edu.regis.merc.model.LCParser;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * A unit test for the LCParser.
 * 
 * @author rickb
 */
public class TestLCParser {
    /**
     * Convenience reference to the Lambda Calculus parser.
     */
    private static LCParser parser;

    /**
     * Initialize the GSON_INS and TUTOR_INS instances.
     */
    @BeforeAll
    public static void setUpClass() {
        //System.out.println("BeforeAll setUpClass");
        parser = new LCParser();
    }

    /**
     * Unit test for the Lambda Calculus Parser.
     */
    @Test
    public void testParser() {
        assertNotNull(parser);

        
        // String churchOne = "(\\x. f x)"; // ALso tested with no parens \\x. x
       // LCExpression t5Expr = Assertions.assertDoesNotThrow(() -> parser.parse(churchOne, false, false),
        //        "Parse test1 abstration should not throw an exception");
        
        //System.out.println("one: " + t5Expr.toString());
        

        String testStr1 = "(\\x. x)"; // ALso tested with no parens \\x. x

        LCExpression t1Expr = Assertions.assertDoesNotThrow(() -> parser.parse(testStr1, false, false),
                "Parse test1 abstration should not throw an exception");

        
        assertEquals("(\\x. x)", t1Expr.toString());

        // ****************************
        String t2Expr = "(\\x. x) x"; // Also tested with (\\x. x) (x)

        LCExpression expr2 = Assertions.assertDoesNotThrow(() -> parser.parse(t2Expr, false, false),
                "Parse test 2 application should not throw an exception");

        assertEquals("(\\x. x) x", t2Expr.toString());

        // ********************************
        String churchZero = "(\\f. (\\x. x))";

        LCExpression expr3 = Assertions.assertDoesNotThrow(() -> parser.parse(churchZero, false, false),
                "Parse church zero should not throw an exception");

        assertEquals("(\\f. (\\x. x))", expr3.toString());

        // ***********************
        String zeroFunction = "(\\n. (\\f. (\\x. x)))";

        LCExpression expr4 = Assertions.assertDoesNotThrow(() -> parser.parse(zeroFunction, false, false),
                "Parse zero function should not throw an exception");

        assertEquals("(\\n. (\\f. (\\x. x)))", expr4.toString());

        // ***********************
        String zeroFunctionApp = "(\\n. (\\f. (\\x. x))) (\\f. (\\x. f x))";

        LCExpression expr5 = Assertions.assertDoesNotThrow(() -> parser.parse(zeroFunctionApp, false, false),
                "Parse zero function should not throw an exception");
        
        assertEquals("(\\n. (\\f. (\\x. x))) (\\f. (\\x. f x))", expr5.toString());

    }
}
