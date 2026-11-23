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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.regis.merc.model.Kind;
import edu.regis.merc.model.MuExpression;
import edu.regis.merc.model.Problem;

/**
 * Integration tests for how ProblemDAO loads a problem's mu function. Each
 * test inserts its own MuFunction and Problem rows, so the seed data is left
 * as it is.
 */
@DisplayName("ProblemDAO Integration Tests")
public class ProblemDAOTest extends BaseMysqlIT {

    // Outside the seeded ids and units, so a leftover row can't show up in a
    // student's unit.
    private static final int TEST_PROBLEM_ID = 9001;
    private static final int TEST_UNIT_ID = 9001;
    private static final String TEST_FUNCTION = "test_problem_mu";

    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = DriverManager.getConnection(MySqlDAO.getURL())) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Problem WHERE Id = ?")) {
                stmt.setInt(1, TEST_PROBLEM_ID);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM MuFunction WHERE Name = ?")) {
                stmt.setString(1, TEST_FUNCTION);
                stmt.executeUpdate();
            }
        }
    }

    @Test
    @DisplayName("A problem's mu function Rhs is parsed into an expression tree")
    public void testMuFunctionRhsIsParsed() throws Exception {
        insertProblemWithMuFunction("S(Z(x))");

        Problem problem = new ProblemDAO().retrieve(TEST_PROBLEM_ID);

        assertNotNull(problem.getMuFunction());
        MuExpression rhs = problem.getMuFunction().getRhs();
        assertEquals(Kind.APPLICATION, rhs.getKind());
        assertEquals("S(Z(x))", rhs.toString());
    }

    @Test
    @DisplayName("An unparseable Rhs leaves the problem without a mu function")
    public void testUnparseableRhsLeavesNoMuFunction() throws Exception {
        insertProblemWithMuFunction("(x +");

        Problem problem = new ProblemDAO().retrieve(TEST_PROBLEM_ID);

        assertNull(problem.getMuFunction());
    }

    private void insertProblemWithMuFunction(String rhs) throws SQLException {
        try (Connection conn = DriverManager.getConnection(MySqlDAO.getURL())) {
            int muId;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO MuFunction (Name, Lhs, Rhs) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, TEST_FUNCTION);
                stmt.setString(2, TEST_FUNCTION + "(x)");
                stmt.setString(3, rhs);
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    keys.next();
                    muId = keys.getInt(1);
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO Problem (Id, Title, Description, UnitId, SequenceIndex,"
                    + " TuringMachineId, LambdaCalculusId, MuRecursiveFunctionId)"
                    + " VALUES (?, ?, ?, ?, ?, NULL, NULL, ?)")) {
                stmt.setInt(1, TEST_PROBLEM_ID);
                stmt.setString(2, "Test mu problem");
                stmt.setString(3, "Loads a mu function for ProblemDAOTest.");
                stmt.setInt(4, TEST_UNIT_ID);
                stmt.setInt(5, 0);
                stmt.setInt(6, muId);
                stmt.executeUpdate();
            }
        }
    }
}
