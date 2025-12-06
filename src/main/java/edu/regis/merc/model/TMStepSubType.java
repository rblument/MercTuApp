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

public enum TMStepSubType {
    /**
     * The user must acknowledge a message (i.e., perhaps via a pop-up dialog)
     */
    DISPLAY_ALL("Display All"),
    DISPLAY_ACCEPT_STATE("Display Accept State"),
    SELECT_ACCEPT_STATE("Select Accept State"),
    DISPLAY_INITIAL_STATE("Display Initial State"),
    SELECT_INITIAL_STATE("Select Initial State"),
    DISPLAY_REJECT_STATE("Display Reject State"),
    SELECT_REJECT_STATE("Select Reject State"),
    DISPLAY_STATE("Display State"),
    SELECT_STATE("Select State"),
    DISPLAY_TRANSITION("Display Transition"),
    SELECT_TRANSITION("Select Transition"),
    HIGHLIGHT_TAPE_CELL("Highlight Tape Cell"),
    SELECT_TAPE_CELL("Select a tape cell"),
    DISPLAY_CONFIGURATION("Display Configuration (TBD)"),
    SELECT_CONFIGURATION("Select Configuration (TBD)"),
    INPUT_ALPHABET("Input Alphabet"),
    TAPE_ALPHABET("Tape Alphabet");

//    ERROR("Error");

    /**
     * The name used by the server to identify this request.
     */
    private final String subType;

    /**
     * Initialize this enum object with the given title.
     *
     * @param subType
     */
    TMStepSubType(String subType) {
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
    //public static TMStepSubType findValue(String aTitle) {
     //   for (TMStepSubType kind : values()) {
       //     if (kind.getSubType().equalsIgnoreCase(aTitle))
         //       return kind;
        //}

       // throw new IllegalArgumentException("Invalid TMStepSubType: " + aTitle);
   // }




}
