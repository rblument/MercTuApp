// Kristin Ingram
// last updated: 10/28


package edu.regis.merc.model;

import java.util.List;
import java.util.ArrayList;

public class MuExpression {
    private Kind kind;
    private OpKind opKind;
    private MuExpression leftOperand;
    private MuExpression rightOperand;
    private String variableName;
    private int numberValue;
    private String functionName;
    private List<MuExpression> arguments;

    // For numbers
    public MuExpression(int numberValue) {
        this.kind = Kind.NUMBER;
        this.numberValue = numberValue;
    }

    // For variables
    public MuExpression(String variableName) {
        this.kind = Kind.VARIABLE;
        this.variableName = variableName;
    }

    // For operations
    public MuExpression(OpKind opKind, MuExpression leftOperand, MuExpression rightOperand) {
        this.kind = Kind.OPERATION;
        this.opKind = opKind;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    // For applications
    public MuExpression(String functionName, List<MuExpression> arguments) {
        this.kind = Kind.APPLICATION;
        this.functionName = functionName;
        // Copying into a new ArrayList keeps a caller from mutating the tree
        // after the fact.
        this.arguments = new ArrayList<>(arguments);
    }

    public Kind getKind() { return kind; }
    public OpKind getOpKind() { return opKind; }
    public MuExpression getLeftOperand() { return leftOperand; }
    public MuExpression getRightOperand() { return rightOperand; }

    @Override
    public String toString() {
        switch (kind) {
            case NUMBER: return Integer.toString(numberValue);
            case VARIABLE: return variableName;
            case OPERATION: return "(" + leftOperand + " " + opKind + " " + rightOperand + ")";
            case APPLICATION: return functionName + "(" + argumentList() + ")";
            default: return "";
        }
    }

    // helper to account for String.join not being able to take MuExpression
    private String argumentList() {
        StringBuilder buff = new StringBuilder();

        for (MuExpression arg : arguments) {
            if (buff.length() > 0) {
                buff.append(", ");
            }
            buff.append(arg);
        }
        return buff.toString();
    }
}
