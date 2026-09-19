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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * Test file for Turing Machine model
 *
 * @author Michael Nguyen
 */
public class TuringMachineTest {
    private TuringMachine tm;
    private State q0;
    private State qAccept;
    private State qReject;

    @BeforeEach
    void setUp() {
        ArrayList<Character> inputAlphabet = new ArrayList<>();
        inputAlphabet.add('0');
        inputAlphabet.add('1');

        ArrayList<Character> tapeAlphabet = new ArrayList<>(inputAlphabet);
        tapeAlphabet.add(TuringMachine.BLANK);

        q0 = new State();
        q0.setName("q0");
        qAccept = new State();
        qAccept.setName("qAccept");
        qReject = new State();
        qReject.setName("qReject");

        tm = new TuringMachine(
            inputAlphabet,
            tapeAlphabet,
            q0,
            qAccept,
            qReject
        );

        q0.setTmId(tm.getId());
        qAccept.setTmId(tm.getId());
        qReject.setTmId(tm.getId());

        Transition t0 = new Transition('0', '0', MoveKind.RIGHT, q0);
        tm.addTransition(q0, t0);
        Transition t1 = new Transition('1', '1', MoveKind.RIGHT, qReject);
        tm.addTransition(q0, t1);
        Transition tBlank = new Transition(TuringMachine.BLANK, TuringMachine.BLANK, MoveKind.RIGHT, qAccept);
        tm.addTransition(q0, tBlank);
    }

    @Test
    void testEmptyStringAccepted() {
        tm.setInput("");
        tm.stepAll();
        assertTrue(tm.isAccepted());
    }

    @Test
    void testSingleZeroAccepted() {
        tm.setInput("0");
        tm.stepAll();
        assertTrue(tm.isAccepted());
    }

    @Test
    void testDoubleZeroAccepted() {
        tm.setInput("00");
        tm.stepAll();
        assertTrue(tm.isAccepted());
    }

    @Test
    void testSingleOneRejected() {
        tm.setInput("1");
        tm.stepAll();
        assertFalse(tm.isAccepted());
    }

    @Test
    void testZeroOneRejected() {
        tm.setInput("01");
        tm.stepAll();
        assertFalse(tm.isAccepted());
    }

    @Test
    void testZeroZeroOneRejected() {
        tm.setInput("001");
        tm.stepAll();
        assertFalse(tm.isAccepted());
    }
}
