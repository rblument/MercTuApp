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
    //private StepSubType subType;
    private StudentActionKind studentAction;

    /**
     * Which step this is in the parent task.
     */
    protected int sequenceIndex = 1;
    
    /**
     * A displayable description providing background information for solving
     * the problem presented by the prompt in this step.
     */
    private String context;
    
    /**
     * A displayable description describing what the student needs to perform
     * in this step.
     */
    private String prompt;
    
    private ViewConfiguration viewConfiguration;

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
     * Instantiate this step with the given information.
     * 
     * @param id
     * @param sequenceId
     * @param subType
     */
    public Step(int id) {
        super(id);

        timeout = null;

        exercisedComponentIds = new ArrayList<>();

        hints = new ArrayList<>();

        //this.subType = subType;

    }

    public StudentActionKind getStudentAction() {
        return studentAction;
    }

    public void setStudentAction(StudentActionKind studentAction) {
        this.studentAction = studentAction;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public ViewConfiguration getViewConfiguration() {
        return viewConfiguration;
    }

    public void setViewConfiguration(ViewConfiguration viewConfiguration) {
        this.viewConfiguration = viewConfiguration;
    }    

  //  public StepSubType getSubType() {
   //     return subType;
   // }

  //  public void setSubType(StepSubType subType) {
   //     System.out.println("Sub type set: " + subType); // Error checking

   //     this.subType = subType;
   // }

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
    
    public void setSequenceIndex(int seqeunceIndex) {
       this.sequenceIndex = sequenceIndex;
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
}

