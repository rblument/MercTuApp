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
import java.util.ArrayList;

/**
 * The base class for Lambda Calculus expressions. 
 * @author ellis
 */
public class LCExpression {

    protected boolean isParentheses;
    protected boolean isApplied;
    
    // fields for JSON data
    protected boolean isCurried;
    protected ArrayList<LCVariable> parameters;
    protected ArrayList<LCExpression> body;
    protected LCExpression function;
    protected LCExpression arg;
    protected String name;
    
    public LCExpression() {
        isParentheses = false;
        isApplied = false;
        parameters = new ArrayList<>();
        body = new ArrayList<>();
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
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString(){
        StringBuilder buff = new StringBuilder();
        
        if(isParentheses) {
            buff.append("(");
        }
        
        // 1. Abstraction logic 
        if (parameters != null && !parameters.isEmpty()){
            buff.append("\\");
            for(LCVariable param : parameters){
                buff.append(param.toString()); 
            }
            buff.append(".");
            if(body != null){
                for (LCExpression expr : body){
                    buff.append(" ");
                    buff.append(expr.toString());
                }
            }
        }
        // 2. Application logic
        else if (function != null && arg != null){
            buff.append(function.toString());
            buff.append(" ");
            buff.append(arg.toString());
        }
        // 3. Variable logic 
        else if (name != null){
            buff.append(name);
        }
        
        if (isParentheses){
            buff.append(")");
        }
        
        return buff.toString();
        
    }
          
}
