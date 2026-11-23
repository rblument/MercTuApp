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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.regis.merc.err.MuParseException;
import edu.regis.merc.model.Kind;
import edu.regis.merc.model.LeftHandSide;
import edu.regis.merc.model.MuExpression;
import edu.regis.merc.model.MuFunction;
import edu.regis.merc.model.MuParser;
import edu.regis.merc.model.OpKind;

/**
 * Integration tests for MuFunctionDAO, ported from the manual MuDAOTest
 * main() class. Runs against a real MySQL container (see BaseIT).
 */
@DisplayName("MuFunctionDAO Integration Tests")
public class MuFunctionDAOTest extends BaseMysqlIT {

    private MuFunctionDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new MuFunctionDAO();
    }

    @AfterEach
    public void tearDown() {
        // Cleanup: delete test functions so each run starts from the seed data
        deleteTestFunctions();
    }

    private void deleteTestFunctions() {
        String sql = "DELETE FROM MuFunction WHERE Name LIKE ?";
        try (Connection conn = DriverManager.getConnection(MySqlDAO.getURL());
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "test_%");
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Insert and load a MuFunction")
    public void testInsertAndLoadFunction() {
        // Insert
        LeftHandSide lhs = new LeftHandSide("testFunc");
        lhs.addParameter("x");
        MuExpression rhs = new MuExpression("0"); // f(x) = 0

        MuFunction f = new MuFunction(999, lhs, rhs);

        dao.insertFunction(f);

        // Verify insert by loading all functions
        List<MuFunction> functions = dao.getAllFunctions();
        assertNotNull(functions);
        assertFalse(functions.isEmpty());

        // Find our test function
        Optional<MuFunction> testFunc = functions.stream()
            .filter(func -> "testFunc".equals(func.getLhs().getName()))
            .findFirst();

        assertTrue(testFunc.isPresent(), "Inserted function testFunc was not loaded back");
        assertEquals("testFunc", testFunc.get().getLhs().getName());
        assertEquals("0", testFunc.get().getRhs().toString());
    }

    @Test
    @DisplayName("Load all functions from database")
    public void testLoadAllFunctions() {
        List<MuFunction> functions = dao.getAllFunctions();
        assertNotNull(functions);
        // Should have at least the seed data functions (add, Z)
        assertFalse(functions.isEmpty());
    }

    @Test
    @DisplayName("Seeded add Rhs loads as an operation, not a variable")
    public void testSeededAddLoadsAsOperation() {
        MuExpression rhs = findFunction(dao.getAllFunctions(), "add").getRhs();

        assertEquals(Kind.OPERATION, rhs.getKind());
        assertEquals(OpKind.ADD, rhs.getOpKind());
        assertEquals(Kind.VARIABLE, rhs.getLeftOperand().getKind());
        assertEquals(Kind.VARIABLE, rhs.getRightOperand().getKind());
        assertEquals("(x + y)", rhs.toString());
    }

    @Test
    @DisplayName("An application Rhs round trips through the DB")
    public void testApplicationRoundTrips() throws MuParseException {
        LeftHandSide lhs = new LeftHandSide("test_app");
        lhs.addParameter("x");

        dao.insertFunction(new MuFunction(999, lhs, new MuParser().parse("S(Z(x))")));

        MuExpression rhs = findFunction(dao.getAllFunctions(), "test_app").getRhs();

        assertEquals(Kind.APPLICATION, rhs.getKind());
        assertEquals("S(Z(x))", rhs.toString());
    }

    @Test
    @DisplayName("A row with an unparseable Rhs is skipped, the rest still load")
    public void testUnparseableRhsIsSkipped() throws Exception {
        String sql = "INSERT INTO MuFunction (Name, Lhs, Rhs) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(MySqlDAO.getURL());
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "test_bad");
            stmt.setString(2, "test_bad(x)");
            stmt.setString(3, "(x +");
            stmt.executeUpdate();
        }

        List<MuFunction> functions = dao.getAllFunctions();

        assertTrue(functions.stream().noneMatch(f -> "test_bad".equals(f.getLhs().getName())),
            "The unparseable function should be skipped");
        assertNotNull(findFunction(functions, "add"));
    }

    private MuFunction findFunction(List<MuFunction> functions, String name) {
        return functions.stream()
            .filter(f -> name.equals(f.getLhs().getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Function " + name + " was not loaded"));
    }
}
