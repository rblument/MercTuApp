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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.regis.merc.err.InconsistentDBException;
import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.*;
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
        final String sql = "SELECT Title,PrimaryPedagogy,Description FROM Course WHERE Id = ?";

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

        final String sql = "SELECT Title,Description FROM Course WHERE Id = ?";

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

        final String sql = "SELECT Title,Description FROM Unit WHERE CourseId = ? AND UnitId = ?";

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
        final String sql = "SELECT Title,Description,Kind,SequenceIndex,ExampleType,ProblemId FROM Task WHERE CourseId = ? AND TaskId = ?";

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
     * Extract child &lt;Outcome> elements from given XML DOM parent element
     * adding each as a Outcome to the given Course.
     *
     * @param parent an XML DOM Element containing one or more child
     *               &lt;Outcome> node elements.
     * @throws ShaTuException a nonrecoverable exception also see getCause()
     * @return a List of KnowledgeComponent outcomes
     */
    private ArrayList<KnowledgeComponent> retrieveKnowledgeComponents(Course course, Connection conn)
            throws NonRecoverableException {

        final String sql = "SELECT Id, Title, Description, BloomLevel, IsDomainFocus, Pedagogy, ExercisingLocations, Granularity FROM KnowledgeComponent WHERE CourseId = ?";

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
     * Return the units in this course from the &lt;Unit> elements.
     * 
     * @param parent a &lt;Course> element
     * @return a List of Units
     * @throws NonRecoverableException
     */
    private ArrayList<Unit> retrieveUnits(Course course, Connection conn) throws NonRecoverableException {
        final String sql = "SELECT UnitId,Title,Description,SequenceIndex,Pedagogy FROM Unit WHERE CourseId = ?";

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
     * Extract and return the tasks in the given &lt;Unit> element
     * 
     * @param parent &lt;Unit> // ToDo is this always true?
     * @return a Task list.
     */
    private ArrayList<Task> retrieveTasks(Course course, Unit unit, Connection conn)
            throws NonRecoverableException {
        final String sql = "SELECT TaskId,Title,Description,Kind,SequenceIndex,ExampleType,ProblemId FROM Task WHERE CourseId = ? AND UnitId = ?";

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
        final String sql = "SELECT Id,Title,Description,SequenceIndex,StepSubType,SubTypeId,TimeoutId FROM Step WHERE CourseId = ? AND TaskId = ?";

        ArrayList<Step> steps = new ArrayList<>();
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, courseId);
            stmt.setInt(2, taskId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StepSubType subType = StepSubType.findValue(rs.getString(5));
                int subTypeId = rs.getInt(6); //capture the specific generic id for the description table

                Step step = new Step(rs.getInt(1), rs.getInt(4), subType);

                step.setTitle(rs.getString(1));
                step.setDescription(rs.getString(2));
                step.setTimeout(retrieveTimeout(rs.getInt(7), conn));

//                extractStepSubTypeData(step, subType, rs, conn);
                extractStepSubTypeData(step, subType, subTypeId, conn); //passing extracted ID, not resultSet

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

        final String sql = "SELECT Id,Text,SequenceIndex FROM Hint WHERE StepId = ?";

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
//    private String extractStepSubTypeData(Step step, StepSubType subType, ResultSet rs, Connection conn)
    private String extractStepSubTypeData(Step step, StepSubType subType, int subTypeId, Connection conn)
            throws NonRecoverableException {
        switch (subType) {
            case INFO_MESSAGE:
                return extractInfoMsgData(subTypeId, conn);          
            case REQUEST_HINT:
                return ""; // TBD
            case TM_DESCRIPTION:
                retrieveTMDescription(step, subTypeId, conn);
                break;
            case LC_DESCRIPTION:
                retrieveLCDescription(step, subTypeId, conn);
                break;
            case MU_DESCRIPTION:
                retrieveMUDescription(step,subTypeId, conn);
                break;
            default:
                return "";
        }
    }

    private void retrieveTMDescription(Step step,int id, Connection conn) throws NonRecoverableException {
        final String sql = "SELECT Id,TuringMachineId,SubType,ComponentId, DataId FROM TMDescription WHERE Id = ?";
        PreparedStatement stmt = null;

        try {
//            int id = rs.getInt("SubTypeId");
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                TMStepSubType tmStepSubType = TMStepSubType.valueOf(rs.getString("SubType"));

                TMDescription tmDescription = new TMDescription(id, TMStepSubType.valueOf(rs.getString("SubType")));
                tmDescription.setTmId(rs.getInt("TuringMachineId"));
                tmDescription.setComponentId(rs.getInt("ComponentId"));
//                tmDescription.setDataId(rs.getInt("DataId")); // uncomment if model has this field

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                step.setData(gson.toJson(tmDescription));
                }
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-8" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection, retrieve(courseId) will
        }


    }

    // ADDED retrieveLCDescription method
    private void retrieveLCDescription(Step step, int id, Connection conn) throws NonRecoverableException {
        final String sql = "SELECT LambdaCalculusId, SubType, ComponentId, DataId FROM LCDescription WHERE Id = ?";
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LCStepSubType lcSubType = LCStepSubType.valueOf(rs.getString("SubType"));

                LCDescription lcDescription = new LCDescription(id, lcSubType);
                lcDescription.setLcID(rs.getInt("LambdaCalculusId"));
                lcDescription.setComponentID(rs.getInt("ComponentId"));

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                step.setData(gson.toJson(lcDescription));
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-LC " + e.toString(), e);
        } finally {
            close(stmt);
        }
    }

    // ADDED retrieveMUDescription method
    private void retrieveMUDescription(Step step, int id, Connection conn) throws NonRecoverableException {
        final String sql = "SELECT MuRecursiveFunctionId, SubType, ComponentId, DataId FROM MUDescription WHERE Id = ?";
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                MUStepSubType muSubType = MUStepSubType.valueOf(rs.getString("SubType"));

                MUDescription muDescription = new MUDescription(id, muSubType);
                muDescription.setMuID(rs.getInt("MuRecursiveFunctionId"));
                muDescription.setComponentID(rs.getInt("ComponentId"));

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                step.setData(gson.toJson(muDescription));
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("CourseDAO-ERR-MU " + e.toString(), e);
        } finally {
            close(stmt);
        }
    }

    /**
     * 
     * The data is a POJO String object
     */
    private String extractInfoMsgData(int subTypeId, Connection conn) throws NonRecoverableException {
        final String sql = "SELECT Text FROM InfoMsgStep WHERE SubStepId = ?";

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
        final String sql = "SELECT TimeoutType,Seconds,Event,Msg FROM Timeout WHERE Id = ?";

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
     * Extract child &lt;ExercisingLocation> elements from given XML DOM parent
     * element and return the list of these locations.
     *
     * @param parent an XML DOM Element containing one or more child
     *               &lt;ExercisingLocation> node elements.
     * @throws ShaTuException a nonrecoverable exception also see getCause()
     * @return a List of ExercisingElements
     */
    private ArrayList<ExercisingLocation> retrieveExercisingLocations(int courseId, Connection conn)
            throws NonRecoverableException {

        final String sql = "SELECT Id, UnitId, TaskId, StepId FROM ExercisingLocation WHERE CourseId = ?";

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
}
