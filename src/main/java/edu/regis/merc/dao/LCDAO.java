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

import edu.regis.merc.err.InconsistentDBException;
import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.BloomLevel;
import edu.regis.merc.model.LCExpression;
import edu.regis.merc.model.LCAbstraction;
import edu.regis.merc.model.LCApplication;
import edu.regis.merc.model.LCVariable;
import edu.regis.merc.svc.LCSvc;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


/**
 * This is a data access object for loading recursive Lambda Calculus 
 * Expressions from the database.
 * @author Ellis Langham
 */
public class LCDAO extends MySqlDAO implements LCSvc{
    
    /**
     * Instantiates this LC DAO with default values 
     */
    public LCDAO(){
        super();
    }
    
   
    /**
     * Retrieve the LC Expression using the given ID from the database.
     * This method manages the database connection and initiates the loading of 
     * the expression tree. 
     * 
     * @param exprId - the unique ID fo the expression to be loaded
     * @return - fully loaded LCExpression object tree
     * @throws ObjNotFoundException - the the expression ID does not exist
     * @throws NonRecoverableException - if a database error occurs 
     */
    public LCExpression retrieve(int exprId) throws ObjNotFoundException, NonRecoverableException {
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(URL);
            return retrieveExpressionRecursive(exprId, conn);
        }catch (SQLException e){
            throw new NonRecoverableException("LCDAO_ERR-1: " + e.toString(), e);
        }finally {
            close(conn);
        }
    }
    
    /**
     * Recursively determines the type of expression and delegate to the 
     * specific loader.
     * @param exprId - id of the expression to load
     * @param conn - database connection
     * @return - the specific subclass of LCExpression (Variable, 
     * Abstraction, or Application).
     * @throws SQLException
     * @throws ObjNotFoundException
     * @throws NonRecoverableException 
     */
    private LCExpression retrieveExpressionRecursive(int exprId, Connection conn) 
                throws SQLException, ObjNotFoundException, NonRecoverableException {

        String sql = "SELECT ExprType FROM LC_EXPRESSION WHERE Id = ?";
        PreparedStatement stmt = null;
        String type = null;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, exprId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                type = rs.getString("ExprType");
            } else {
                throw new ObjNotFoundException("LCExpression Id: " + exprId);
            }
        } finally {
            close(stmt);
        }
        
        // Delegate to the specific loader based on the type that's found
        switch (type) {
            case "VAR": 
                return retrieveVariable(exprId, conn);
            case "ABS": 
                return retrieveAbstraction(exprId, conn);
            case "APP": 
                return retrieveApplication(exprId, conn);
            default: 
                throw new NonRecoverableException("Unknown ExprType: " + type);
        }
    }
    
    /**
     * Helper method to load an LCVariable from the database
     * @param id - id of the Variable
     * @param conn
     * @return
     * @throws SQLException
     * @throws ObjNotFoundException 
     */
    private LCVariable retrieveVariable(int id, Connection conn) throws SQLException, ObjNotFoundException {
        String sql = "SELECT Name FROM LC_VARIABLE WHERE Id = ?";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return new LCVariable(rs.getString("Name"));
            }else { 
                throw new ObjNotFoundException("LCVariable Id: " + id);
            }
        }finally {
            close(stmt);
        }
    }
    
    /**
     * Helper method to load an LCAbstraction along with its parameter list and body list. 
     * @param id - id of the abstraction 
     * @param conn
     * @return
     * @throws SQLException
     * @throws ObjNotFoundException
     * @throws NonRecoverableException 
     */
    private LCAbstraction retrieveAbstraction(int id, Connection conn) 
            throws SQLException, ObjNotFoundException, NonRecoverableException {
        
        LCAbstraction abs = new LCAbstraction();
        
        // 1. Load the main properties (IsCurried)
        String mainSQL = "SELECT IsCurried FROM LC_ABSTRACTION WHERE Id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(mainSQL)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                abs.setIsCurried(rs.getBoolean("IsCurried"));
            }else {
                throw new ObjNotFoundException("LCAbstraction Id: " + id);
            }
        }
        
        // 2. load the parameters 
        String paramSql = "SELECT VarId FROM LC_ABS_PARAMS WHERE AbsId = ? ORDER BY SeqIndex ASC";
        try (PreparedStatement stmt = conn.prepareStatement(paramSql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int varId = rs.getInt("VarId");
                LCVariable param = retrieveVariable(varId, conn);
                abs.addParameter(param); // Uses your addParameter method
            }
        }
        
        // 3. load the list of body parts
        String bodySql = "SELECT BodyExprId FROM LC_ABS_BODY WHERE AbsId = ? ORDER BY SeqIndex ASC";
        try (PreparedStatement stmt = conn.prepareStatement(bodySql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int bodyId = rs.getInt("BodyExprId");
                LCExpression bodyPart = retrieveExpressionRecursive(bodyId, conn);
                abs.addBodyPart(bodyPart); // Uses your addBodyPart method
            }
        }

        return abs;
        
        
    }
    
    /**
     * Helper method to load an LCApplication, recursively loading its function and argument
     * @param id - id of the application 
     * @param conn
     * @return
     * @throws SQLException
     * @throws ObjNotFoundException
     * @throws NonRecoverableException 
     */
    private LCApplication retrieveApplication(int id, Connection conn) 
            throws SQLException, ObjNotFoundException, NonRecoverableException {
            
        String sql = "SELECT FuncAbsId, ArgExprId FROM LC_APPLICATION WHERE Id = ?";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int funcId = rs.getInt("FuncAbsId");
                int argId = rs.getInt("ArgExprId");

                LCApplication app = new LCApplication();
                app.setFunction(retrieveAbstraction(funcId, conn));
                app.setArg(retrieveExpressionRecursive(argId, conn));
                
                return app;
            } else {
                throw new ObjNotFoundException("LCApplication Id: " + id);
            }
        } finally {
            close(stmt);
        }
    }
    
}
