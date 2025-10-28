// Kristin Ingram
// last updated: 10/28
package edu.regis.merc.dao;

import java.sql.*;
import java.util.*;

import edu.regis.merc.model.MuFunction;

public class MuFunctionDAO extends MySqlDAO {

    public List<MuFunction> getAllFunctions() {
        List<MuFunction> functions = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(MySqlDAO.URL);  
            stmt = conn.prepareStatement("SELECT * FROM MuFunction");
            rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("Id");
                String name = rs.getString("Name");
                String lhs = rs.getString("Lhs");
                String rhs = rs.getString("Rhs");

                System.out.println("MuFunction: " + id + " " + name + " = " + rhs);
                // TO DO: create and add MuFunction objects if needed
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, stmt); 
        }

        return functions;
    }

    public void insertFunction(MuFunction f) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(MySqlDAO.URL);
            stmt = conn.prepareStatement(
                "INSERT INTO MuFunction (Name, Lhs, Rhs) VALUES (?, ?, ?)");
            stmt.setString(1, f.getLhs().getName());
            stmt.setString(2, f.getLhs().toString());
            stmt.setString(3, f.getRhs().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, stmt); //
        }
    }
}
