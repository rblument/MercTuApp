/*
 * MERC^T: Multiple External Representations of Computation Tutor
 * 
 *  (C) Richard Blumenthal, All rights reserved
 * 
 *  Unauthorized use, duplication or distribution without the authors'
 *  permission is strictly prohibited.
 * 
 *  Unless required by applicable law or agreed to in writing, this
 *  software is distributed on an "AS IS" basis without warranties
 *  or conditions of any kind, either expressed or implied.
 */
package edu.regis.merc.model;

/**
 * The legal step types
 * 
 * This class is essentially kludge since I couldn't get subclassing of
 * Gson/Json subtypes to work. So, I now manually handle it. 
 * 
 * @author rickb
 */
public enum StepSubType {
    /**
     * The user must acknowledge a message (i.e., perhaps via a pop-up dialog)
     */
    INFO_MESSAGE("Information Message"),
    
    REQUEST_HINT("Request Hint"),    
    
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
    StepSubType(String subType) {
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
    public static StepSubType findValue(String aTitle) {
        for (StepSubType kind : values()) {
            if (kind.getSubType().equalsIgnoreCase(aTitle))
                return kind;
        }
        
        return ERROR;
    }
}
