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
 * A Lambda Calculus Application (function call) with an identified Abstraction
 * and argument.
 * 
 * @author ellis
 * @author rickb
 */
public class LCApplication extends LCExpression {
    /**
     * The Lambda Calculus Abstraction that is applied to the argument in this
     * Application.
     */
    private LCAbstraction function;
    
    /**
     * The argument for this Application.
     */
    private LCExpression arg;
    
    public LCApplication(){
        function = null;
        arg = null;
    }
    
    public void setFunction(LCAbstraction function){
        this.function = function;
    }
    
    public LCAbstraction getFunction(){
        return function;
    }
    
    public void setArg(LCExpression arg){
        this.arg = arg;
    }
    
    public LCExpression getArg(){
        return arg;
    }
    
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        
        buff.append(function.toString());
        buff.append(" ");
        buff.append(arg.toString());
        
        return buff.toString();
    }
}
