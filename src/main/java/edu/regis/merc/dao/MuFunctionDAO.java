// Kristin Ingram
// Last updated: December 08
// Fully DB - integrated MU DAO

package edu.regis.merc.dao;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.regis.merc.err.MuParseException;
import edu.regis.merc.model.*;
import edu.regis.merc.svc.MuFunctionSvc;

public class MuFunctionDAO extends MySqlDAO implements MuFunctionSvc {

    private static final Logger LOGGER = Logger.getLogger(MuFunctionDAO.class.getName());

    @Override
    public List<MuFunction> getAllFunctions() {
        List<MuFunction> functions = new ArrayList<>();

        String sql = "SELECT Id, Name, Lhs, Rhs FROM MuFunction";

        try (Connection conn = DriverManager.getConnection(getURL());
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery())
        {
            while (rs.next()) {
                int id = rs.getInt("Id");
                String name = rs.getString("Name");
                String lhsText = rs.getString("Lhs");
                String rhsText = rs.getString("Rhs");

                System.out.println("Loaded MU Function from DB → " + name
                                   + "  LHS=" + lhsText + "  RHS=" + rhsText);

                MuExpression rhs;
                try {
                    rhs = new MuParser().parse(rhsText);
                } catch (MuParseException e) {
                    // Skip a row the parser rejects so one bad Rhs doesn't
                    // keep the rest of the functions from loading.
                    LOGGER.log(Level.WARNING, "Skipping MuFunction " + id + " (" + name
                            + "): Rhs could not be parsed", e);
                    continue;
                }

                MuFunction f = new MuFunction(id, new LeftHandSide(name), rhs);
                functions.add(f);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return functions;
    }

    @Override
    public void insertFunction(MuFunction f) {
        String sql = "INSERT INTO MuFunction (Name, Lhs, Rhs) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(getURL());
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, f.getLhs().getName());
            stmt.setString(2, f.getLhs().toString());
            stmt.setString(3, f.getRhs().toString());

            stmt.executeUpdate();

            System.out.println("Inserted MU Function → " + f);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int evaluate(MuFunction function, int... args) {
        MuEvaluator evaluator = new MuEvaluator();
        Map<String, Integer> env = function.getLhs().bindArguments(args);
        return evaluator.eval(function.getRhs(), env);
    }
}
