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
 * Permissible types of actions the tutor is asking a student to perform.
 * 
 * @author rickb
 */
public enum StudentActionKind {
    /**
     * The student is being asked to acknowledge the message being displayed.
     */
    INFORMATION_MESSAGE("Information Message"),
    
    /**
     * The student is being asked by the tutor to request a hint.
     */
    HINT_REQUEST("Hint Request"),
    
    /**
     * The student is being asked to perform some operation(s) on the
     * computational models currently displayed in the GUI.
     */
    MODEL_REQUEST("Model Request");
    
    /**
     * A GUI displayable string identifying this student action.
     */
    private final String title;
    
    StudentActionKind(String title) {
        this.title = title;
    }
    
    /**
     * Return this student action's title.
     * 
     * @return a GUI displayable string identifying this student action.
     */
    public String title() {
        return title;
    }
}
