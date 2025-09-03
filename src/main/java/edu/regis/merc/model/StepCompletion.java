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
 * Contains a student's solution to a problem in a task step.
 * 
 * @author rickb
 */
public class StepCompletion {
    /**
     * The step that was completed.
     */
    private Step step;

    /**
     * The date the associated step was completed (time in milliseconds).
     */
    private long date;

    /**
     * Whether the step timed out.
     */
    private boolean timeoutOccur;

    /**
     * The number of hints given in the GUI for this step.
     */
    private int hintsGiven;

    /**
     * A JSon object corresponding to the step type, which contains the 
     * students answer to the problem in the step.
     * 
     * For example, if the step.getSubType() == StepSubType.CHOICE_FUNCTION,
     * the data is a Json encoding of a ChoiceFunctionStep object. The student's
     * answer is the result field of this ChoiceFunctionStep.
     */
    private String data;

    public StepCompletion(Step step, String data) {
        date = System.currentTimeMillis();

        this.data = data;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
       this.data = data;
    }

    /**
     * Return the time the associated step was completed.
     * 
     * @return a long (time in milliseconds)
     */
    public long getDate() {
        return date;
    }

    /**
     * Set the time the associated step was completed (to only be used by
     * the database when restoring a step completion).
     * 
     * @param date a long (time in milliseconds)
     */
    public void setDate(long date) {
        this.date = date;
    }

    public boolean isTimeoutOccur() {
        return timeoutOccur;
    }

    public void setTimeoutOccur(boolean timeoutOccur) {
        this.timeoutOccur = timeoutOccur;
    }

    public int getHintsGiven() {
        return hintsGiven;
    }

    public void setHintsGiven(int hintsGiven) {
        this.hintsGiven = hintsGiven;
    }
}

