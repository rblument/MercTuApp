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

    public LCApplication(){
    }

    public void setFunction(LCExpression function){
        this.function = function;
    }

    public LCExpression getFunction(){
        return function;
    }

    public void setArg(LCExpression arg){
        this.arg = arg;
    }

    public LCExpression getArg(){
        return arg;
    }
}
