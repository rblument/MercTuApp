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
 * Transition class for Turing Machine Model
 *
 * @author Michael Nguyen
 */
public class Transition extends TitledModel {
    private char read; // Symbol under the head
    private char write; // Symbol to overwrite in current cell
    private MoveKind direction; // Move left or right
    private State nextState; // Next state after applying transition

    // Default constructor method for Transition class
    public Transition(char read, char write, MoveKind direction, State nextState) {
        this.read = read;
        this.write = write;
        this.direction = direction;
        this.nextState = nextState;
    }

    // Getter methods
    public char getRead() {
        return read;
    }
    public char getWrite() {
        return write;
    }
    public MoveKind getDirection() {
        return direction;
    }
    public State getNextState() {
        return nextState;
    }

}
