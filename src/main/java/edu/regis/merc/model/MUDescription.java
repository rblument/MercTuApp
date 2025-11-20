package edu.regis.merc.model;

public class MUDescription extends Model{
    private MUStepSubType type;
    private int muID = Model.DEFAULT_ID;
    private int componentID;
    private String data;

    public MUDescription(int id, MUStepSubType type) {
        super(id);
        this.type = type;
    }

    public MUStepSubType getType() {
        return type;
    }

    public void setType(MUStepSubType type) {
        this.type = type;
    }

    public int getMuID() {
        return muID;
    }

    public void setMuID(int muID) {
        this.muID = muID;
    }

    public int getComponentID() {
        return componentID;
    }

    public void setComponentID(int componentID) {
        this.componentID = componentID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
