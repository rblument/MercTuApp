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

import edu.regis.merc.BaseIT;
import edu.regis.merc.model.LeftHandSide;
import edu.regis.merc.model.MuExpression;
import edu.regis.merc.model.MuFunction;

/**
 * Integration tests for MuFunctionDAO, ported from the manual MuDAOTest
 * main() class. Runs against a real MySQL container (see BaseIT).
 */
@DisplayName("MuFunctionDAO Integration Tests")
public class MuFunctionDAOTest extends BaseIT {

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
        try (Connection conn = DriverManager.getConnection(MySqlDAO.URL);
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
}
