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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.regis.merc.model.LCAbstraction;
import edu.regis.merc.model.LCApplication;
import edu.regis.merc.model.LCExpression;
import edu.regis.merc.model.LCVariable;
import edu.regis.merc.model.MuFunction;
import edu.regis.merc.model.Problem;
import edu.regis.merc.model.Step;
import edu.regis.merc.model.Task;

/**
 * Integration tests for loading the seeded successor function.
 *
 * LC_APPLICATION.FuncExprId used to be a foreign key into LC_ABSTRACTION, and
 * retrieveApplication loaded it with retrieveAbstraction, so only an
 * abstraction could sit in function position. Successor puts a variable there
 * (f applied to something) and an application there (n f, applied to x), so
 * these tests fail against the old schema and loader.
 */
@DisplayName("LCDAO Integration Tests")
public class LCDAOTest extends BaseMysqlIT {

    /** The seeded successor function, \n. \f. \x. f (n f x). */
    private static final int SUCCESSOR_ID = 240;

    /** The problem that shows it, second in the unit. */
    private static final int SUCCESSOR_PROBLEM_ID = 1;
    private static final int UNIT_ID = 0;

    /** The seeded projection, P_2^3 = \x1 x2 x3. x2, and its problem. */
    private static final int PROJECTION_ID = 260;
    private static final int PROJECTION_PROBLEM_ID = 2;

    private LCDAO dao;
    private ProblemDAO problemDao;

    @BeforeEach
    public void setUp() {
        dao = new LCDAO();
        problemDao = new ProblemDAO();
    }

    @Test
    @DisplayName("successor loads with a variable in function position")
    public void variableLoadsInFunctionPosition() throws Exception {
        LCApplication body = successorBody();

        // f (n f x): the function is the variable f, which the old
        // LC_ABSTRACTION foreign key could not reference.
        LCVariable function = assertInstanceOf(LCVariable.class, body.getFunction());

        assertEquals("f", function.getName());
    }

    @Test
    @DisplayName("successor loads with an application in function position")
    public void applicationLoadsInFunctionPosition() throws Exception {
        LCApplication body = successorBody();

        // n f x associates left, so the argument of the body is (n f) x and
        // the function of that is itself an application.
        LCApplication nfx = assertInstanceOf(LCApplication.class, body.getArg());
        LCApplication nf = assertInstanceOf(LCApplication.class, nfx.getFunction());

        assertEquals("n", assertInstanceOf(LCVariable.class, nf.getFunction()).getName());
        assertEquals("f", assertInstanceOf(LCVariable.class, nf.getArg()).getName());
        assertEquals("x", assertInstanceOf(LCVariable.class, nfx.getArg()).getName());
    }

    @Test
    @DisplayName("the loaded successor keeps the ids the steps grade against")
    public void loadedSuccessorKeepsComponentIds() throws Exception {
        LCAbstraction absN = assertInstanceOf(LCAbstraction.class, dao.retrieve(SUCCESSOR_ID));
        LCAbstraction absF = assertInstanceOf(LCAbstraction.class, absN.getBody().get(0));
        LCAbstraction absX = assertInstanceOf(LCAbstraction.class, absF.getBody().get(0));

        // Steps 52, 53 and 54 name these ids as their correct answers, and the
        // view reports whatever id it loaded, so a drift here silently makes
        // every answer wrong.
        assertEquals(245, absX.getParameters().get(0).getId(), "step 52 grades x");
        assertEquals(243, absF.getParameters().get(0).getId(), "step 53 grades f");
        assertEquals(246, absX.getBody().get(0).getId(),
                "step 54 grades the application wrapped around n f x");

        assertEquals(241, absN.getParameters().get(0).getId());
    }

    @Test
    @DisplayName("the successor problem loads with its expression and steps")
    public void successorProblemLoads() throws Exception {
        Problem problem = problemDao.retrieve(SUCCESSOR_PROBLEM_ID);

        assertNotNull(problem.getExpression(), "successor problem has no lambda expression");
        assertEquals(SUCCESSOR_ID, problem.getExpression().getId());

        Task task = problem.findTaskBySequence(0);
        assertNotNull(task, "successor problem has no first task");

        List<Step> steps = task.getSteps();
        assertEquals(3, steps.size());

        // ProblemDAO orders steps by SequenceIndex because PendingTask walks
        // them positionally.
        assertEquals(52, steps.get(0).getId());
        assertEquals(53, steps.get(1).getId());
        assertEquals(54, steps.get(2).getId());
    }

    @Test
    @DisplayName("the projection loads as one abstraction over three parameters")
    public void projectionLoadsItsParametersInOrder() throws Exception {
        LCAbstraction projection =
                assertInstanceOf(LCAbstraction.class, dao.retrieve(PROJECTION_ID));

        // P_2^3 is written uncurried, so this is a single abstraction over
        // three parameters rather than three nested ones. LC_ABS_PARAMS is
        // read ORDER BY SeqIndex, and the order is what makes x2 the second
        // of three rather than just some variable.
        List<LCVariable> parameters = projection.getParameters();
        assertEquals(3, parameters.size());
        assertEquals("x1", parameters.get(0).getName());
        assertEquals("x2", parameters.get(1).getName());
        assertEquals("x3", parameters.get(2).getName());

        // The body is the same row as the second parameter, so step 55 grades
        // the same id wherever the student clicks it.
        assertEquals(262, projection.getBody().get(0).getId());
        assertEquals(262, parameters.get(1).getId());
        assertEquals(263, parameters.get(2).getId(), "step 56 grades x3");
    }

    @Test
    @DisplayName("the projection problem carries both representations")
    public void projectionProblemHasBothRepresentations() throws Exception {
        Problem problem = problemDao.retrieve(PROJECTION_PROBLEM_ID);

        assertNotNull(problem.getExpression(), "projection problem has no lambda expression");
        assertEquals(PROJECTION_ID, problem.getExpression().getId());

        // Projection needs no function application, so unlike successor its mu
        // recursive form can be filled in already.
        MuFunction mu = problem.getMuFunction();
        assertNotNull(mu, "projection problem has no mu function");
        assertEquals("P", mu.getLhs().getName());
        assertEquals(List.of("x1", "x2", "x3"), mu.getLhs().getParameters());
    }

    @Test
    @DisplayName("successor is the problem after the zero function")
    public void successorFollowsZeroInTheUnit() throws Exception {
        Optional<Problem> next = problemDao.retrieveByUnitAndSequence(UNIT_ID, 1);

        // This is the lookup the tutor uses to advance, and an empty result is
        // how it decides a unit is finished -- so a gap here would end the unit
        // right after the zero function.
        assertTrue(next.isPresent(), "no problem at sequence 1");
        assertEquals(SUCCESSOR_PROBLEM_ID, next.get().getId());
    }

    /**
     * Load the successor function and return the body of its innermost
     * abstraction, f (n f x).
     */
    private LCApplication successorBody() throws Exception {
        LCExpression expr = dao.retrieve(SUCCESSOR_ID);

        LCAbstraction absN = assertInstanceOf(LCAbstraction.class, expr);
        LCAbstraction absF = assertInstanceOf(LCAbstraction.class, absN.getBody().get(0));
        LCAbstraction absX = assertInstanceOf(LCAbstraction.class, absF.getBody().get(0));

        return assertInstanceOf(LCApplication.class, absX.getBody().get(0));
    }
}
