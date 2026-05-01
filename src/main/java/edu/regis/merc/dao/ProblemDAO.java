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

import static edu.regis.merc.dao.MySqlDAO.URL;
import edu.regis.merc.err.InconsistentDBException;
import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.Hint;
import edu.regis.merc.model.LCViewConfiguration;
import edu.regis.merc.model.MuViewConfiguration;
import edu.regis.merc.model.Problem;
import edu.regis.merc.model.Step;
import edu.regis.merc.model.StudentActionKind;
import edu.regis.merc.model.Task;
import edu.regis.merc.model.Timeout;
import edu.regis.merc.model.TmViewConfiguration;
import edu.regis.merc.model.TuringMachine;
import edu.regis.merc.model.ViewConfiguration;
import edu.regis.merc.model.LCExpression;
import edu.regis.merc.svc.ProblemSvc;
import edu.regis.merc.svc.ServiceFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A Data Access Object implementing {@link ProblemSvc} life-cycle behaviors.
 *
 * @author rickb
 */
public class ProblemDAO extends MySqlDAO implements ProblemSvc {

    /**
     * {@inheritDoc}
     */
    @Override
    public Problem retrieve(int id) throws ObjNotFoundException, NonRecoverableException {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(URL);

            return retrieve(id, conn);

        } catch (SQLException e) {
            throw new NonRecoverableException("ProblemDAO-ERR-1" + e.toString(), e);
        } finally {
            close(conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<Problem> retrieveByUnitId(int unitId) throws NonRecoverableException {
        final String sql = "SELECT Id FROM Problem WHERE UnitId = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        ArrayList<Problem> problems = new ArrayList<>();

        int problemId = -1; // Here for better error messages

        try {
            conn = DriverManager.getConnection(URL);

            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, unitId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                problemId = rs.getInt("Id");
                problems.add(retrieve(problemId, conn));
            }

            return problems;

        } catch (ObjNotFoundException e) {
            InconsistentDBException ex = new InconsistentDBException(
                    "Problem not found in unit " + unitId + " for problem id: " + problemId);
            throw new NonRecoverableException("InconsistentDB see internal exception", ex);
        } catch (SQLException e) {
            throw new NonRecoverableException("ProblemDAO-ERR-2" + e.toString(), e);
        } finally {
            close(conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Problem retrieve(int id, Connection conn) throws ObjNotFoundException, NonRecoverableException {
        final String sql = "SELECT Title,Description,UnitId,SequenceIndex,TuringMachineId,LambdaCalculusId,MuRecursiveFunctionId FROM Problem WHERE Id = ?";

        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Problem problem = new Problem(id);

                problem.setTitle(rs.getString("Title"));
                problem.setDescription(rs.getString("Description"));
                problem.setUnitId(rs.getInt("UnitId"));
                problem.setSequenceIndex(rs.getInt("SequenceIndex"));

                problem.setTasks(retrieveTasks(id, conn));

                int tmId = rs.getInt("TuringMachineId");

                // if statement to allow a log in when a problem's id is null
                if (tmId > 0) {
                    try {
                        TuringMachine tm = ServiceFactory.findTuringMachineSvc().retrieve(tmId);
                        System.out.println("Found TM: " + tm);
                        problem.setTuringMachine(tm);

                    } catch (ObjNotFoundException ex) {
                        throw new NonRecoverableException("Inconsistent DB TmId in Problem wasn't in the DB" + tmId);
                    }
                }

                int lcId = rs.getInt("LambdaCalculusId");
                if (lcId > 0) {
                    try {
                        LCExpression expr = ServiceFactory.findLCSvc().retrieve(lcId);
                        System.out.println("Found LC Expression: " + expr); // Debug log
                        problem.setExpression(expr);
                    } catch (ObjNotFoundException ex) {
                        // Log it but don't crash if the equation is missing
                        System.out
                                .println("Warning: LC Expression " + lcId + " listed in Problem but not found in DB.");
                    }
                }

                return problem;
            } else {
                throw new ObjNotFoundException("No problem with given id exists: " + id);
            }

        } catch (SQLException e) {
            throw new NonRecoverableException("ProblemDAO-ERR-3" + e.toString(), e);
        } finally {
            close(stmt);
        }
    }

    /**
     * Extract and return the tasks associated with the given problem (id).
     *
     * @param id the id of the problem whose tasks are returned.
     * @return a Task list.
     */
    private ArrayList<Task> retrieveTasks(int problemId, Connection conn)
            throws NonRecoverableException {
        final String sql = "SELECT Id,Title,Description,SequenceIndex,ExercisedComponentId FROM Task WHERE ProblemId = ?";

        ArrayList<Task> tasks = new ArrayList<>();

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, problemId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Task task = new Task(rs.getInt("Id"));
                task.setTitle(rs.getString("Title"));
                task.setDescription(rs.getString("Description"));
                task.setSequenceIndex(rs.getInt("SequenceIndex"));

                // ToDo: retrieve the ExercisedComponent
                tasks.add(task);

                task.setSteps(retrieveSteps(task.getId(), conn));

            }

            return tasks;
        } catch (SQLException e) {
            throw new NonRecoverableException("ProblemDAO-ERR-4" + e.toString(), e);
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
    private ArrayList<Step> retrieveSteps(int taskId, Connection conn)
            throws NonRecoverableException {
        final String sql = "SELECT Id,Title,Description,SequenceIndex,Context,Prompt,ExercisedComponentId,ViewConfigId,StudentAction,ActionId,TimeoutId FROM Step WHERE TaskId = ?";

        ArrayList<Step> steps = new ArrayList<>();
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, taskId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Step step = new Step(rs.getInt("Id"));

                step.setTitle(rs.getString("Title"));
                step.setDescription(rs.getString("Description"));
                step.setSequenceIndex(rs.getInt("SequenceIndex"));
                step.setContext(rs.getString("Context"));
                step.setPrompt(rs.getString("Prompt"));
                step.setStudentAction(StudentActionKind.valueOf(rs.getString("StudentAction")));

                step.setViewConfiguration(retrieveViewConfiguration(rs.getInt("ViewConfigId"), conn));

                // ToDo: ActionId
                step.setHints(retrieveHints(step.getId(), conn));
                step.setTimeout(retrieveTimeout(rs.getInt("TimeoutId"), conn));

                // extractStepSubTypeData(step, subType, subTypeId, conn); //passing extracted
                // ID, not resultSet
                // ToDo retrieve exercising locations
                steps.add(step);

            }

            return steps;
        } catch (SQLException e) {
            throw new NonRecoverableException("ProblemDAO-ERR-5" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection, retrieve(courseId) will
        }
    }

    /**
     * Extract and return a list of hints from the given parent element.
     *
     *
     * @param conn an open connection to the DB, which isn't closed by this
     *             method.
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
            throw new NonRecoverableException("ProblemDAO-ERR-6" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection, retrieve(courseId) will
        }
    }

    private ViewConfiguration retrieveViewConfiguration(int viewConfigId, Connection conn)
            throws InconsistentDBException, NonRecoverableException {
        final String sql = "SELECT TmViewConfigId,LCViewConfigId,MuViewConfigId FROM ViewConfiguration WHERE Id = ?";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, viewConfigId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ViewConfiguration viewConfiguration = new ViewConfiguration(viewConfigId);

                viewConfiguration
                        .setTmViewConfiguration(retrieveTmViewConfiguration(rs.getInt("TmViewConfigId"), conn));
                viewConfiguration
                        .setLcViewConfiguration(retrieveLcViewConfiguration(rs.getInt("LCViewConfigId"), conn));
                viewConfiguration
                        .setMuViewConfiguration(retrieveMuViewConfiguration(rs.getInt("MuViewConfigId"), conn));

                return viewConfiguration;

            } else {
                throw new InconsistentDBException("ProblemDAO-Err-10, ViewConfiguration not found: " + viewConfigId);
            }

        } catch (SQLException e) {
            throw new NonRecoverableException("ProblemDAO-ERR-11" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection
        }
    }

    private TmViewConfiguration retrieveTmViewConfiguration(int tmViewConfigId, Connection conn)
            throws InconsistentDBException, NonRecoverableException {
        final String sql = "SELECT StateIds,TransitionIds,TapeCellIds,AcceptStateIndicatorIds,RejectStateIndicatorIds,DisplayStartIndicator,DisplayTapeHead FROM TmViewConfiguration WHERE Id = ?";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, tmViewConfigId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                TmViewConfiguration tmViewConfig = new TmViewConfiguration(tmViewConfigId);

                tmViewConfig.setStateIds(convertIds(rs.getString("StateIds")));
                tmViewConfig.setTransitionIds(convertIds(rs.getString("TransitionIds")));
                tmViewConfig.setTapeCellIds(convertIds(rs.getString("TapeCellIds")));
                tmViewConfig.setAcceptStateIndicatorIds(convertIds(rs.getString("AcceptStateIndicatorIds")));
                tmViewConfig.setRejectStateIndicatorIds(convertIds(rs.getString("RejectStateIndicatorIds")));
                tmViewConfig.setDisplayStartStateIndicator(rs.getBoolean("DisplayStartIndicator"));
                tmViewConfig.setDisplayTapeHead(rs.getBoolean("DisplayTapeHead"));

                return tmViewConfig;

            } else {
                throw new InconsistentDBException(
                        "ProblemDAO-Err-20, TmViewConfiguration not found: " + tmViewConfigId);
            }

        } catch (SQLException e) {
            throw new NonRecoverableException("ProblemDAO-ERR-21" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection
        }
    }

    private LCViewConfiguration retrieveLcViewConfiguration(int lcViewConfigId, Connection conn)
            throws InconsistentDBException, NonRecoverableException {
        final String sql = "SELECT ParameterIds,BodyIds,ArgumentIds FROM LCViewConfiguration WHERE Id = ?";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, lcViewConfigId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LCViewConfiguration lcViewConfig = new LCViewConfiguration(lcViewConfigId);

                // Setting LC data
                lcViewConfig.setParameterIds((rs.getString("ParameterIds")));
                lcViewConfig.setBodyIds((rs.getString("BodyIds")));
                lcViewConfig.setArgumentIds((rs.getString("ArgumentIds")));

                return lcViewConfig;

            } else {
                throw new InconsistentDBException(
                        "ProblemDAO-Err-40, LcViewConfiguration not found: " + lcViewConfigId);
            }

        } catch (SQLException e) {
            throw new NonRecoverableException("ProblemDAO-ERR-41" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection
        }
    }

    private MuViewConfiguration retrieveMuViewConfiguration(int muViewConfigId, Connection conn)
            throws InconsistentDBException, NonRecoverableException {
        final String sql = "SELECT HighlightName,ParameterIds,RhsIds,ArgumentIds FROM MuViewConfiguration WHERE Id = ?";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, muViewConfigId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                MuViewConfiguration muViewConfig = new MuViewConfiguration(muViewConfigId);

                return muViewConfig;

            } else {
                throw new InconsistentDBException(
                        "ProblemDAO-Err-50, MuViewConfiguration not found: " + muViewConfigId);
            }

        } catch (SQLException e) {
            throw new NonRecoverableException("ProblemDAO-ERR-51" + e.toString(), e);
        } finally {
            close(stmt); // Don't close the connection
        }
    }

    /**
     *
     * @param subType
     * @param subTypeId index into the appropriate table determined by subType
     * @return
     */
    /*
     * private void extractStepSubTypeData(Step step, StepSubType subType, int
     * subTypeId, Connection conn)
     * throws NonRecoverableException {
     * switch (subType) {
     * case INFO_MESSAGE:
     * //ToDo: the returned data doesn't appear to be used.
     * extractInfoMsgData(subTypeId, conn);
     * case REQUEST_HINT:
     * // ToDo:
     * break;
     * case DISPLAY_ALL:
     * retrieveTMDescription(step, subTypeId, conn);
     * retrieveLCDescription(step, subTypeId, conn);
     * retrieveMUDescription(step,subTypeId, conn);
     * break;
     * case TM_DESCRIPTION:
     * retrieveTMDescription(step, subTypeId, conn);
     * break;
     * case LC_DESCRIPTION:
     * retrieveLCDescription(step, subTypeId, conn);
     * break;
     * case MU_DESCRIPTION:
     * retrieveMUDescription(step,subTypeId, conn);
     * break;
     * default:
     * System.out.println("Why HERE in extractStepSubTypeData");
     * }
     * }
     * 
     * private void retrieveTMDescription(Step step,int id, Connection conn) throws
     * NonRecoverableException {
     * final String sql =
     * "SELECT Id,TuringMachineId,SubType,ComponentId, DataId FROM TMDescription WHERE Id = ?"
     * ;
     * PreparedStatement stmt = null;
     * 
     * try {
     * // int id = rs.getInt("SubTypeId");
     * stmt = conn.prepareStatement(sql);
     * stmt.setInt(1, id);
     * ResultSet rs = stmt.executeQuery();
     * 
     * if (rs.next()) {
     * TMStepSubType tmStepSubType = TMStepSubType.valueOf(rs.getString("SubType"));
     * 
     * TMDescription tmDescription = new TMDescription(id,
     * TMStepSubType.valueOf(rs.getString("SubType")));
     * tmDescription.setTmId(rs.getInt("TuringMachineId"));
     * tmDescription.setComponentId(rs.getInt("ComponentId"));
     * // tmDescription.setDataId(rs.getInt("DataId")); // uncomment if model has
     * this field
     * 
     * Gson gson = new GsonBuilder().setPrettyPrinting().create();
     * step.setData(gson.toJson(tmDescription));
     * }
     * } catch (SQLException e) {
     * throw new NonRecoverableException("CourseDAO-ERR-8" + e.toString(), e);
     * } finally {
     * close(stmt); // Don't close the connection, retrieve(courseId) will
     * }
     * 
     * 
     * }
     * 
     * // ADDED retrieveLCDescription method
     * private void retrieveLCDescription(Step step, int id, Connection conn) throws
     * NonRecoverableException {
     * final String sql =
     * "SELECT LambdaCalculusId, SubType, ComponentId, DataId FROM LCDescription WHERE Id = ?"
     * ;
     * PreparedStatement stmt = null;
     * 
     * try {
     * stmt = conn.prepareStatement(sql);
     * stmt.setInt(1, id);
     * ResultSet rs = stmt.executeQuery();
     * 
     * if (rs.next()) {
     * LCStepSubType lcSubType = LCStepSubType.valueOf(rs.getString("SubType"));
     * 
     * LCDescription lcDescription = new LCDescription(id, lcSubType);
     * lcDescription.setLcID(rs.getInt("LambdaCalculusId"));
     * lcDescription.setComponentID(rs.getInt("ComponentId"));
     * 
     * Gson gson = new GsonBuilder().setPrettyPrinting().create();
     * step.setData(gson.toJson(lcDescription));
     * }
     * } catch (SQLException e) {
     * throw new NonRecoverableException("CourseDAO-ERR-LC " + e.toString(), e);
     * } finally {
     * close(stmt);
     * }
     * }
     * 
     * // ADDED retrieveMUDescription method
     * private void retrieveMUDescription(Step step, int id, Connection conn) throws
     * NonRecoverableException {
     * final String sql =
     * "SELECT MuRecursiveFunctionId, SubType, ComponentId, DataId FROM MUDescription WHERE Id = ?"
     * ;
     * PreparedStatement stmt = null;
     * 
     * try {
     * stmt = conn.prepareStatement(sql);
     * stmt.setInt(1, id);
     * ResultSet rs = stmt.executeQuery();
     * 
     * if (rs.next()) {
     * MUStepSubType muSubType = MUStepSubType.valueOf(rs.getString("SubType"));
     * 
     * MUDescription muDescription = new MUDescription(id, muSubType);
     * muDescription.setMuID(rs.getInt("MuRecursiveFunctionId"));
     * muDescription.setComponentID(rs.getInt("ComponentId"));
     * 
     * Gson gson = new GsonBuilder().setPrettyPrinting().create();
     * step.setData(gson.toJson(muDescription));
     * }
     * } catch (SQLException e) {
     * throw new NonRecoverableException("CourseDAO-ERR-MU " + e.toString(), e);
     * } finally {
     * close(stmt);
     * }
     * }
     */
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
     * Convert the DB comma delimited string of int values into an ArrayList.
     *
     * @param ids a comma delimited string of int values, "2,4,6")
     * @return an ArrayList of Integers corresponding the given ids.
     * @throws NonRecoverableException
     */
    private ArrayList<Integer> convertIds(String ids) throws NonRecoverableException {
        System.out.println("convertids: '" + ids + "'");
        ArrayList<Integer> nums = new ArrayList<>();

        if (!ids.trim().equals("")) {
            for (String s : ids.split(",")) {
                try {
                    int x = Integer.parseInt(s.trim());
                    nums.add(x);
                } catch (NumberFormatException e) {
                    throw new NonRecoverableException("Expecting an int: '" + s + "'");
                }
            }
        }
        return nums;
    }
}
