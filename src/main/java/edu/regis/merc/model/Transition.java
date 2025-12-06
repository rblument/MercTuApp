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
public class Transition extends Edge {
    private char read; // Symbol under the head
    private char write; // Symbol to overwrite in current cell
    private MoveKind direction; // Move left or right
    private State toState; // Next state after applying transition
    private State fromState;
    
    public Transition() {
        this(DEFAULT_ID);
    }
    
    public Transition(int id) {
        super(id);
        
        read = TuringMachine.BLANK;
        write = TuringMachine.BLANK;
        direction = MoveKind.RIGHT;
    }

    // Default constructor method for Transition class
    public Transition(char read, char write, MoveKind direction, State nextState) {
        super(DEFAULT_ID);
        
        this.read = read;
        this.write = write;
        this.direction = direction;
        this.toState = nextState;
    }

    // Getter methods
    public char getRead() {
        return read;
    }
    
    public void setRead(char read) {
        this.read = read;
    }
    
    public char getWrite() {
        return write;
    }
    
    public void setWrite(char write) {
        this.write = write;
    }
    
    public MoveKind getDirection() {
        return direction;
    }
    
    public void setDirection(MoveKind direction) {
        this.direction = direction;
    }
    
    public State getToState() {
        return toState;
    }
    
    public void setToState(State toState) {
        this.toState = toState;
    }

    public State getFromState() {
        return fromState;
    }

    public void setFromState(State fromState) {
        this.fromState = fromState;
    }
    
    

}
