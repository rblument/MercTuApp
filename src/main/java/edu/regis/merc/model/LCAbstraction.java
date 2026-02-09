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
 * An Lambda Calculus Abstraction (function) with identified parameter(s) and body.
 * 
 * @author ellis
 * @author rickb
 */
public class LCAbstraction extends LCExpression {
    /**
     * If true, this Abstraction allows Curried parameters.
     */
    //private boolean isCurried = false;
    
    /**
     * The parameters in this Abstraction as a list of Lambda Calculus variables.
     */
    //private ArrayList<LCVariable> parameters;
    
    /**
     * The body of this Abstraction as a list of Lambda Calculus expressions.
     */
    //private ArrayList<LCExpression> body;
    
    /**
     * Initialize this Lambda Calculus Abstraction with an empty parameter list
     * and an empty body.
     */
    public LCAbstraction(){
        super();
//        this.parameters = new ArrayList<>();
//        this.body = new ArrayList<>();
        
    }
    
    public void addParameter(LCVariable parameter) {
        parameters.add(parameter);
    }
    
    public void setParameters(ArrayList<LCVariable> parameters){
        this.parameters = parameters;
    }
    
    public ArrayList<LCVariable> getParameters(){
        return parameters;
    }
    
    public void addBodyPart(LCExpression expression) {
        body.add(expression);
    }
    
    public void setBody(ArrayList<LCExpression> body){
        this.body = body;
    }
    
    public ArrayList<LCExpression> getBody(){
        return body;
    }

    public boolean isIsCurried() {
        return isCurried;
    }

    public void setIsCurried(boolean isCurried) {
        this.isCurried = isCurried;
    }
    
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        
        if (isParentheses) {
            buff.append("(");
        }
        
        buff.append("\\");
        
        for (LCVariable parameter : parameters) {
            buff.append(parameter);
        }
        
        buff.append(".");
        
        for (LCExpression expr : body) {
            buff.append(" ");
            buff.append(expr.toString());
        }
        
        if (isParentheses) {
            buff.append(")");
        }
        
        return buff.toString();
    }
}  
   