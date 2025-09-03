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
import edu.regis.merc.model.Assessment;
import edu.regis.merc.model.AssessmentLevel;
import edu.regis.merc.model.ScaffoldLevel;
import edu.regis.merc.model.Student;
import edu.regis.merc.model.StudentModel;
import edu.regis.merc.model.StudentModelFieldKind;
import java.util.List;

/**
 * Specifies the API for {@link StudentModel} life-cycle maintenance
 * (CRUD persistence).
 * 
 * @author rickb
 */
public interface StudentModelSvc {
    /**
     * Insert the given {@link Student} and {@link StudentModel} into the database.
     * 
     * @param student the student's email address is used as the student id
     *                (email: user@university.edu)
     * @throws NonRecoverableException perhaps see getCause().getErrorCode()
     */
    void create(Student student) throws NonRecoverableException;
    
    /**
     * Return whether a student with the given user id exists. 
     * 
     * The idea is that this will execute quickly since it avoid loading the
     * student model.
     * 
     * @param userId the student's user id (email: user@university.edu)
     * @return true if the given student exists, otherwise false
     * @throws NonRecoverableException
     */
    boolean exists(String userId) throws NonRecoverableException;
    
    /**
     * Return the {@link Student} with the given user id.
     * 
     * See exists(String) for a faster check as to whether a student exists.
     * 
     * @param userId the student's user id (email: user@university.edu)
     * @return the desired student
     * @throws ObjNotFoundException no student with the give user id exists
     * @throws NonRecoverableException perhaps see getCause().getErrorCode()
     */
    StudentModel retrieve(String userId) throws ObjNotFoundException, NonRecoverableException;
    
    /**
     * Return the {@link StudentModel} for the given user id.
     * 
     * @param userId the student's user id (email: user@university.edu)
     * @return the StudentModel for the Student with the given user id
     * @throws ObjNotFoundException No student with the given user id exists

     * @throws NonRecoverableException perhaps see getCause().getErrorCode()
     */
    StudentModel findModelById(String userId) throws ObjNotFoundException, 
            NonRecoverableException;
    
    /**
     * Delete the student from the database including the student's account,
     * current session, and student model.
     * 
     * @param userId the student's user id (email: user@university.edu)
     * @throws NonRecoverableException 
     */
    void delete(String userId) throws NonRecoverableException;
    
       /**
     * Update the field of the assessment in the given student model.
     * 
     * @param model the student model to update.
     * @param assessment the assessment to update.
     * @param field the field to update, which might be ALL.
     * @throws NonRecoverableException 
     */
    void updateAssessment(StudentModel model, Assessment assessment, StudentModelFieldKind field)
            throws NonRecoverableException;
    
       /**
    * Retrieve a list of unfinished lessons for a student in a specific learning mode.
    * 
    * @param userId the unique identifier for the student.
    * @param learningCategory the category of learning (e.g., "Teach Me", "Practice", "Quiz Me").
    * @return a list of strings representing unfinished lesson names.
    * @throws ObjNotFoundException if the student record is not found.
    * @throws NonRecoverableException if a database error occurs.
    */
   List<String> retrieveIncompleteLessons(String userId, String learningCategory) 
           throws ObjNotFoundException, NonRecoverableException;
   
    /**
     * Retrieves the assessment level for a given lesson.
     *
     * @param userId the user id.
     * @param lesson the lesson title.
     * @return the assessment level.
     * @throws ObjNotFoundException if the assessment record is not found.
     * @throws NonRecoverableException if an error occurs during retrieval.
     */
    AssessmentLevel retrieveAssessmentLevel(String userId, String lesson)
            throws ObjNotFoundException, NonRecoverableException;
    
    /**
     * Updates the scaffold level for the given student.
     *
     * @param userId the unique identifier of the student.
     * @param level the new ScaffoldLevel to be set.
     * @throws NonRecoverableException if a database error occurs.
     * @throws ObjNotFoundException if no student model is found.
     */
    void updateScaffoldLevel(String userId, ScaffoldLevel level)
            throws ObjNotFoundException, NonRecoverableException;
}

