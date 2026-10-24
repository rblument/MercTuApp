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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.Account;
import edu.regis.merc.model.CourseDigest;
import edu.regis.merc.model.PendingStep;
import edu.regis.merc.model.PendingTask;
import edu.regis.merc.model.Problem;
import edu.regis.merc.model.Student;
import edu.regis.merc.model.Task;
import edu.regis.merc.model.TutoringSession;
import edu.regis.merc.model.UnitDigest;

/**
 * Integration tests for the session writes that move a student through a
 * course. Runs against a real MySQL container (see BaseMysqlIT).
 */
@DisplayName("SessionDAO Integration Tests")
public class SessionDAOTest extends BaseMysqlIT {

    /** The seeded course, unit, problem and task of the "See One" unit. */
    private static final int COURSE_ID = 0;
    private static final int UNIT_ID = 0;
    private static final int PROBLEM_ID = 0;
    private static final int TASK_ID = 10;
    private static final int SECOND_STEP_ID = 51;

    private static final String TEST_USER = "sessiondaotest@regis.edu";

    private SessionDAO dao;
    private ProblemDAO problemDao;
    private TutoringSession session;

    @BeforeEach
    public void setUp() throws Exception {
        dao = new SessionDAO();
        problemDao = new ProblemDAO();

        deleteTestSession();

        session = newSession(problemDao.retrieve(PROBLEM_ID));
        dao.create(session);
    }

    @AfterEach
    public void tearDown() {
        deleteTestSession();
    }

    // ---------------------------------------------------------------
    // update
    // ---------------------------------------------------------------

    @Test
    @DisplayName("update writes the session's problem in place")
    public void updateWritesTheProblemId() throws Exception {
        session.setProblem(new Problem(7));

        dao.update(session);

        assertEquals(7, readSessionProblemId());
    }

    @Test
    @DisplayName("update does not create a second session")
    public void updateDoesNotInsertASecondSession() throws Exception {
        // This is the defect the method was written to fix: it used to be an
        // INSERT, so calling it gave a student a second session row.
        dao.update(session);
        dao.update(session);

        assertEquals(1, countSessions());
    }

    @Test
    @DisplayName("update rejects a session that is not in the database")
    public void updateRejectsAnUnknownSession() throws Exception {
        deleteTestSession();

        assertThrows(ObjNotFoundException.class, () -> dao.update(session));
    }

    // ---------------------------------------------------------------
    // updateCurrentTask
    // ---------------------------------------------------------------

    @Test
    @DisplayName("updateCurrentTask inserts exactly one pending step")
    public void updateCurrentTaskInsertsOnePendingStep() throws Exception {
        int before = countPendingSteps();

        pointAtSecondStep();
        dao.updateCurrentTask(session);

        // A nested call once made this insert twice, leaving an orphan row
        // that nothing pointed at.
        assertEquals(before + 1, countPendingSteps());
    }

    @Test
    @DisplayName("updateCurrentTask repoints the pending task at the new step")
    public void updateCurrentTaskRepointsThePendingTask() throws Exception {
        int originalStepId = readPendingStepId();

        pointAtSecondStep();
        dao.updateCurrentTask(session);

        int repointed = readPendingStepId();

        assertNotEquals(originalStepId, repointed);
        assertEquals(TASK_ID, readPendingTaskId());
        assertEquals(SECOND_STEP_ID, readStepIdOfPendingStep(repointed));
    }

    @Test
    @DisplayName("updateCurrentTask writes the session's problem too")
    public void updateCurrentTaskWritesTheProblemId() throws Exception {
        session.setProblem(new Problem(7));
        pointAtSecondStep();

        dao.updateCurrentTask(session);

        // One method serves both kinds of advance, so it writes the problem id
        // whether or not it changed.
        assertEquals(7, readSessionProblemId());
    }

    @Test
    @DisplayName("updateCurrentTask rejects a session with no step to advance to")
    public void updateCurrentTaskRejectsAMissingStep() throws Exception {
        PendingTask empty = new PendingTask(session.currentTask().getTask());

        ArrayList<PendingTask> tasks = new ArrayList<>();
        tasks.add(empty);
        session.setTasks(tasks);

        // currentStep() is null here, and it is written straight to the
        // database, so this has to fail rather than raise an NPE inside the
        // transaction. Asserting the specific type is what makes this a test of
        // the guard: without it the call still throws, just an NPE.
        assertThrows(NonRecoverableException.class, () -> dao.updateCurrentTask(session));
    }

