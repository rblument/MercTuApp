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
import edu.regis.merc.model.LCExpression;
import edu.regis.merc.model.LCAbstraction;
import edu.regis.merc.model.LCApplication;
import edu.regis.merc.model.Task;
import edu.regis.merc.model.UnitDigest;
import java.sql.Connection;
/**
 *
 * @author ellis
 */
public interface LCSvc {
    
    /**
     * Retrieve the LC Expression using the given ID.
     * 
     * @param exprId
     * @return the fully loaded LCExpression object tree
     * @throws ObjNotFoundException if the ID does not exist
     * @throws NonRecoverableException if a database error occurs
     */
    LCExpression retrieve(int exprId) throws ObjNotFoundException, NonRecoverableException;
}
