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
 * Specifies the components of the Lambda Calculus expression in the current 
 * problem that should be displayed to the student in the GUI.
 * 
 * @author rickb
 */
public class LCViewConfiguration extends Model {
    /**
     * A comma delimited id list of the parameters that should be displayed.
     */
    private String parameterIds = "";
    
    /**
     * A comma delimited id list of the expression bodies that should be displayed.
     */
    private String bodyIds = "";
    
    /**
     * A comma delimited id list of the arguments that should be displayed.
     */
    private String argumentIds = "";
    
    public LCViewConfiguration() {
        this(DEFAULT_ID);
    }
    
    public LCViewConfiguration(int id) {
        super(id);
    }

    public String getParameterIds() {
        return parameterIds;
    }

    public void setParameterIds(String parameterIds) {
        this.parameterIds = parameterIds;
    }

    public String getBodyIds() {
        return bodyIds;
    }

    public void setBodyIds(String bodyIds) {
        this.bodyIds = bodyIds;
    }

    public String getArgumentIds() {
        return argumentIds;
    }

    public void setArgumentIds(String argumentIds) {
        this.argumentIds = argumentIds;
    }
}
