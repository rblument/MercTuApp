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
package edu.regis.merc.model;

import edu.regis.merc.err.EtxException;
import edu.regis.merc.err.LCParseException;

/**
 * A utility for parsing a Lambda Calculus string into its constituent objects.
 *
 * Note: Lambdas are represented ala Haskell with a '\', which in a Java String
 * must be escaped, hence, "(\\x. x)"
 *
 * Tested on: "\\x. x" Simple Abstractions "(\\x. x)" "(\\x . x)" "(\\x. f x)"
 * "(\\f. (\\x. x))" Church Numeral Zero "(\\f. (\\x. f x))" Church Numeral One
 * "(\\x. x) x Simple Applications "(\\x. x) (x)" "(\\n. (\\f. (\\x. x))) Zero
 * Function "(\\n. (\\f. (\\x. x))) (\\f. (\\x. f x))" Zero Function applied to
 * 1
 *
 * @author rickb
 */
public class LCParser {
    /**
     * The singleton instance of this class.
     */
    private final static LCParser SINGLETON;

    /**
     * ASCII End of Text (indicates the end of the input text to parse).
     */
    private final static char ETX = '\3';

    // Invoked when this class is loaded
    static {
        SINGLETON = new LCParser();
    }

    /**
     * Return the singleton instance of this frame.
     *
     * @return the LCParser singleton
     */
    public static LCParser instance() {
        return SINGLETON;
    }

    /**
     * The Lambda Calculus expression currently being parsed.
     */
    private String expression;

    /**
     * If true, an applied calculus is allowed in the expression syntax.
     */
    private boolean isApplied;

    /**
     * If true, currying of parameters is allowed in the expression syntax for
     * abstractions.
     */
    private boolean isCurried;

    /**
     * The current character being parsed in the input expression.
     */
    private int pos;

    /**
     * Parse the given Lambda Calculus expression.
     *
     * @param expression the Lambda Calculus Expression to parse, such as "(\\x.
     * x) x"
     * @param isApplied true, if variables can be more than one character
     * @param isCurried true, if abstractions can have multiple parameters
     * separated by commas
     * @return an LCExpression
     * @throws LCParseException see the message and if the wrapped cause is not
     * null, it is an EtxException (indicating the end of the expression was
     * reached before parsing finished).
     */
    public LCExpression parse(String expression, boolean isApplied, boolean isCurried) throws LCParseException {
        this.expression = expression;
        this.isApplied = isApplied;
        this.isCurried = isCurried;

        if (expression == null) {
            throw new LCParseException("Expression cannot be null");

        } else if (expression.length() == 0) {
            throw new LCParseException("Expression cannot be an empty string");

        } else {
            pos = -1;

            return parseLCExpression();
        }
    }

    /**
     * Given the current position (pos) in the input expression, we're expecting
     * another Lambda Calculus expression.
     *
     * @return an LCExpression
     * @throws LCParseException see the note on the parse() method exception.
     */
    private LCExpression parseLCExpression() throws LCParseException {
        char ch;

        try {
            ch = nextChar();
        } catch (EtxException e) {
            throw new LCParseException("ETX reached before reading anything", e);
        }

        switch (ch) {
            case '\\':  // a Lambda ala Haskell beginning an Abstraction
                LCAbstraction abstraction = parseAbstraction();

                return abstraction;

            case '(':
                LCExpression expr = parseLCExpression();

                try {
                    ch = nextChar();
                } catch (EtxException e) {
                    throw new LCParseException("Etx encountered when expecting a ')'", e);
                }

                matchChar(ch, ')');

                expr.setIsParentheses(true);

                if (expr instanceof LCAbstraction) {
                    return parsePossibleApplication((LCAbstraction) expr);
                } else {
                    return expr;
                }

            default:
                if (Character.isAlphabetic(ch)) {
                    return parseVariable();
                } else {
                    throw new LCParseException("Expecting a variable");
                }
        }
    }

    /**
     * As a Lambda '\\' was read and is at the current position, begin parsing 
     * the remainder of the Abstraction returning it as an LCAbstraction.
     * 
     * @return an LCAbstraction
     * @throws LCParseException see the note on the parse() method exception.
     */
    private LCAbstraction parseAbstraction() throws LCParseException {
        LCAbstraction abstraction = new LCAbstraction();

        parseAbstractionParameters(abstraction);

        try {
            char ch = nextChar();

            matchChar(ch, '.');

            parseAbstractionBody(abstraction);

            return abstraction;

        } catch (EtxException e) {
            throw new LCParseException("Etx occurred when expecting '.' in abstraciton body.");
        }

    }

    /**
     * As a Lambda '\\' was read and is at the current position, begin parsing 
     * the parameters in the Abstraction adding them to the given LCAbstraction.
     * 
     * There must be at least one parameter to read, otherwise a parse exception.
     * 
     * @param abstraction the LCAbstraction that is being parsed.
     * @throws LCParseException see the note on the parse() method exception.
     */
    public void parseAbstractionParameters(LCAbstraction abstraction) throws LCParseException {
        boolean moreParameters = true;

        try {
            // We're currently parsing the '\\', so move past it to the first
            // character of the first parameter. 
            char ch = nextChar();

            do {
                parseParameter(abstraction);

                ch = nextChar();

                if (isCurried && (ch == ',')) {
                    abstraction.setIsCurried(true);
                    nextChar();

                } else {
                    pos--; // Peeked one char too far, push it back.
                    moreParameters = false;
                }

            } while (moreParameters); // if true get the next curried parameter

        } catch (EtxException e) {
            throw new LCParseException("Etx encountered while looking for abstraction parameter", e);
        }
    }

