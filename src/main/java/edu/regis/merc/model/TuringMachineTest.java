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
 * Test file for Turing Machine model
 *
 * @author Michael Nguyen
 */
public class TuringMachineTest {
    public static void main(String[] args) {
        // Set up Input Alphabet
        ArrayList<Character> inputAlphabet = new ArrayList<>();
        inputAlphabet.add('0');
        inputAlphabet.add('1');

        // Set up Tape Alphabet
        ArrayList<Character> tapeAlphabet = new ArrayList<>(inputAlphabet);
        tapeAlphabet.add('_');

        // Declare States
        State q0 = new State("q0");
        State qAccept = new State("qAccept");
        State qReject = new State("qReject");

        TuringMachine tm = new TuringMachine(inputAlphabet, tapeAlphabet, q0, qAccept, qReject);

        // Add Transitions
        Transition t0 = new Transition('0', '0', MoveKind.RIGHT, q0);
        tm.addTransition(q0, t0);
        Transition tBlank = new Transition('_', '_', MoveKind.RIGHT, qAccept);
        tm.addTransition(q0, tBlank);

        // Run tests
        System.out.println("\nTuringMachine Test:");
        System.out.println("Expected behavior: accept all strings of '0' and none of '1'\n");
        System.out.println("Expected: Accept");
        runTest(tm, ""); // Expected: true
        runTest(tm, "0"); // Expected: true
        runTest(tm, "00"); // Expected: true
        System.out.println("\nExpected: Reject");
        runTest(tm, "1"); // Expected: false
        runTest(tm, "01"); // Expected: false
        runTest(tm, "001"); // Expected: false
    }

    // Test method for inputs
    private static void runTest(TuringMachine tm, String input) {
        tm.setInput(input);
        tm.stepAll();
        System.out.println("Input '" + input + "' accepted: " + tm.isAccepted());
    }
}
