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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests that sequence indexes assigned to a {@link Step} or {@link Hint} can
 * be read back.
 *
 * Both setters were once written as self-assignments -- the parameter name was
 * misspelled, so "this.sequenceIndex = sequenceIndex" quietly assigned the
 * field to itself and every step and hint reported its initial value of 1
 * regardless of what the database held. That compiles and raises no warning,
 * so it survived until someone read the serialized session and noticed every
 * index was the same. These assertions are trivial by design: they exist to
 * make the same typo fail loudly rather than silently.
 */
public class SequenceIndexTest {

    @Test
    void stepRetainsItsSequenceIndex() {
        Step step = new Step(50);

        step.setSequenceIndex(7);

        assertEquals(7, step.getSequenceIndex());
    }

    @Test
    void stepRetainsAZeroSequenceIndex() {
        Step step = new Step(50);

        // Zero is the value the bug hid best: it is the first step's index,
        // and the field's stale default of 1 made step 0 unfindable.
        step.setSequenceIndex(0);

        assertEquals(0, step.getSequenceIndex());
    }

    @Test
    void hintRetainsItsSequenceIndex() {
        Hint hint = new Hint(12);

        hint.setSequenceIndex(3);

        assertEquals(3, hint.getSequenceIndex());
    }

    @Test
    void hintRetainsAZeroSequenceIndex() {
        Hint hint = new Hint(12);

        hint.setSequenceIndex(0);

        assertEquals(0, hint.getSequenceIndex());
    }
}
