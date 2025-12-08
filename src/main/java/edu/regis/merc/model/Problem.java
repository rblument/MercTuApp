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
 * The primary "task" that a student is attempting to solve, which consists of
 * one or more complex, multi-step tasks.
 * 
 * In VanLehn's sense, this is a task for the student to complete, but we treat
 * tasks at a finer granularity i.e. as "sub-problems" within this primary problem.
 * 
 * @author rickb
 */
public class Problem extends TitledModel {
    /**
     * The Tasks that a Student must complete to solve this Problem.
     */
    private ArrayList<Task> tasks;
    
    /**
     * The Turing Machine, if any, associated with this problem.
     */
    private TuringMachine turingMachine;
    
    /**
     * The Lambda Calculus expression, if any, associated with this problem.
     */
    private LCExpression expression;
    
    /**
     * The Mu Function expression, if any, associated with this problem.
     */
    private MuFunction muFunction;
    
    /**
     * The id of the unit in which this Problem resides.
     */
    private int unitId;
    
    /**
     * The position of this problem in the unit's sequence of problems 
     */
    private int sequenceIndex;
    
    public Problem() {
        this(DEFAULT_ID);
    }
    
    public Problem(int id) {
        super(id);
        
        tasks = new ArrayList<>();
    } 
    
    public void addTask(Task task) {
        tasks.add(task);
    }
    
    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }
    
     public Task findTaskById(int taskId) {
        for (Task task : tasks)
            if (task.getId() == taskId)
                return task;
        
        return null;
    }

    /**
     * Return the task, if any, with the given sequence id.
     * 
     * @param sequence the sequence position of the task to find.
     * @return a Task, or null if not found.
     */
    public Task findTaskBySequence(int sequence) {
        for (Task task : tasks)
            if (task.getSequenceIndex() == sequence)
                return task;
        
        return null;
    }
    
    public TuringMachine getTuringMachine() {
        return turingMachine;
    }

    public void setTuringMachine(TuringMachine turingMachine) {
        this.turingMachine = turingMachine;
    }

    public LCExpression getExpression() {
        return expression;
    }

    public void setExpression(LCExpression expression) {
        this.expression = expression;
    }

    public MuFunction getMuFunction() {
        return muFunction;
    }

    public void setMuFunction(MuFunction muFunction) {
        this.muFunction = muFunction;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public int getSequenceIndex() {
        return sequenceIndex;
    }

    public void setSequenceIndex(int sequenceIndex) {
        this.sequenceIndex = sequenceIndex;
    }

    @Override
    public String toString() {
        return title;
    }
}
