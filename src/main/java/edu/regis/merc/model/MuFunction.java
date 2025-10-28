// kristin ingram +
// last updated: 10/28


package edu.regis.merc.model;

public class MuFunction {
    private int id;
    private LeftHandSide lhs;
    private MuExpression rhs;

    public MuFunction(int id, LeftHandSide lhs, MuExpression rhs) {
        this.id = id;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public int getId() { return id; }
    public LeftHandSide getLhs() { return lhs; }
    public MuExpression getRhs() { return rhs; }

    @Override
    public String toString() {
        return lhs.getName() + "(" + lhs.getParameters() + ") = " + rhs;
    }
}
