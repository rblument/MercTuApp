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

import java.util.LinkedList;

/**
 * Configuration class for Turing Machine Model
 *
 * @author Michael Nguyen
 */
public class Configuration extends TitledModel {
    private State state; // The current state
    private LinkedList<Character> tape; // Copy of the tape at the moment
    private int headLocation; // Head current position on tape

    // Default constructor method for Configuration class
    public Configuration(State state, LinkedList<Character> tape, int headLocation) {
        this.state = state;
        this.tape = new LinkedList<>(tape);
        this.headLocation = headLocation;
    }

    // Getter methods
    public State getState() {
        return state;
    }
    public LinkedList<Character> getTape() {
        return tape;
    }
    public int getHeadLocation() {
        return headLocation;
    }

}