    /**
     * Assumed that we're sitting at the first letter of a variable token,
     * parse it and add it to the given LCAbstraction
     * 
     * @param abstraction the LCAbstraction whose parameter is being parsed.
     * @throws LCParseException see the note on the parse() method exception.
     */
    private void parseParameter(LCAbstraction abstraction) throws LCParseException {
        abstraction.addParameter(parseVariable());
    }

    /**
     * As a '.' was read and is at the current position, begin parsing the
     * Abstraction's body adding it to the given LCAbstraction.
     * 
     * @param abstraction
     * @throws LCParseException see the note on the parse() method exception.
     */
    private void parseAbstractionBody(LCAbstraction abstraction) throws LCParseException {
        boolean readingBodyParts = true;

        char ch;

        try {
            ch = nextChar(); // There needs to be something
            pos--; // Peeked too far, push it back.

        } catch (EtxException e) {
            throw new LCParseException("Etx encountered when expecting abstraction body", e);
        }

        do {
            LCExpression bodyPart = parseLCExpression();
            
            abstraction.addBodyPart(bodyPart);

            try {
                ch = nextChar();

                if (ch == ')') // A body part cannot begin with a ')'
                    readingBodyParts = false;

                pos--; // We peeked one char too far, push it back.

            } catch (EtxException e) {
                readingBodyParts = false;
            }

        } while (readingBodyParts);
    }

    /**
     * As we've just parsed an Abstraction, it's possible that it's followed by
     * arguments, in which case they are parsed and an LCApplication is returned,
     * otherwise, return the given LCAbstraction.
     * 
     * @param abstraction a recently parsed LCAbstraction.
     * @return either the given LCAbstraction or an LCApplication with the
     *         given LCAbstraction as its function.
     * @throws LCParseException see the note on the parse() method exception.
     */
    private LCExpression parsePossibleApplication(LCAbstraction abstraction) throws LCParseException {
        char ch;

        try {
            ch = nextChar(); // Looking for anything vs. ETX

            if (ch == ')') { // Cannot be the start of an application
                pos--;
                return abstraction; // return the abstraction   
            }

            // Peeked one too far, push it back, should be the start
            // of the args in an application
            pos--;

        } catch (EtxException e) { // this is okay, it's not an application
            return abstraction;           // return the abstraction
        }

        LCExpression arg = parseLCExpression();

        LCApplication application = new LCApplication();
        application.setFunction(abstraction);
        application.setArg(arg);

        return application;
    }

    /**
     * Beginning at the current position (pos), parse a Variable, it's expected
     * that the current position corresponds to the first character in the
     * variable.
     * 
     * @return an LCVariable
     * @throws LCParseException see the note on the parse() method exception.
     */
    private LCVariable parseVariable() throws LCParseException {
        return new LCVariable(parseToken());
    }

    /**
     * If the given received character doesn't match the expected character,
     * throw a parsing exception
     * 
     * @param received the char that was just read in the expression input.
     * @param expected the char that was expected.
     * @throws LCParseException see the note on the parse() method exception.
     */
    private void matchChar(char received, char expected) throws LCParseException {
        if (received != expected) {
            String msg = "Expecting '" + expected + "but received " + received + " at position " + pos;
            throw new LCParseException(msg);
        }
    }

    /**
     * Return the alphabetic token beginning at the current parse position (pos).
     *
     * It is assumed pos is at the first alphabetic character of a token.
     *
     * @return
     * @throws LCParseException
     */
    private String parseToken() throws LCParseException {
        StringBuilder buff = new StringBuilder();

        char ch = expression.charAt(pos);

        if (Character.isAlphabetic(ch)) {
            buff.append(ch);
        } else {
            throw new LCParseException("Initial char of token '" + ch + "' at pos " + pos + " should be alphabet in parseToken");
        }

        // For an applied calculus look for the remainder of a variable name.
        if (isApplied) {
            boolean isAppliedToken = true;
                    
            do {
                try {
                    ch = nextChar();

                    if (Character.isAlphabetic(ch)) {
                        buff.append(ch);
                    } else {
                        pos--; // we read one character too far, push it back.
                        isAppliedToken = false;
                    }
                } catch (EtxException e) {  // This is okay token ended at ETX
                    isAppliedToken = false;
                }

            } while (isAppliedToken);
        }

        return buff.toString();
    }

    /**
     * Increment to the next position in the expression and return the next
     * non white space char.
     * 
     * @return a non white space char
     * @throws EtxException the end of the expression was reached, which isn't
     *                      necessarily an error depending on where we are in
     *                      the parsing rules.
     */
    private char nextChar() throws EtxException {
        char ch;

        do {
            if (++pos == expression.length())
                throw new EtxException("Etx reached");

            ch = expression.charAt(pos);

        } while (Character.isWhitespace(ch));

        return ch;
    }
}
