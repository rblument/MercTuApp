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

import java.util.*;

/**
 * The Turing Machine Model for MERC Tutor
 *
 * @author Michael Nguyen
 */
public class TuringMachine extends TitledModel {
    private LinkedList<Character> tape; // Working tape, starts as input, grows with blanks
    private LinkedList<Character> inputTape; // Copy of original input string - used to reset
    private ArrayList<Character> inputAlphabet; // List of allowed symbols
    private ArrayList<Character> tapeAlphabet; // List of allowed symbols on tape
    private State startState, acceptState, rejectState; // Special states
    private State currentState; // Current state of machine
    private int head; // Current index of tape

    private final Map<State, List<Transition>> transitions = new HashMap<>(); // Rules grouped by state they apply to
    private static final char BLANK = '_'; // Default blank symbol

    // Default constructor for Turing Machine class
    public TuringMachine(ArrayList<Character> inputAlphabet, ArrayList<Character> tapeAlphabet, State startState, State acceptState, State rejectState) {
        this.inputAlphabet = inputAlphabet;
        this.tapeAlphabet = tapeAlphabet;
        this.startState = startState;
        this.acceptState = acceptState;
        this.rejectState = rejectState;
        this.tape = new LinkedList<>();
        this.inputTape = new LinkedList<>();
        reset();
    }

    // Getter methods
    public State getStartState() {
        return startState;
    }
    public State getAcceptState() {
        return acceptState;
    }
    public State getRejectState() {
        return rejectState;
    }
    public State getCurrentState() {
        return currentState;
    }

    // Setter methods
    public void setStartState(State startState) {
        this.startState = startState;
    }
    public void setAcceptState(State acceptState) {
        this.acceptState = acceptState;
    }
    public void setRejectState(State rejectState) {
        this.rejectState = rejectState;
    }

    // Load input string into tape
    public void setInput(String input) {
        inputTape.clear();
        for (char c: input.toCharArray()) {
            if (!inputAlphabet.contains(c)) {
                throw new IllegalArgumentException("Invalid character: " + c);
            }
            inputTape.add(c);
        }
        reset();
    }

    // Add transition rule
    public void addTransition(State from, Transition transition) {
        transitions.computeIfAbsent(from, k -> new ArrayList<>()).add(transition);
    }

    // Reset head and tape to input
    public void reset() {
        tape = new LinkedList<>(inputTape);
        if (tape.isEmpty()) {
            tape.add(BLANK);
        }
        currentState = startState;
        head = 0;
    }

    // Execute one transition, if no match then go to reject state
    public void step() {
        if (currentState.equals(acceptState) || currentState.equals(rejectState)) {
            return;
        }
        char currentSymbol = tape.get(head);
        List<Transition> possible = transitions.getOrDefault(currentState, Collections.emptyList());

        for (Transition transition : possible) {
            if (transition.getRead() == currentSymbol) {
                tape.set(head, transition.getWrite());
                currentState = transition.getNextState();
                if (transition.getDirection() == MoveKind.LEFT) {
                    head--;
                    if (head < 0) {
                        tape.addFirst(BLANK);
                        head = 0;
                    }
                } else if (transition.getDirection() == MoveKind.RIGHT) {
                    head++;
                    if (head >= tape.size()) {
                        tape.addLast(BLANK);
                    }
                }
                return;
            }
        }
        currentState = rejectState;
    }

    // Execute up to n steps, stop if halted
    public void step(int n) {
        for (int i = 0; i < n && !isHalted(); i++) {
            step();
        }
    }

    // Run steps until machine is accepted or rejected
    public void stepAll() {
        while (!isHalted()) {
            step();
        }
    }

    // Snapshot of current configuration
    public Configuration getConfiguration() {
        return new Configuration(currentState, tape, head);
    }

    // Check if machine is accepted or rejected
    public boolean isHalted() {
        return currentState.equals(acceptState) || currentState.equals(rejectState);
    }

    // Check if machine is in accept state
    public boolean isAccepted() {
        return currentState.equals(acceptState);
    }

}
