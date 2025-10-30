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
 * 
 * @author ellis
 */
public class LCAbstraction {
    
    private ArrayList<String> parameters;
    private ArrayList<LCExpression> body;
    
    public LCAbstraction(){
        this.parameters = new ArrayList<String>();
        this.body = new ArrayList<LCExpression>();
        
    }
    
    public void setParameters(ArrayList<String> parameters){
        this.parameters = parameters;
    }
    
    public ArrayList<String> getParameters(){
        return parameters;
    }
    
    public void setBody(ArrayList<LCExpression> body){
        this.body = body;
    }
    
    public ArrayList<LCExpression> getBody(){
        return body;
    }
}  
   