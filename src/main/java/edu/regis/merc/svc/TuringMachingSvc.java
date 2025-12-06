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
import edu.regis.merc.model.State;
import edu.regis.merc.model.TuringMachine;

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
    /**
     * Creates a new Turing Machine.
     *
     * @param machine the machine to create
     * @return the unique identifier of the created machine
     * @throws NonRecoverableException perhaps see getCause().getErrorCode()
     */
    int create(TuringMachine machine) throws NonRecoverableException;

    /**
     * Retrieves a Turing Machine by its identifier from the database.
     *
     * @param id the unique identifier of the machine
     * @return the corresponding {@link TuringMachine}
     * @throws ObjNotFoundException no Turing Machine with the given id exists
     * @throws NonRecoverableException perhaps see getCause().getErrorCode()
     */
    TuringMachine retrieve(int id) throws ObjNotFoundException, NonRecoverableException;

    /**
     * Retrieves all Turing Machines from the database.
     *
     * @return a list of all machines
     * @throws NonRecoverableException perhaps see getCause().getErrorCode()
     */
    List<TuringMachine> retrieveAll() throws NonRecoverableException;

    /**
     * Deletes a Turing Machine by its identifier.
     *
     * @param id the unique identifier of the machine
     * @return true if the machine was deleted, false otherwise
     * @throws ObjNotFoundException no Turing Machine with the given id exists
     * @throws NonRecoverableException perhaps see getCause().getErrorCode()
     */
    boolean delete(int id) throws ObjNotFoundException, NonRecoverableException;

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
}
