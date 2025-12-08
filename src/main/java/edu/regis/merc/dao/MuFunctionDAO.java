// Kristin Ingram
// Last updated: December 08
// Fully DB - integrated MU DAO

package edu.regis.merc.dao;

import java.sql.*;
import java.util.*;

import edu.regis.merc.model.*;
import edu.regis.merc.svc.MuFunctionSvc;

public class MuFunctionDAO extends MySqlDAO implements MuFunctionSvc {

    @Override
    public List<MuFunction> getAllFunctions() {
        List<MuFunction> functions = new ArrayList<>();

        String sql = "SELECT Id, Name, Lhs, Rhs FROM MuFunction";

        try (Connection conn = DriverManager.getConnection(MySqlDAO.URL);
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

                MuFunction f = new MuFunction(id, new LeftHandSide(name), new MuExpression(rhsText));
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

        try (Connection conn = DriverManager.getConnection(MySqlDAO.URL);
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
