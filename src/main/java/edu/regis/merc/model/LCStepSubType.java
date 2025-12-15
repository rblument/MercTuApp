package edu.regis.merc.model;

/**
 * 
 * @author oscar
 */
public enum LCStepSubType {

    DISPLAY_ALL("Display All"),
    DISPLAY_EXPRESSION("Display Expression"),
    SELECT_EXPRESSION("Select Expression"),
    DISPLAY_ABSTRACTION("Display Abstraction"),
    SELECT_ABSTRACTION("Select Abstraction"),
    DISPLAY_PARAMETER("Display Parameter"),
    SELECT_PARAMETER("Select Parameter"),
    DISPLAY_BODY("Display Body"),
    SELECT_BODY("Select Body"),
    DISPLAY_APPLICATION("Display Application"),
    SELECT_APPLICATION("Select Application"),
    DISPLAY_ARG("Display Arguments"),
    SELECT_ARG("Select Arguments"),
    ERROR("Error"); 


    /**
     * The name used by the server to identify this request.
     */
    private final String subType;

    /**
     * Initialize this enum object with the given title.
     *
     * @param subType
     */
    LCStepSubType(String subType) {
    this.subType = subType;
}



    /**
     * Return the request name that is used by the server.
     *
     * @return a String
     */
    public String getSubType() {
        return subType;
    }

    /**
     * Return the subType name that is used by the server
     *
     * @return a String
     */
    @Override
    public String toString() {
        return subType;
    }
}


