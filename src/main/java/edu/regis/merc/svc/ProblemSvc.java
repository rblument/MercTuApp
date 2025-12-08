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

import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.Problem;
import java.sql.Connection;
import java.util.ArrayList;

/**
 * Specifies the API for Problem life-cycle maintenance (CRUD persistence).
 *
 * @author rickb
 */
public interface ProblemSvc {
    /**
     * Return the problem with the given id.
     * 
     * @param id the id of the Problem to return
     * @return a Problem
     * @throws ObjNotFoundException no Problem with the given id exists.
     * @throws NonRecoverableException perhaps see getCause().getErrorCode().
     */
    Problem retrieve(int id) throws ObjNotFoundException, NonRecoverableException;
    
    /**
     * Return the problem with the given id.
     * 
     * @param id the id of the Problem to return
     * @param conn an open connection to the DB, which is not closed.
     * @return a Problem
     * @throws ObjNotFoundException no Problem with the given id exists.
     * @throws NonRecoverableException perhaps see getCause().getErrorCode().
     */
    Problem retrieve(int id, Connection conn) throws ObjNotFoundException, NonRecoverableException;
    
    /**
     * Retrieve one or more Problems for the Unit with the given unit id.
     * 
     * @param unitId the id of the Unit whose associated Problems are retrieved
     * @return a List of Problems in the given Unit
     * @throws NonRecoverableException perhaps see getCause().getErrorCode().
     */
    ArrayList<Problem> retrieveByUnitId(int unitId) throws NonRecoverableException;  
}