    // ---------------------------------------------------------------
    // retrieveByUnitAndSequence
    // ---------------------------------------------------------------

    @Test
    @DisplayName("retrieveByUnitAndSequence finds the seeded problem")
    public void retrieveByUnitAndSequenceFindsTheSeededProblem() throws Exception {
        Optional<Problem> found = problemDao.retrieveByUnitAndSequence(UNIT_ID, 0);

        assertTrue(found.isPresent());
        assertEquals(PROBLEM_ID, found.get().getId());
    }

    @Test
    @DisplayName("retrieveByUnitAndSequence is empty past the last problem")
    public void retrieveByUnitAndSequenceIsEmptyPastTheEnd() throws Exception {
        // Empty is how the tutor learns a unit is finished, so it is a normal
        // outcome rather than an error.
        assertTrue(problemDao.retrieveByUnitAndSequence(UNIT_ID, 99).isEmpty());
    }

    // ---------------------------------------------------------------
    // fixture helpers
    // ---------------------------------------------------------------

    /**
     * Build a session for the test user sitting on the given problem's first
     * task and first step.
     */
    private TutoringSession newSession(Problem problem) {
        Account account = new Account(TEST_USER);
        TutoringSession tSession = new TutoringSession(new Student(account));

        tSession.setSecurityToken("sessiondaotest-token");
        tSession.setStartDate(new GregorianCalendar());
        tSession.setCourse(new CourseDigest(COURSE_ID));
        tSession.setUnit(new UnitDigest(UNIT_ID));
        tSession.setProblem(problem);

        Task task = problem.findTaskBySequence(0);
        PendingTask pendingTask = new PendingTask(task);
        pendingTask.setCurrentStep(new PendingStep(task.getSteps().get(0)));
        tSession.addTask(pendingTask);

        return tSession;
    }

    /**
     * Point the session at an unsaved pending step for the task's second step,
     * which is what the session looks like after an advance but before it is
     * written.
     */
    private void pointAtSecondStep() {
        Task task = session.currentTask().getTask();

        PendingTask pendingTask = new PendingTask(task);
        pendingTask.setCurrentStep(new PendingStep(task.findStepById(SECOND_STEP_ID)));

        ArrayList<PendingTask> tasks = new ArrayList<>();
        tasks.add(pendingTask);
        session.setTasks(tasks);
    }

    private int countSessions() throws Exception {
        return queryInt("SELECT COUNT(*) FROM TutoringSession WHERE UserId = ?", TEST_USER);
    }

    private int readSessionProblemId() throws Exception {
        return queryInt("SELECT ProblemId FROM TutoringSession WHERE UserId = ?", TEST_USER);
    }

    private int countPendingSteps() throws Exception {
        return queryInt("SELECT COUNT(*) FROM PendingStep WHERE SessionId = ?", session.getId());
    }

    private int readPendingStepId() throws Exception {
        return queryInt("SELECT PendingStepId FROM PendingTask WHERE SessionId = ?", session.getId());
    }

    private int readPendingTaskId() throws Exception {
        return queryInt("SELECT TaskId FROM PendingTask WHERE SessionId = ?", session.getId());
    }

    private int readStepIdOfPendingStep(int pendingStepId) throws Exception {
        return queryInt("SELECT StepId FROM PendingStep WHERE Id = ?", pendingStepId);
    }

    /**
     * Run a single-column, single-row query with one bound parameter.
     */
    private int queryInt(String sql, Object parameter) throws Exception {
        try (Connection conn = DriverManager.getConnection(MySqlDAO.getURL());
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (parameter instanceof Integer) {
                stmt.setInt(1, (Integer) parameter);
            } else {
                stmt.setString(1, parameter.toString());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    /**
     * Remove the test user's session and its pending rows, so each test starts
     * from the seed data.
     */
    private void deleteTestSession() {
        String findSessions = "SELECT Id FROM TutoringSession WHERE UserId = ?";

        try (Connection conn = DriverManager.getConnection(MySqlDAO.getURL());
                PreparedStatement find = conn.prepareStatement(findSessions)) {

            find.setString(1, TEST_USER);

            ArrayList<Integer> ids = new ArrayList<>();
            try (ResultSet rs = find.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }

            for (int id : ids) {
                delete(conn, "DELETE FROM PendingStep WHERE SessionId = ?", id);
                delete(conn, "DELETE FROM PendingTask WHERE SessionId = ?", id);
                delete(conn, "DELETE FROM TutoringSession WHERE Id = ?", id);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Unable to clean up the test session", e);
        }
    }

    private void delete(Connection conn, String sql, int id) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
