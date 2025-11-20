package edu.regis.merc.model;

public class LCDescription extends Model {
    private LCStepSubType type;
    private int lcID = Model.DEFAULT_ID;
    private int componentID;
    private String data;

    public LCDescription(LCStepSubType type) {
        this(DEFAULT_ID, type);
    }

    public LCDescription(int id, LCStepSubType type) {
        super(id);
        this.type = type;
    }

    public LCStepSubType getType() {
        return type;
    }

    public void setType(LCStepSubType type) {
        this.type = type;
    }

    public int getLcID() {
        return lcID;
    }

    public void setLcID(int lcID) {
        this.lcID = lcID;
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
