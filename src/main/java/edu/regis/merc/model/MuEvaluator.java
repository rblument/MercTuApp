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
                return Integer.parseInt(expr.toString());

            case VARIABLE:
                if (!env.containsKey(expr.toString())) {
                    throw new IllegalArgumentException("Unbound variable: " + expr.toString());
                }
                return env.get(expr.toString());

            case OPERATION:
                return evalOperation(expr, env);

            default:
                throw new IllegalStateException("Unknown expression kind: " + expr.getKind());
        }
    }

    private int evalOperation(MuExpression expr, Map<String, Integer> env) {
        Object left = expr.getLeftOperand();
        Object right = expr.getRightOperand();

        // Each operand may itself be a MuExpression or a raw String/int
        int leftVal = convertOperand(left, env);
        int rightVal = convertOperand(right, env);

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

    private int convertOperand(Object operand, Map<String, Integer> env) {
        if (operand instanceof MuExpression) {
            return eval((MuExpression) operand, env);
        } else if (operand instanceof String) {
            return env.get((String) operand);
        } else if (operand instanceof Integer) {
            return (int) operand;
        }
        throw new IllegalArgumentException("Unknown operand type: " + operand);
    }
}
