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
 * A user interface gesture/event performed by a student, as part of a Task.
 *
 * Steps in tasks are based on tutoring behaviors in (VanLehn, 2006).
 * 
 * As a Step appears within actions/expectations created by an agent,
 * all fields are final so that other malicious agents cannot change them.
 *
 * @author rickb
 */
public class Step extends TitledModel {
    /**
     * The type of this step, which determines the object specified in this
     * step's data along with a potential cont
     */
    private StepSubType subType;
    /**
     * Which step this is in the parent task.
     */
    protected int sequenceIndex = 1;

    /**
     * The hints, if any, associated with this step (in order to be given).
     */
    protected ArrayList<Hint> hints;

    /**
     * The amount of seconds the student can take on this step before the GUI
     * prompts the student concerning their inaction.
     */
    protected Timeout timeout;

    /**
     * The knowledge component outcomes demonstrated/exercised by this step.
     */
    protected ArrayList<Integer> exercisedComponentIds;

    /**
     * A JSon encoded object specified by out step subtype, which provides the
     * contextual environment (information) related to this step.
     */
    private String data;

    /**
     * Current question for student to answer.
     */
    public String question;

    /**
     * Correct answer to the presented question. Checked against userAnswer
     */
    private String correctAnswer;

    /**
     * Default JSON state for this step (nullable)
     */
    private String defaultState;

    /**
     * Correct JSON state for this step (nullable)
     */
    private String correctState;

    /**
     * Level required/demonstrated by this step
     */
    private int level = 1;

    /**
     * Instantiate this step with the given information.
     * 
     * @param id database id of this step
     * @param sequenceIndex which position this step has in the parent task
     * @param subType the subtype describing this step's data/behavior
     */
    public Step(int id, int sequenceIndex, StepSubType subType) {
        super(id);

        this.sequenceIndex = sequenceIndex;

        timeout = null;

        exercisedComponentIds = new ArrayList<>();

        hints = new ArrayList<>();

        this.subType = subType;

    }

    public StepSubType getSubType() {
        return subType;
    }

    public void setSubType(StepSubType subType) {
        System.out.println("Sub type set: " + subType); // Error checking

        this.subType = subType;
    }

    public ArrayList<Hint> getHints() {
        return hints;
    }

    public Hint findHintById(int id) {
        for (int i = 0; i < hints.size(); i++)
            if (hints.get(i).getId() == id)
                return hints.get(i);

        return null;
    }

    public void setHints(ArrayList<Hint> hints) {
        this.hints = hints;
    }

    public int getSequenceIndex() {
        return sequenceIndex;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public void addHint(Hint hint) {
        System.out.println("Adding hint");
        hints.add(hint);
    }

    public void addExercisedComponentId(int componentId) {
        exercisedComponentIds.add(componentId);
    }

    public ArrayList<Integer> getExercisedComponentIds() {
        return exercisedComponentIds;
    }

    public void setExercisedComponentIds(ArrayList<Integer> componentIds) {
        this.exercisedComponentIds = componentIds;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * Setter method for the question
     * 
     * @param question
     */
    public void setQuestion(String question) {
        this.question = question;
    }

    /**
     * Getter method for the question
     * 
     * @return
     */
    public String getQuestion() {
        return this.question;
    }

    /**
     * Setter method for the correct answer
     * 
     * @param newResult
     */
    public void setResult(String newResult) {
        this.correctAnswer = newResult;
    }

    /**
     * Getter method for the correct answer
     * 
     * @return
     */
    public String getResult() {
        return this.correctAnswer;
    }

    // New accessors for defaultState, correctState, and level
    public String getDefaultState() {
        return defaultState;
    }

    public void setDefaultState(String defaultState) {
        this.defaultState = defaultState;
    }

    public String getCorrectState() {
        return correctState;
    }

    public void setCorrectState(String correctState) {
        this.correctState = correctState;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /*
     * Setter method for the user's answer
     *
     * @param userResponse
     */
   // public void setUserAnswer(String userResponse) {
   //     this.userAnswer = userResponse;
   // }

    /*
     * Getter method for the user's answer
     *
     * @return
     */
   // public String getUserAnswer() {
   //     return this.userAnswer;
   // }
}

