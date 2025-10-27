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
public class TuringMachine extends GraphicalComponent {
    /**
     * The sates in this Turing Machine.
     */
    private ArrayList<State> states;

    private LinkedList<Character> tape; // Working tape, starts as input, grows with blanks
    private LinkedList<Character> inputTape; // Copy of original input string - used to reset
    private ArrayList<Character> inputAlphabet; // List of allowed symbols
    private ArrayList<Character> tapeAlphabet; // List of allowed symbols on tape
    private State startState, acceptState, rejectState; // Special states
    private State currentState; // Current state of machine
    private int head; // Current index of tape

    private Map<State, List<Transition>> transitions = new HashMap<>(); // Rules grouped by state they apply to

    private HashSet<Transition> cachedTransitions;
    private static final char BLANK = '_'; // Default blank symbol

    public TuringMachine() {
        this(DEFAULT_ID);
    }

    public TuringMachine(int id) {
        super(id);

        states = new ArrayList<>();
        cachedTransitions = new HashSet<>();

        tape = new LinkedList<>();
        inputTape = new LinkedList<>();

    }

    // Default constructor for Turing Machine class

    public TuringMachine(ArrayList<Character> inputAlphabet, ArrayList<Character> tapeAlphabet, State startState, State acceptState, State rejectState) {
        this.inputAlphabet = inputAlphabet;
        this.tapeAlphabet = tapeAlphabet;
        this.startState = startState;
        this.acceptState = acceptState;
        this.rejectState = rejectState;
        this.tape = new LinkedList<>();
        this.inputTape = new LinkedList<>();
        states = new ArrayList<>();

        cachedTransitions = new HashSet<>();

        reset();
    }

    public TuringMachine(int id, String name, String description, ArrayList<Character> inputAlphabet, ArrayList<Character> tapeAlphabet, State startState, State acceptState, State rejectState) {
        super(id);
        super.setTitle(name);
        super.setDescription(description);
        this.inputAlphabet = inputAlphabet;
        this.tapeAlphabet = tapeAlphabet;
        this.startState = startState;
        this.acceptState = acceptState;
        this.rejectState = rejectState;
        this.tape = new LinkedList<>();
        this.inputTape = new LinkedList<>();
        states = new ArrayList<>();

        cachedTransitions = new HashSet<>();

        reset();
    }


    public void addState(State state) {
        states.add(state);
    }

    public ArrayList<State> getStates() {
        return states;
    }

    public void setStates(ArrayList<State> states) {
        this.states = states;
    }

    public void removeState(State state) {
        states.remove(state);

        // ToDo: How to handle transitions for this deleted state.
    }

    public State getStartState() {
        return startState;
    }

    public void setStartState(State startState) {
        this.startState = startState;
    }

    public State getAcceptState() {
        return acceptState;
    }

    public void setAcceptState(State acceptState) {
        this.acceptState = acceptState;
    }

    public State getRejectState() {
        return rejectState;
    }

    public void setRejectState(State rejectState) {
        this.rejectState = rejectState;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    public LinkedList<Character> getTape() {
        return tape;
    }

    public void setTape(LinkedList<Character> tape) {
        this.tape = tape;
    }

    public LinkedList<Character> getInputTape() {
        return inputTape;
    }

    public void setInputTape(LinkedList<Character> inputTape) {
        this.inputTape = inputTape;
    }

    public ArrayList<Character> getInputAlphabet() {
        return inputAlphabet;
    }

    public void setInputAlphabet(ArrayList<Character> inputAlphabet) {
        this.inputAlphabet = inputAlphabet;
    }

    public ArrayList<Character> getTapeAlphabet() {
        return tapeAlphabet;
    }

    public void setTapeAlphabet(ArrayList<Character> tapeAlphabet) {
        this.tapeAlphabet = tapeAlphabet;
    }

    public int getHead() {
        return head;
    }

    public void setHead(int head) {
        this.head = head;
    }

    // Add transition rule
    public void addTransition(State from, Transition transition) {
        transitions.computeIfAbsent(from, k -> new ArrayList<>()).add(transition);

        if (!cachedTransitions.contains(transition))
            cachedTransitions.add(transition);
    }

    public Map<State, List<Transition>> getTransitions() {
        return transitions;
    }

    public HashSet<Transition> getCachedTransitions() {
        return cachedTransitions;
    }

    public void setTransitions(Map<State, List<Transition>> transitions) {
        this.transitions = transitions;

        for (List<Transition> fromTransitions : transitions.values()) {
            for (Transition transition : fromTransitions)
                cachedTransitions.add(transition);
        }
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