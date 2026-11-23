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

import edu.regis.merc.err.MuParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * A Parser for Mu functions.
 *
 * <p>The grammar it accepts:
 * <pre>
 *   expr := number
 *         | name [ '(' expr (',' expr)* ')' ]
 *         | '(' expr op expr ')'
 *   op   := '+' | '-' | '*' | '/'
 * </pre>
 *
 * <p>Not thread safe: a parser tracks its position in the input, so each
 * parse needs its own instance.
 *
 * @author AndrewBazen
 */
public class MuParser {

    private String input;
    private int position;

    /**
     * Given the right hand side of an Expression, parse the internal expressions.
     *
     * @param rhs the right hand side of a Mu function
     * @return a MuExpression
     * @throws MuParseException throws on null, empty, or syntax it can't parse
     */
    public MuExpression parse(String rhs) throws MuParseException {
        if (rhs == null || rhs.isEmpty()) {
            throw new MuParseException("Mu Expression cannot be null or empty!");
        }

        input = rhs;
        position = 0;

        MuExpression expr = parseMuExpression();
        skipWhitespace();

        if (position < input.length()) {
            throw new MuParseException("Unexpected input at position " + position
                + ": " + input.substring(position));
        }
        return expr;
    }

    /**
     * Parses one expression, leaving the position just past what it consumed.
     *
     * @return a MuExpression
     */
    private MuExpression parseMuExpression() throws MuParseException {
        skipWhitespace();

        if (position >= input.length()) {
            throw new MuParseException("Expected an expression but reached end of input");
        }

        char ch = input.charAt(position);

        if (Character.isDigit(ch)) {
            return parseNumber();
        } else if (Character.isLetter(ch)) {
            return parseNameOrApplication();
        } else if (ch == '(') {
            return parseOperation();
        } else {
            throw new MuParseException("Unexpected character '" + ch
                + "' at position " + position);
        }
    }

    /**
     * Parses a run of digits into a number.
     *
     * @return a MuExpression
     */
    private MuExpression parseNumber() throws MuParseException {
        int start = position;

        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            position++;
        }

        String digits = input.substring(start, position);

        try {
            return new MuExpression(Integer.parseInt(digits));
        } catch (NumberFormatException ex) {
            throw new MuParseException("Number '" + digits + "' at position " + start
                + " is too large", ex);
        }
    }

    /**
     * Parses a name, which is an application when a '(' follows it immediately
     * and a variable otherwise.
     *
     * @return a MuExpression
     */
    private MuExpression parseNameOrApplication() throws MuParseException {
        String name = consumeName();

        if (position < input.length() && input.charAt(position) == '(') {
            return new MuExpression(name, parseArguments());
        }

        return new MuExpression(name);
    }

    /**
     * Parses a parenthesized argument list, which must hold at least one argument.
     *
     * @return the arguments in the order they appear
     */
    private List<MuExpression> parseArguments() throws MuParseException {
        expect('(');

        List<MuExpression> arguments = new ArrayList<>();

        skipWhitespace();

        if (position < input.length() && input.charAt(position) == ')') {
            throw new MuParseException("Expected an argument but found ')' at position "
                + position);
        }

        arguments.add(parseMuExpression());
        skipWhitespace();

        while (position < input.length() && input.charAt(position) == ',') {
            position++;
            arguments.add(parseMuExpression());
            skipWhitespace();
        }

        expect(')');

        return arguments;
    }

    /**
     * Parses a parenthesized infix operation.
     *
     * @return a MuExpression
     */
    private MuExpression parseOperation() throws MuParseException {
        expect('(');

        MuExpression leftOperand = parseMuExpression();
        OpKind opKind = consumeOperator();
        MuExpression rightOperand = parseMuExpression();

        expect(')');

        return new MuExpression(opKind, leftOperand, rightOperand);
    }

    /**
     * Consumes a name, which starts with a letter and may then hold digits.
     *
     * @return the name
     */
    private String consumeName() {
        int start = position;

        while (position < input.length() && Character.isLetterOrDigit(input.charAt(position))) {
            position++;
        }

        return input.substring(start, position);
    }

    /**
     * Consumes one infix operator symbol.
     *
     * @return the operator it denotes
     */
    private OpKind consumeOperator() throws MuParseException {
        skipWhitespace();

        if (position >= input.length()) {
            throw new MuParseException("Expected an operator but reached end of input");
        }

        int start = position;
        char ch = input.charAt(position);

        position++;

        if (ch == '+') {
            return OpKind.ADD;
        } else if (ch == '-') {
            return OpKind.SUB;
        } else if (ch == '*') {
            return OpKind.MUL;
        } else if (ch == '/') {
            return OpKind.DIV;
        } else {
            throw new MuParseException("Expected an operator but found '" + ch
                + "' at position " + start);
        }
    }

    /**
     * Consumes the given character, throwing if it is not the next one.
     *
     * @param expected the character the grammar requires here
     */
    private void expect(char expected) throws MuParseException {
        skipWhitespace();

        if (position >= input.length()) {
            throw new MuParseException("Expected '" + expected
                + "' but reached end of input");
        }

        if (input.charAt(position) != expected) {
            throw new MuParseException("Expected '" + expected + "' but found '"
                + input.charAt(position) + "' at position " + position);
        }

        position++;
    }

    /**
     * Skips any leading whitespace in the input.
     */
    private void skipWhitespace() {
        while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
            position++;
        }
    }
}
