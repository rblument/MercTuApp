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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for how a {@link PendingTask} moves a student through the steps of a
 * task.
 *
 * These cover the contract SessionDAO.updateCurrentStep depends on when it
 * persists an advance: which step becomes current, which step was left behind,
 * and whether the new one looks saved. A student who answered correctly but
 * was shown the same question again is the failure these guard against.
 */
public class PendingTaskTest {

    private Step firstStep;
    private Step lastStep;
    private Task task;
    private PendingTask pendingTask;

    @BeforeEach
    void setUp() {
        firstStep = new Step(50);
        firstStep.setSequenceIndex(0);

        lastStep = new Step(51);
        lastStep.setSequenceIndex(1);

        ArrayList<Step> steps = new ArrayList<>();
        steps.add(firstStep);
        steps.add(lastStep);

        task = new Task(10);
        task.setSteps(steps);

        pendingTask = new PendingTask(task);
    }

    @Test
    void advancesToTheNextStep() {
        pendingTask.setCurrentStep(new PendingStep(1, firstStep));

        assertTrue(pendingTask.advanceStep(), "a step remained, so the task should advance");
        assertEquals(51, pendingTask.currentStep().getStep().getId());
    }

    @Test
    void doesNotAdvancePastTheLastStep() {
        pendingTask.setCurrentStep(new PendingStep(2, lastStep));

        assertFalse(pendingTask.advanceStep(), "no step remained, so the task is complete");
        assertEquals(51, pendingTask.currentStep().getStep().getId(),
                "a refused advance must leave the current step alone");
    }

    @Test
    void doesNotAdvanceWhenTheTaskHasASingleStep() {
        ArrayList<Step> onlyStep = new ArrayList<>();
        onlyStep.add(firstStep);
        task.setSteps(onlyStep);

        pendingTask.setCurrentStep(new PendingStep(1, firstStep));

        assertFalse(pendingTask.advanceStep());
    }

    @Test
    void hasNoReplacedStepBeforeAdvancing() {
        pendingTask.setCurrentStep(new PendingStep(1, firstStep));

        assertNull(pendingTask.getReplacedStep());
    }

    @Test
    void retainsTheStepItAdvancedOff() {
        PendingStep completed = new PendingStep(1, firstStep);
        pendingTask.setCurrentStep(completed);

        pendingTask.advanceStep();

        // The DAO writes this row's completion when it saves the advance. If
        // advanceStep drops it, the completed step is silently never recorded.
        assertSame(completed, pendingTask.getReplacedStep());
    }

    @Test
    void theStepAdvancedToIsUnsaved() {
        pendingTask.setCurrentStep(new PendingStep(1, firstStep));

        pendingTask.advanceStep();

        // SessionDAO.updateCurrentStep branches on this to decide between
        // inserting a row for a newly reached step and updating an existing one.
        assertEquals(Model.DEFAULT_ID, pendingTask.currentStep().getId());
    }

    @Test
    void aRefusedAdvanceLeavesTheReplacedStepUnset() {
        pendingTask.setCurrentStep(new PendingStep(2, lastStep));

        pendingTask.advanceStep();

        assertNull(pendingTask.getReplacedStep(),
                "nothing was replaced, so there is no prior row to write");
    }
}
