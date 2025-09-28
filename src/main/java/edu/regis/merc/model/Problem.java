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
 * The primary task that a student is attempting to solve, as part of a unit 
 * within a course. 
 * 
 * In VanLehn's sense, this is a task for the student to complete, but we treat
 * tasks at a finer granularity i.e. as subproblems within the primary problem.
 * The tasks within a problem are derived from the specific nature of the
 * problem.
 * 
 * @author rickb
 */
public class Problem extends TitledModel {

    
    
    private TuringMachine turingMachine;
    
    
    // ToDo:
    //public abstract ArrayList<Task> getTasks();
    
    public Problem() {
        this(DEFAULT_ID);
    }
    
    public Problem(int id) {
        super(id);
  
        
    }

    public TuringMachine getTuringMachine() {
        return turingMachine;
    }

    public void setTuringMachine(TuringMachine turingMachine) {
        this.turingMachine = turingMachine;
    }
    
    

    @Override
    public String toString() {
        return title;
    }
}
