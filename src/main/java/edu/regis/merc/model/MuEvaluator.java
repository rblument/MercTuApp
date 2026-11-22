package edu.regis.merc.model;

import java.util.Map;

// evaluates MuExpression trees into integer results

public class MuEvaluator{
    /**
     * Evaluate a MuExpression using a mapping of variable names to integer values.
     *
     * @param expr the MuExpression to evaluate
     * @param env a map from variable names to integer values
     * @return integer result
     */
    public int eval(MuExpression expr, Map<String, Integer> env) {

        switch (expr.getKind()) {

            case NUMBER:
                return expr.getNumberValue();

            case VARIABLE: {
                String name = expr.getVariableName();
                if (!env.containsKey(name)) {
                    throw new IllegalArgumentException("Unbound variable: " + name);
                }
                return env.get(name);
            }

            case OPERATION:
                return evalOperation(expr, env);

            case APPLICATION:
                // Evaluating an application needs a table of the functions it
                // can call (S, Z, P, and those defined in the DB).
                throw new UnsupportedOperationException(
                        "Function application is not evaluated yet: " + expr);

            default:
                throw new IllegalStateException("Unknown expression kind: " + expr.getKind());
        }
    }

    private int evalOperation(MuExpression expr, Map<String, Integer> env) {
        int leftVal = evalOperand(expr.getLeftOperand(), expr, env);
        int rightVal = evalOperand(expr.getRightOperand(), expr, env);

        switch (expr.getOpKind()) {
            case ADD: return leftVal + rightVal;
            case SUB: return leftVal - rightVal;
            case MUL: return leftVal * rightVal;
            case DIV:
                if (rightVal == 0) throw new ArithmeticException("Division by zero");
                return leftVal / rightVal;

            case ZERO:
                return 0;

            case SUCCESSOR:
                return leftVal + 1;

            case PROJECTION:
                return rightVal;

            default:
                throw new UnsupportedOperationException("Unsupported operator: " + expr.getOpKind());

        }
    }

    private int evalOperand(MuExpression operand, MuExpression operation,
                            Map<String, Integer> env) {
        if (operand == null) {
            throw new IllegalArgumentException("Missing operand in " + operation);
        }
        return eval(operand, env);
    }
}
