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
package edu.regis.merc.dao;

import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.Assessment;
import edu.regis.merc.model.AssessmentLevel;
import edu.regis.merc.model.Course;
import edu.regis.merc.model.KnowledgeComponent;
import edu.regis.merc.model.ScaffoldLevel;
import edu.regis.merc.model.Student;
import edu.regis.merc.model.StudentModel;
import edu.regis.merc.model.StudentModelFieldKind;
import edu.regis.merc.svc.CourseSvc;
import edu.regis.merc.svc.ServiceFactory;
import edu.regis.merc.svc.StudentModelSvc;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentModelDAO extends MySqlDAO implements StudentModelSvc {

    public StudentModelDAO() {
        super();
    }

    /**
     * Creates a new student model and its associated assessment records.
     *
     * @param student the student for whom the model is created.
     * @throws NonRecoverableException if an error occurs during creation.
     */
    @Override
    public void create(Student student) throws NonRecoverableException {
        final String sql1 = "INSERT INTO StudentModel (UserId, ScaffoldLevel) VALUES (?,?)";
        final String sql2 = "INSERT INTO Assessment (UserId, KnowledgeComponentId, AssessmentLevel, Exposures, Successes, Hints) VALUES (?,?,?,?,?,?)";
        String userId = student.getAccount().getUserId();
        StudentModel studentModel = student.getStudentModel();

        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        try {
            conn = DriverManager.getConnection(URL);
            stmt1 = conn.prepareStatement(sql1);

            stmt1.setString(1, userId);
            stmt1.setString(2, ScaffoldLevel.EXTREME.toString());
            stmt1.executeUpdate();

            stmt2 = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS);
            for (Assessment assessment : studentModel.getAssessments().values()) {
                stmt2.setString(1, userId);
                stmt2.setInt(2, assessment.getOutcome().getId());
                stmt2.setString(3, "Not Started");
                stmt2.setInt(4, assessment.getExposures());
                stmt2.setInt(5, assessment.getSuccessess());
                stmt2.setInt(6, assessment.getHints());
                stmt2.executeUpdate();

                ResultSet rs = stmt2.getGeneratedKeys();
                if (rs.next()) {
                    assessment.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("UserDAO-ERR-5" + e.toString(), e);
        } finally {
            close(stmt2);
            close(conn, stmt1);
        }
    }

    /**
     * Retrieves the student model along with its assessment records.
     *
     * @param userId the user id.
     * @return the student model.
     * @throws ObjNotFoundException if the student model is not found.
     * @throws NonRecoverableException if an error occurs during retrieval.
     */
    @Override
    public StudentModel retrieve(String userId) throws ObjNotFoundException, NonRecoverableException {
        final String sql = "SELECT ScaffoldLevel FROM StudentModel WHERE UserId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                StudentModel studentModel = new StudentModel(userId);
                studentModel.setScaffoldLevel(ScaffoldLevel.fromString(rs.getString(1)));
                for (Assessment assessment : retrieveAssessments(userId, conn)) {
                    studentModel.addAssessment(assessment);
                }
                return studentModel;
            } else {
                throw new ObjNotFoundException("Student Id:" + userId);
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("UserDAO-ERR-5" + e.toString(), e);
        } finally {
            close(conn, stmt);
        }
    }

    /**
     * Updates a specific field in an assessment.
     *
     * @param model the student model.
     * @param assessment the assessment to update.
     * @param field the field to update.
     * @throws NonRecoverableException if an error occurs during the update.
     */
    @Override
    public void updateAssessment(StudentModel model, Assessment assessment, StudentModelFieldKind field)
            throws NonRecoverableException {
        String sql = "";
        int assessmentId = assessment.getId();

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DriverManager.getConnection(URL);
            switch (field) {
                case ASSESSMENT_LEVEL:
                    sql = "UPDATE Assessment SET AssessmentLevel = ? WHERE KnowledgeComponentId = ? ";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, assessment.getAssessment().title());
                    break;
                case ATTEMPTS:
                    sql = "UPDATE Assessment SET Exposures = ? WHERE KnowledgeComponentId = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, assessment.getExposures());
                    break;
                case SUCCESSES:
                    sql = "UPDATE Assessment SET Successes = ? WHERE KnowledgeComponentId = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, assessment.getSuccessess());
                    break;
                case HINTS:
                    sql = "UPDATE Assessment SET Hints = ? WHERE KnowledgeComponentId = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, assessment.getHints());
                    break;
                default:
                    break;
            }
            stmt.setInt(2, assessmentId);
            stmt.execute();
        } catch (SQLException e) {
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("SQL Error Code: " + e.getErrorCode());
            throw new NonRecoverableException("UserDAO-ERR-5" + e.toString(), e);
        } finally {
            close(conn, stmt);
        }
    }

    /**
     * Retrieves a list of unfinished lessons for a student in a specific learning
     * category.
     * 
     * The category is inferred from `AssessmentLevel`:
     * - "Not Started" → Teach Me
     * - "In Progress" → Practice
     * - "Completed", "Very Low", "Low", "Medium", "High", "Very High" → Quiz Me
     *
     * If a lesson is not yet completed in a **previous category**, it will indicate
     * that.
     *
     * @param userId           The unique identifier of the student.
     * @param learningCategory The learning category ("Teach Me", "Practice", "Quiz
     *                         Me").
     * @return A list of unfinished lesson names, formatted accordingly.
     * @throws ObjNotFoundException    If the student record is not found.
     * @throws NonRecoverableException If a database error occurs.
     */
    @Override
    public List<String> retrieveIncompleteLessons(String userId, String learningCategory)
            throws ObjNotFoundException, NonRecoverableException {

        List<String> lessons = new ArrayList<>();

        // SQL Query: Retrieve all lessons and their assessment levels
        final String sql = """
                    SELECT kc.Title, a.AssessmentLevel
                    FROM Assessment a
                    JOIN KnowledgeComponent kc ON a.KnowledgeComponentId = kc.Id
                    WHERE a.UserId = ?
                """;

        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String lessonTitle = rs.getString("Title");
                    String assessmentLevel = rs.getString("AssessmentLevel");

                    // Determine category based on AssessmentLevel
                    switch (AssessmentLevel.fromString(assessmentLevel)) {
                        case NOT_STARTED:
                            if (learningCategory.equalsIgnoreCase("Teach Me")) {
                                lessons.add(lessonTitle);
                            } else {
                                // If user is in "Practice" or "Quiz Me" but hasn't done Teach Me
                                lessons.add(lessonTitle + " (Complete in Teach Me first)");
                            }
                            break;
                        case IN_PROGRESS:
                            if (learningCategory.equalsIgnoreCase("Practice")) {
                                lessons.add(lessonTitle);
                            } else {
                                // If user is in "Quiz Me" but hasn't completed Practice
                                lessons.add(lessonTitle + " (Complete in Practice first)");
                            }
                            break;
                        case COMPLETED:
                        case VERY_LOW:
                        case LOW:
                        case MEDIUM:
                        case HIGH:
                        case VERY_HIGH:
                            if (learningCategory.equalsIgnoreCase("Quiz Me")) {
                                lessons.add(lessonTitle);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("Error retrieving incomplete lessons: " + e, e);
        }

        // If no lessons are found, return a single message to indicate completion
        if (lessons.isEmpty()) {
            lessons.add("All lessons completed!");
        }

        return lessons;
    }

    /**
     * Retrieves all lesson titles (excluding lessons with IDs 0, 10, and 20).
     *
     * @param userId the user id.
     * @return a list of all lesson titles.
     * @throws NonRecoverableException if an error occurs during retrieval.
     */
    //@Override
    public List<String> retrieveAllLessons(String userId) throws NonRecoverableException {
        List<String> lessons = new ArrayList<>();
        final String sql = "SELECT kc.Title " +
                           "FROM Assessment a " +
                           "JOIN KnowledgeComponent kc ON a.KnowledgeComponentId = kc.Id " +
                           "WHERE a.UserId = ? AND kc.Id NOT IN (0, 10, 20) " +
                           "ORDER BY kc.Id";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lessons.add(rs.getString("Title"));
                }
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("Error retrieving all lessons: " + e.toString(), e);
        }
        return lessons;
    }

    /**
     * Retrieves the assessment level for a given lesson.
     *
     * @param userId the user id.
     * @param lesson the lesson title.
     * @return the assessment level.
     * @throws ObjNotFoundException if the assessment record is not found.
     * @throws NonRecoverableException if an error occurs during retrieval.
     */
    @Override
    public AssessmentLevel retrieveAssessmentLevel(String userId, String lesson)
            throws ObjNotFoundException, NonRecoverableException {
        final String sql = "SELECT a.AssessmentLevel " +
                           "FROM Assessment a " +
                           "JOIN KnowledgeComponent kc ON a.KnowledgeComponentId = kc.Id " +
                           "WHERE a.UserId = ? AND kc.Title = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, lesson);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String levelStr = rs.getString("AssessmentLevel");
                    AssessmentLevel level = AssessmentLevel.fromString(levelStr);
                    if (level != null) {
                        return level;
                    } else {
                        throw new ObjNotFoundException("Invalid AssessmentLevel for lesson: " + lesson);
                    }
                } else {
                    throw new ObjNotFoundException("No assessment record found for lesson: " + lesson);
                }
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("Error retrieving assessment level: " + e.toString(), e);
        }
    }

     @Override
    public void updateScaffoldLevel(String userId, ScaffoldLevel level)
            throws ObjNotFoundException, NonRecoverableException {
        final String sql = "UPDATE StudentModel SET ScaffoldLevel = ? WHERE UserId = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setString(1, level.toString());
             stmt.setString(2, userId);
             int rows = stmt.executeUpdate();
             if (rows == 0) {
                 throw new ObjNotFoundException("No student model found for user: " + userId);
             }
        } catch (SQLException e) {
             throw new NonRecoverableException("Error updating scaffold level: " + e.toString(), e);
        }
    }
    
    /**
     * Retrive
     * 
     * @param userId
     * @param conn
     * @return
     * @throws ObjNotFoundException
     * @throws SQLException
     * @throws NonRecoverableException
     */
    private ArrayList<Assessment> retrieveAssessments(String userId, Connection conn)
            throws ObjNotFoundException, SQLException, NonRecoverableException {

        final String sql = "SELECT Id,KnowledgeComponentId,AssessmentLevel,Exposures,Successes,Hints FROM Assessment WHERE UserId = ?";

        CourseSvc courseSvc = ServiceFactory.findCourseSvc();
        Course course = courseSvc.retrieve(1); // Note only one course possible.

        ArrayList<Assessment> assessments = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, userId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            int assessmentId = rs.getInt(1);
            int knowledgeComponentId = rs.getInt(2);
            KnowledgeComponent outcome = course.findKnowledgeComponent(knowledgeComponentId);
            AssessmentLevel level = AssessmentLevel.fromString(rs.getString(3));
            Assessment assessment = new Assessment(outcome, level);
            assessment.setId(knowledgeComponentId);
            assessment.setExposures(rs.getInt(4));
            assessment.setSuccessess(rs.getInt(5));
            assessment.setHints(rs.getInt(6));
            assessments.add(assessment);
        }
        return assessments;
    }

    @Override
    public boolean exists(String userId) throws NonRecoverableException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public StudentModel findModelById(String userId) throws ObjNotFoundException, NonRecoverableException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void delete(String userId) throws NonRecoverableException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}

