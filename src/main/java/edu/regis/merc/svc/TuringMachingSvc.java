/*
 * MERC^T: Multiple External Representations of Computation Tutor
 *
 *  (C) PK Ewusie-Mensah, All rights reserved
 *
 *  Unauthorized use, duplication or distribution without the authors'
 *  permission is strictly prohibited.
 *
 *  Unless required by applicable law or agreed to in writing, this
 *  software is distributed on an "AS IS" basis without warranties
 *  or conditions of any kind, either expressed or implied.
 */
package edu.regis.merc.svc;

import edu.regis.merc.err.IllegalArgException;
import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.Account;
import edu.regis.merc.model.State;
import edu.regis.merc.model.Transition;
import edu.regis.merc.model.TuringMachine;

import java.lang.module.Configuration;
import java.util.LinkedList;
import java.util.List;

/**
 * Specifies the behaviors provided by the Turing Machine service.
 *
 * This service supports the creation, retrieval, updating, and deletion
 * of Turing Machine components, including machines, states, and transitions.
 * Implementations of this interface manage the persistence and integrity
 * of these components within the MERC^T system.
 *
 * The methods in this interface may throw exceptions such as
 * {@link IllegalArgException}, {@link ObjNotFoundException}, or
 * {@link NonRecoverableException} depending on the implementation and
 * context of use.
 *
 * @author PK Ewusie-Mensah
 */

public interface TuringMachingSvc {



    // -------- Turing Machine --------

    /**
     * Creates a new Turing Machine.
     *
     * @param machine the machine to create
     * @return the unique identifier of the created machine
     */
    int createMachine(TuringMachine machine);

    /**
     * Retrieves a Turing Machine by its identifier.
     *
     * @param id the unique identifier of the machine
     * @return the corresponding {@link TuringMachine}
     */
    TuringMachine getMachineById(int id);

    /**
     * Retrieves all Turing Machines.
     *
     * @return a list of all machines
     */
    List<TuringMachine> getAllMachines();

    /**
     * Deletes a Turing Machine by its identifier.
     *
     * @param id the unique identifier of the machine
     * @return true if the machine was deleted, false otherwise
     */
    boolean deleteMachine(int id);



    // -------- States --------

    /**
     * Creates a new state for a Turing Machine.
     *
     * @param state the state to create
     * @return the unique identifier of the created state
     */
    int createState(State state);

    /**
     * Updates an existing state.
     *
     * @param state the state with updated information
     * @return the unique identifier of the updated state
     */
    int updateState(State state);

    /**
     * Retrieves all states belonging to a specific Turing Machine.
     *
     * @param machineId the unique identifier of the machine
     * @return a list of states associated with the machine
     */
    List<State> getStatesByMachine(int machineId);



    // -------- Transitions --------

    /**
     * Creates a new transition for a Turing Machine.
     *
     * @param transition the transition to create
     * @return the unique identifier of the created transition
     */
    int createTransition(Transition transition);

    /**
     * Retrieves all transitions belonging to a specific Turing Machine.
     *
     * @param machineId the unique identifier of the machine
     * @return a list of transitions associated with the machine
     */
    List<Transition> getTransitionsByMachine(int machineId);

}
