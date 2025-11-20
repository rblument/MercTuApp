package edu.regis.merc.model;

public class TMDescription extends Model {
private TMStepSubType type;
private int tmId = Model.DEFAULT_ID;
private int componentId;
private String data;

public TMDescription(TMStepSubType type) {
    this(DEFAULT_ID, type);
}

    public TMDescription(int id, TMStepSubType type) {
        super(id);
        this.type = type;
    }

    public TMStepSubType getType() {
        return type;
    }

    public void setType(TMStepSubType type) {
        this.type = type;
    }

    public int getTmId() {
        return tmId;
    }

    public void setTmId(int tmId) {
        this.tmId = tmId;
    }

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
