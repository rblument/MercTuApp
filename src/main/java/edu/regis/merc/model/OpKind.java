// Kristin Ingram
// last updated: 10/28


package edu.regis.merc.model;

public enum OpKind {
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    ZERO("0"), 
    SUCCESSOR("S"), 
    PROJECTION("P"); 

    private final String symbol;

    OpKind(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
