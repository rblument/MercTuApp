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

import edu.regis.merc.err.InconsistentDBException;
import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.BloomLevel;
import edu.regis.merc.model.Course;
import edu.regis.merc.model.CourseDigest;
import edu.regis.merc.model.ExercisingLocation;
import edu.regis.merc.model.Hint;
import edu.regis.merc.model.KnowledgeComponent;
import edu.regis.merc.model.OutcomeGranularity;
import edu.regis.merc.model.ProblemType;
import edu.regis.merc.model.Step;
import edu.regis.merc.model.StepSubType;
import edu.regis.merc.model.Task;
import edu.regis.merc.model.TaskKind;
import edu.regis.merc.model.TaskSelectionKind;
import edu.regis.merc.model.Timeout;
import edu.regis.merc.model.Unit;
import edu.regis.merc.model.UnitDigest;
import edu.regis.merc.svc.CourseSvc;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * An XML-based Data Access Object implementing CourseSvc behaviors.
 *
 * @author rickb
 */
public class CourseDAO extends MySqlDAO implements CourseSvc {

    /**
     * Instantiate this Course DAO with default values.
     */
    public CourseDAO() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Course retrieve(int courseId) throws ObjNotFoundException, NonRecoverableException {
        final String sql = "SELECT title AS Title, pedagogy AS PrimaryPedagogy, description AS Description FROM courses WHERE courseId = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, courseId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Course course = new Course(courseId);

                course.setTitle(rs.getString(1));
                course.setPrimaryPedagogy(TaskSelectionKind.valueOf(rs.getString(2).toUpperCase()));
                course.setDescription(rs.getString(3));
                course.setExercisingLocations(retrieveExercisingLocations(courseId, conn));
                course.setUnits(retrieveUnits(course, conn));
                course.setOutcomes(retrieveKnowledgeComponents(course, conn));

                return course;

            } else {
                throw new ObjNotFoundException("Course Id:" + courseId);
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-1" + e.toString(), e);
        } finally {
            close(conn, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CourseDigest retrieveDigest(int courseId, Connection conn)
            throws ObjNotFoundException, NonRecoverableException {

        final String sql = "SELECT title AS Title, description AS Description FROM courses WHERE courseId = ?";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, courseId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                CourseDigest digest = new CourseDigest(courseId);

                digest.setTitle(rs.getString(1));
                digest.setDescription(rs.getString(2));

                return digest;

            } else {
                throw new ObjNotFoundException("Course Id:" + courseId);
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-2" + e.toString(), e);
        } finally {
            close(stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnitDigest retrieveUnitDigest(int courseId, int unitId, Connection conn)
            throws ObjNotFoundException, NonRecoverableException {

        final String sql = "SELECT title AS Title, description AS Description FROM Unit WHERE courseId = ? AND unitId = ?";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, courseId);
            stmt.setInt(2, unitId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UnitDigest digest = new UnitDigest(unitId);

                digest.setTitle(rs.getString(1));
                digest.setDescription(rs.getString(2));

                return digest;

            } else {
                throw new ObjNotFoundException("Unit Id:" + courseId);
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-3" + e.toString(), e);
        } finally {
            close(stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Task retrieveTask(int courseId, int taskId, Connection conn)
            throws ObjNotFoundException, NonRecoverableException {
        final String sql = "SELECT title AS Title, description AS Description, taskType AS Kind, sequenceOrder AS SequenceIndex, exampleType AS ExampleType, NULL AS ProblemId FROM tasks WHERE courseId = ? AND taskId = ?";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, courseId);
            stmt.setInt(2, taskId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Task task = new Task(taskId);
                task.setTitle(rs.getString(1));
                task.setDescription(rs.getString(2));
                task.setKind(TaskKind.valueOf(rs.getString(3)));
                task.setSequenceIndex(rs.getInt(4));

                if (task.getKind() == TaskKind.PROBLEM) {
                    task.setType(ProblemType.valueOf(rs.getString(5)));
                    // ToDo: What about problem id? (this is example type)
                }

                task.setSteps(retrieveSteps(courseId, task.getId(), conn));

                return task;

            } else {
                throw new ObjNotFoundException("Course Id:" + courseId);
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-4" + e.toString(), e);
        } finally {
            close(stmt);
        }
    }

    /**
     * Extract child <Outcome> elements from given XML DOM parent element
     * adding each as a Outcome to the given Course.
     *
     * @param parent an XML DOM Element containing one or more child
     *               <Outcome> node elements.
     * @throws ShaTuException a nonrecoverable exception also see getCause()
     * @return a List of KnowledgeComponent outcomes
     */
    private ArrayList<KnowledgeComponent> retrieveKnowledgeComponents(Course course, Connection conn)
            throws NonRecoverableException {

        final String sql = "SELECT kcId AS Id, title AS Title, description AS Description, bloomLevel AS BloomLevel, isCore AS IsDomainFocus, 'FIXED_SEQUENCE' AS Pedagogy, '' AS ExercisingLocations, 'Course' AS Granularity FROM knowledge_components WHERE courseId = ?";

        ArrayList<KnowledgeComponent> outcomes = new ArrayList<>();

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, course.getId());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                KnowledgeComponent comp = new KnowledgeComponent(rs.getInt(1));
                comp.setTitle(rs.getString(2));
                comp.setDescription(rs.getString(3));
                comp.setBloomLevel(BloomLevel.findValue(rs.getString(4)));
                comp.setIsDomainFocus(rs.getBoolean(5));
                comp.setPedagogy(TaskSelectionKind.findValue(rs.getString(6)));

                comp.setGranularity(OutcomeGranularity.findValue(rs.getString(8)));

                // Link the exercising locations in this outcome to those in the course.
                ArrayList<ExercisingLocation> locations = new ArrayList<>();
                String[] ids = rs.getString(7).split(",");
                for (int i = 0; i < ids.length; i++)
                    locations.add(course.findLocation(i));

                comp.setExercisingLocations(locations);

                outcomes.add(comp);
            }

            return outcomes;

        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-5" + e.toString(), e);
        } finally {
            close(stmt);
        }
    }

    /**
     * Return the units in this course from the <Unit> elements.
     *
     * @param parent a <Course> element
     * @return a List of Units
     * @throws NonRecoverableException
     */
    private ArrayList<Unit> retrieveUnits(Course course, Connection conn) throws NonRecoverableException {
        final String sql = "SELECT unitId AS UnitId, title AS Title, description AS Description, sequenceOrder AS SequenceIndex, pedagogy AS Pedagogy FROM Unit WHERE courseId = ?";

        ArrayList<Unit> units = new ArrayList<>();

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, course.getId());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Unit unit = new Unit(rs.getInt(1));
                unit.setTitle(rs.getString(2));
                unit.setDescription(rs.getString(3));
                unit.setSequenceId(rs.getInt(4));
                unit.setPedagogy(TaskSelectionKind.findValue(rs.getString(5)));
                unit.setTasks(retrieveTasks(course, unit, conn));

                units.add(unit);
            }

            return units;
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-6" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection, retrieve(courseId) will
        }
    }

    /**
     * Extract and return the tasks in the given <Unit> element
     *
     * @param parent <Unit> // ToDo is this always true?
     * @return a Task list.
     */
    private ArrayList<Task> retrieveTasks(Course course, Unit unit, Connection conn)
            throws NonRecoverableException {
        final String sql = "SELECT taskId AS TaskId, title AS Title, description AS Description, taskType AS Kind, sequenceOrder AS SequenceIndex, exampleType AS ExampleType, NULL AS ProblemId FROM tasks WHERE courseId = ? AND unitId = ?";

        ArrayList<Task> tasks = new ArrayList<>();

        PreparedStatement stmt = null;

        int courseId = course.getId();

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, courseId);
            stmt.setInt(2, unit.getId());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Task task = new Task(rs.getInt(1));
                task.setTitle(rs.getString(2));
                task.setDescription(rs.getString(3));
                task.setKind(TaskKind.valueOf(rs.getString(4)));
                task.setSequenceIndex(rs.getInt(5));

                if (task.getKind() == TaskKind.PROBLEM) {
                    task.setType(ProblemType.valueOf(rs.getString(6)));
                    // ToDo: What about problem id? (this is example type)
                }

                tasks.add(task);

                task.setSteps(retrieveSteps(courseId, task.getId(), conn));

                // ToDo: retrieve exercising locations
            }

            return tasks;
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-7" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection, retrieve(courseId) will
        }

    }

    /**
     * Extract and return a list of child steps from the given parent element.
     * 
     * @param courseId
     * @param taskId
     * @param conn     an open connection to the DB, which isn't closed by this
     *                 method.
     * @return a List of Step elements, may be empty.
     */
    private ArrayList<Step> retrieveSteps(int courseId, int taskId, Connection conn)
            throws NonRecoverableException {
        final String sql =
                "SELECT " +
                "  ts.stepId AS Id, " +
                "  ts.title AS Title, " +
                "  ts.description AS Description, " +
                "  ts.sequenceOrder AS SequenceIndex, " +
                "  ts.stepSubtype AS StepSubType, " +
                "  ts.kcId AS SubTypeId, " +
                "  COALESCE((SELECT st.timeoutId FROM step_timeouts st WHERE st.durationSeconds = ts.timeoutSeconds LIMIT 1), (SELECT st2.timeoutId FROM step_timeouts st2 ORDER BY st2.timeoutId LIMIT 1)) AS TimeoutId " +
                "FROM task_steps ts WHERE ts.courseId = ? AND ts.taskId = ?";

        ArrayList<Step> steps = new ArrayList<>();

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, courseId);
            stmt.setInt(2, taskId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StepSubType subType = StepSubType.findValue(rs.getString(5));

                Step step = new Step(rs.getInt(1), rs.getInt(4), subType);

                step.setTitle(rs.getString(1));
                step.setDescription(rs.getString(2));
                step.setTimeout(retrieveTimeout(rs.getInt(7), conn));

                extractStepSubTypeData(subType, rs.getInt(6), conn);

                // ToDo retrieve exercising locations

                steps.add(step);

                step.setHints(retrieveHints(step.getId(), conn));
            }

            return steps;
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-8" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection, retrieve(courseId) will
        }
    }

    /**
     * Extract and return a list of hints from the given parent element.
     * 
     *
     * @param conn an open connection to the DB, which isn't closed by this method.
     * @return an ArrayList of Hint
     */
    private ArrayList<Hint> retrieveHints(int stepId, Connection conn)
            throws NonRecoverableException {

        final String sql = "SELECT hintId AS Id, hintText AS Text, sequenceOrder AS SequenceIndex FROM step_hints WHERE stepId = ?";

        ArrayList<Hint> hints = new ArrayList<>();

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, stepId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Hint hint = new Hint(rs.getInt(1));
                hint.setText(rs.getString(2));
                hint.setSequenceIndex(rs.getInt(3));

                hints.add(hint);
            }

            return hints;

        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-9" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection, retrieve(courseId) will
        }
    }

    /**
     * 
     * @param subType
     * @param subTypeId index into the appropriate table determined by subType
     * @return
     */
    private String extractStepSubTypeData(StepSubType subType, int subTypeId, Connection conn)
            throws NonRecoverableException {
        switch (subType) {
            case INFO_MESSAGE:
                return extractInfoMsgData(subTypeId, conn);          
            case REQUEST_HINT:
                return ""; // TBD
            default:
                return "";
        }
    }

    /**
     * 
     * The data is a POJO String object
     */
    private String extractInfoMsgData(int subTypeId, Connection conn) throws NonRecoverableException {
        final String sql = "SELECT hintText AS Text FROM step_hints WHERE stepId = ? ORDER BY sequenceOrder LIMIT 1";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, subTypeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString(1);
            } else {
                String errMsg = "ERROR: ToDo: Throw database inconsistency InfoMsgStep table: " + subTypeId;
                throw new NonRecoverableException(errMsg, new InconsistentDBException(errMsg));
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-10" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection, retrieve(courseId) will
        }
    }

    private Timeout retrieveTimeout(int timeoutId, Connection conn)
            throws NonRecoverableException {
        final String sql = "SELECT timeoutType AS TimeoutType, durationSeconds AS Seconds, eventAction AS Event, messageText AS Msg FROM step_timeouts WHERE timeoutId = ?";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, timeoutId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Timeout timeout = new Timeout(rs.getString(1), rs.getInt(2), rs.getString(3), rs.getString(4));
                return timeout;
            } else {
                // ToDo: throw a dabase inconsistency error
                String errMsg = "Timeout not found, id: " + timeoutId;
                throw new NonRecoverableException(errMsg, new InconsistentDBException(errMsg));
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-11" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection, retrieve(courseId) will
        }
    }

    /**
     * Extract child <ExercisingLocation> elements from given XML DOM parent
     * element and return the list of these locations.
     *
     * @param parent an XML DOM Element containing one or more child
     *               <ExercisingLocation> node elements.
     * @throws ShaTuException a nonrecoverable exception also see getCause()
     * @return a List of ExercisingElements
     */
    private ArrayList<ExercisingLocation> retrieveExercisingLocations(int courseId, Connection conn)
            throws NonRecoverableException {

        final String sql = "SELECT stepId AS Id, unitId AS UnitId, taskId AS TaskId, stepId AS StepId FROM task_steps WHERE courseId = ?";

        ArrayList<ExercisingLocation> locations = new ArrayList<>();

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, courseId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ExercisingLocation location = new ExercisingLocation(rs.getInt(1));
                location.setCourseId(courseId);
                location.setUnitId(rs.getInt(2));
                location.setTaskId(rs.getInt(3));
                location.setStepId(rs.getInt(4));

                locations.add(location);
            }

            return locations;

        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-12" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection, retrieve(courseId) will
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Task findTaskByStepId(int courseId, int stepId, Connection conn)
            throws ObjNotFoundException, NonRecoverableException {
        final String sql = "SELECT taskId FROM task_steps WHERE courseId = ? AND stepId = ?";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, courseId);
            stmt.setInt(2, stepId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int taskId = rs.getInt(1);
                return retrieveTask(courseId, taskId, conn);
            } else {
                throw new ObjNotFoundException("No task found for stepId:" + stepId);
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-findTaskByStepId" + e.toString(), e);
        } finally {
            close(stmt);
        }
    }
}
