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
 * A Lambda Calculus variable (token) with a given name.
 * 
 * @author ellis
 * @author rickb
 */
public class LCVariable extends LCExpression {
    
    /**
     * Initialize this Lambda Calculus variable with the given symbolic name.
     * 
     * @param name an alphabetic name
     */
    public LCVariable(String name){
        this.name = name; // uses protected field from LCExpression
    }
    
    @Override
    public void setName(String name){
        this.name = name;
    }
    
    @Override
    public String getName(){
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
