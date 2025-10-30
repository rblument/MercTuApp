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
public class LCApplication {
    
    private LCAbstraction function;
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
}
