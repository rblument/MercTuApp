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
 * The task types that a student may be required to complete in the tutor.
 * 
 * @author rickb
 */
public enum TaskKind {
    /**
     * A message from the tutor that must be acknowledged, such as, an 
     * indication that the tutor is still waiting (this is not a USAGE).
     */
    MESSAGE("Message"),
    
    /**
     * A task requiring a Student to complete a domain Problem.
     */
    PROBLEM("Problem"),
    
    /**
     * A message from the tutor that needs to be acknowledge, which describes
     * how to use the tutor.
     */
    USAGE("Usage"),
    
    ERROR("Error");
    
    /**
     * A GUI displayable string identifying this task kind.
     */
    private final String title;

    /**
     * Initialize this task kind with its title.
     * 
     * @param title 
     */
    TaskKind(String title) {
        this.title = title;
    }
    
    /**
     * Return the title for this task.
     * 
     * @return a String
     */
    public String title() {
        return title;
    }
}
