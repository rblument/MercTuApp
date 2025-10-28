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
 *
 * @author ellis
 */
public class LCExpression {

    private boolean isParentheses;
    private boolean isApplied;
    
    public LCExpression() {
        isParentheses = false;
        isApplied = false;
    }
    
    public void setIsParentheses(boolean isParentheses){
        this.isParentheses = isParentheses;
    }
    
    public boolean getIsParentheses(){
        return isParentheses;
    }
    
    public void setIsApplied(boolean isApplied){
        this.isApplied = isApplied;
    }
    
    public boolean getIsApplied(){
        return isApplied;
    }
    
}
