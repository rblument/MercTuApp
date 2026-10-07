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
package edu.regis.merc.svc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.regis.merc.model.RepresentationKind;
import org.junit.jupiter.api.Test;

/**
 * Tests for how a submitted answer is matched against what a step expects.
 *
 * A component id is only unique within one representation. The lambda calculus
 * view numbers its components from LC_EXPRESSION primary keys while the
 * mu-recursive view numbers its own, and the ranges used to overlap: the
 * lambda variable "s" and the mu-recursive parameter "x" were both 101. Step
 * 50 asks for the outer parameter of \s.\z.z and expects 101, so grading on
 * the id alone accepted a click on the mu-recursive function as the answer to
 * a lambda calculus question.
 */
public class GradingTest {

    private static final int LAMBDA_S = 101;

    private static MercTutor.StepData expecting(RepresentationKind kind, int componentId) {
        MercTutor.StepData data = new MercTutor.StepData();
        data.representation = kind;
        data.correctComponentId = componentId;
        return data;
    }

    private static MercTutor.StudentPayload answering(RepresentationKind kind, int componentId) {
        MercTutor.StudentPayload payload = new MercTutor.StudentPayload();
        payload.representation = kind;
        payload.selectedComponentId = componentId;
        return payload;
    }

    @Test
    void acceptsTheRightComponentInTheRightRepresentation() {
        assertTrue(MercTutor.isCorrect(
                expecting(RepresentationKind.LAMBDA_CALCULUS, LAMBDA_S),
                answering(RepresentationKind.LAMBDA_CALCULUS, LAMBDA_S)));
    }

    @Test
    void rejectsTheSameIdFromAnotherRepresentation() {
        // The regression: clicking "x" in Z(x) = 0 once scored as the outer
        // parameter of \s.\z.z, because both carried id 101.
        assertFalse(MercTutor.isCorrect(
                expecting(RepresentationKind.LAMBDA_CALCULUS, LAMBDA_S),
                answering(RepresentationKind.MU_RECURSIVE, LAMBDA_S)));
    }

    @Test
    void rejectsTheWrongComponentInTheRightRepresentation() {
        assertFalse(MercTutor.isCorrect(
                expecting(RepresentationKind.LAMBDA_CALCULUS, LAMBDA_S),
                answering(RepresentationKind.LAMBDA_CALCULUS, 102)));
    }

    @Test
    void rejectsAnAnswerThatNamesNoRepresentation() {
        assertFalse(MercTutor.isCorrect(
                expecting(RepresentationKind.LAMBDA_CALCULUS, LAMBDA_S),
                answering(null, LAMBDA_S)));
    }

    @Test
    void rejectsAStepThatNamesNoRepresentation() {
        // Seed data written before representations existed would otherwise be
        // graded on the id alone, which is the defect this guards against.
        assertFalse(MercTutor.isCorrect(
                expecting(null, LAMBDA_S),
                answering(RepresentationKind.LAMBDA_CALCULUS, LAMBDA_S)));
    }

    @Test
    void rejectsMissingData() {
        assertFalse(MercTutor.isCorrect(null, answering(RepresentationKind.LAMBDA_CALCULUS, LAMBDA_S)));
        assertFalse(MercTutor.isCorrect(expecting(RepresentationKind.LAMBDA_CALCULUS, LAMBDA_S), null));
    }
}
