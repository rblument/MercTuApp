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
 * Specifies the components of the Mu Function in the current 
 * problem that should be displayed to the student in the GUI.
 * 
 * @author rickb
 */
public class MuViewConfiguration extends Model {
    /**
     * If true, the name of the Mu function shoudl be highlighted.
     */
    private boolean highlightName = false;
    
    /**
     * A comma delimited id list of the parameters that should be displayed.
     */
    private String parameterIds = "";
    
    /**
     * A comma delimited id list of the right-hand-side operands that should be displayed.
     */
    private String rhsIds = "";
    
    /**
     * A comma delimited id list of the arguments that should be displayed.
     */
    private String argumentIds = "";
    
    public MuViewConfiguration() {
        this(DEFAULT_ID);
    }
    
    public MuViewConfiguration(int id) {
        super(id);
    }

    public boolean isHighlightName() {
        return highlightName;
    }

    public void setHighlightName(boolean highlightName) {
        this.highlightName = highlightName;
    }

    public String getParameterIds() {
        return parameterIds;
    }

    public void setParameterIds(String parameterIds) {
        this.parameterIds = parameterIds;
    }

    public String getRhsIds() {
        return rhsIds;
    }

    public void setRhsIds(String rhsIds) {
        this.rhsIds = rhsIds;
    }

    public String getArgumentIds() {
        return argumentIds;
    }

    public void setArgumentIds(String argumentIds) {
        this.argumentIds = argumentIds;
    }
}

