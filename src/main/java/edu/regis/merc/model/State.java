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
 * State class for Turing Machine Model
 *
 * @author Michael Nguyen
 */
public class State extends Node {
    /**
     * The name of this state.
     */
    private String name;
    private int stateId;

    // Default constructor for State class
    public State(String name) {
        this.name = name;
    }

    public State(int stateId, int machineId, String name) {
        this.name = name;
        this.stateId = stateId;
        setId(machineId);
    }

    // Getter and setter methods
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getStateId() {
        return stateId;
    }
    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    // Override equals() method for State comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        State state = (State) o;
        return name.equals(state.name);
    }

    // Override hashCode() method for hash codes
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    // Override toString method
    @Override
    public String toString() {
        return name;
    }

}
