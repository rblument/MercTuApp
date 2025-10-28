// Kristin Ingram
// last updated: 10/28


package edu.regis.merc.model;

public class MuExpression {
    private Kind kind;
    private OpKind opKind;
    private Object leftOperand;
    private Object rightOperand;
    private String variableName;
    private int numberValue;

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
    public MuExpression(OpKind opKind, Object leftOperand, Object rightOperand) {
        this.kind = Kind.OPERATION;   // ✅ fixed
        this.opKind = opKind;         // ✅ correct type & variable name
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    public Kind getKind() { return kind; }
    public OpKind getOpKind() { return opKind; }
    public Object getLeftOperand() { return leftOperand; }
    public Object getRightOperand() { return rightOperand; }

    @Override
    public String toString() {
        switch (kind) {
            case NUMBER: return Integer.toString(numberValue);
            case VARIABLE: return variableName;
            case OPERATION: return "(" + leftOperand + " " + opKind + " " + rightOperand + ")";
            default: return "";
        }
    }
}
