package edu.regis.merc.model;

public enum MUStepSubType {
    DISPLAY_ALL("Displays All"),
    DISPLAY_FUNCTION_NAME("Displays Function name"),
    SELECT_FUNCTION_NAME("Selects Function name"),
    DISPLAY_PARAMETER("Displays parameter"),
    SELECT_PARAMETER("Selects parameter"),
    DISPLAY_RHS_COMPONENT("Display Right Hand Side Component"),
    SELECT_RHS_COMPONENT("Selects Right Hand Side Component"),
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
    MUStepSubType(String subType) {
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

    /**
     * Return the enum value for the given title.
     *
     * @param aTitle
     * @return
     */
    public static MUStepSubType findValue(String aTitle) {
        for (MUStepSubType kind : values()) {
            if (kind.getSubType().equalsIgnoreCase(aTitle))
                return kind;
        }

        return ERROR;
    }

}
