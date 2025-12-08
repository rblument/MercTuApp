/*
 * MERC^T: Multiple External Representations of Computation Tutor
 *
 *  (C) PK Ewusie-Mensah, All rights reserved
 *
 *  Unauthorized use, duplication or distribution without the authors'
 *  permission is strictly prohibited.
 *
 *  Unless required by applicable law or agreed to in writing, this
 *  software is distributed on an "AS IS" basis without warranties
 *  or conditions of any kind, either expressed or implied.
 */
package edu.regis.merc.dao;

import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import edu.regis.merc.model.GuiCtx;
import edu.regis.merc.model.Model;
import edu.regis.merc.model.MoveKind;
import edu.regis.merc.model.State;
import edu.regis.merc.model.Transition;
import edu.regis.merc.model.TuringMachine;
import edu.regis.merc.svc.TuringMachingSvc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A Data Access Object implementing {@link TuringMachine} CRUD behaviors.
 * 
 * @author PK
 * @author rickb
 */
public class TuringMachineDAO extends MySqlDAO implements TuringMachingSvc {
    /**
     * Initialize a default DAO.
     */
    public TuringMachineDAO() {
        super();
    }

   /**
     * {@inheritDoc}
     */
    @Override
    public int create(TuringMachine machine) throws NonRecoverableException {
        // ToDo: Not Tested
        final String sql = "INSERT INTO TuringMachine(Name, Description, StartStateId, "
                + "AcceptStateId, RejectSstateId) VALUES (?, ?, ?, ?, ?)";

        Connection conn;
        PreparedStatement stmt;

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // 1. Save States and get their IDs
            int startStateId = saveState(conn, machine.getStartState());
            int acceptStateId = saveState(conn, machine.getAcceptState());
            int rejectStateId = saveState(conn, machine.getRejectState());

            //stmt.setInt(1, machine.getId());
            stmt.setString(1, machine.getTitle());
            stmt.setString(2, machine.getDescription());
            stmt.setInt(3, startStateId);
            stmt.setInt(4, acceptStateId);
            stmt.setInt(5, rejectStateId);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int machineId = rs.getInt(1);
                    machine.setId(machineId);

                    // 3. Save alphabets (linking to the new machine ID)
                    //saveAlphabet(conn, machineId, "input", machine.getInputAlphabet());
                    //saveAlphabet(conn, machineId, "tape", machine.getTapeAlphabet());

                    for (State s : machine.getStates()) {
                        if (s != machine.getStartState() && s != machine.getAcceptState() && s != machine.getRejectState()) {
                            saveState(conn, machineId, s);
                        }
                    }
//
//                    // Save transitions
//                    for (State s : machine.getStates()) {
//                        for (Transition t : s.getTransitions()) {
//                            saveTransition(conn, machineId, t);
//                        }
//                    }

                    return machineId;
                }
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("TMDAO-ERR-1", e);
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TuringMachine retrieve(int id) throws ObjNotFoundException, NonRecoverableException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(URL);
            
            return retrieve(id, conn);
            
        } catch (SQLException e) {
            throw new NonRecoverableException("TMDAO-ERR-4", e);
        } finally {
            close(conn, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TuringMachine> retrieveAll() throws NonRecoverableException {
        // ToDo: Not Tested
        final String sql = "SELECT Id from TuringMachine";
        
        List<TuringMachine> machines = new ArrayList<>();
         
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();
  
            while (rs.next()) {
                machines.add(retrieve(rs.getInt("Id"), conn));
            }
            
            return machines;
        } catch (ObjNotFoundException e) {
            throw new NonRecoverableException("TMDAO-ERR-6 Should not occur since we retrieved ids");
        } catch (SQLException e) {
            throw new NonRecoverableException("TMDAO-ERR-8", e);
        } finally {
            close(conn, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(int id) throws ObjNotFoundException, NonRecoverableException {
        // ToDo: Not Tested
        Connection conn;
        PreparedStatement stmt;
        String sql = "DELETE FROM TuringMachine WHERE Id = ?";

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(sql);

            try {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            throw new NonRecoverableException("TMDAO-ERR-10", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int createState(State state) {
        // ToDo: Not Tested
        Connection conn = null;
        PreparedStatement stmt = null;
        final String sql = "INSERT INTO State(TmId, Name, GuiCtxId) VALUES (?, ?, ?)";

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

//            stmt.setInt(1, state.getMachineId());
            stmt.setString(2, state.getName());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                return 0;
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error creating state", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public int updateState(State state) {
        // ToDo, 
        Connection conn = null;
        PreparedStatement stmt = null;
        final String sql = "UPDATE State SET Name = ? WHERE Id = ?";

        try {
            conn = DriverManager.getConnection(URL);
           stmt = conn.prepareStatement(sql);
   
            stmt.setString(1, state.getName());
            stmt.setInt(2, state.getId());

            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating state", e);
        }
    }
    
        
    /**
     * Retrieved the identified Turing Machine from the database4.     * 
     * 
     * @param id the unique identifier of the machine
     * @param conn an open connection to the DB, which is not closed
     * @return the corresponding {@link TuringMachine}
     * @throws ObjNotFoundException no Turing Machine with the given id exists
     * @throws NonRecoverableException perhaps see getCause().getErrorCode()
     */
    public TuringMachine retrieve(int id, Connection conn) throws ObjNotFoundException, NonRecoverableException {
        final String query = "Select Name, Description, StartStateId, AcceptStateId, RejectStateId FROM TuringMachine WHERE Id = ?";
  
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(query);
    
            stmt.setInt(1, id);

            try {
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
   
                    TuringMachine tm = new TuringMachine(id);
                    tm.setTitle(rs.getString("Name"));
                    tm.setDescription(rs.getString("Description"));
  
                    tm.setStates(retrieveStates(id, conn));

                    // Assumes there must be a start state in the TM
                    tm.setStartState(tm.findState(rs.getInt("StartStateId")));
 
                    // Assumes there must be an accept state in the TM
                    tm.setAcceptState(tm.findState(rs.getInt("AcceptStateId")));
    
                    // We do not assume the TM has a Reject state.
                    int rejectStateId = rs.getInt("RejectStateId");
                    if (rejectStateId != Model.DEFAULT_ID) {
                        tm.setRejectState(tm.findState(rejectStateId));
                    }
                   
                    tm.setTransitions(retrieveTransitions(tm, conn));

                    String type = "INPUT";
                    try {
                        tm.setInputAlphabet(retrieveAlphabet(id, type, conn));
                        type = "TAPE";
                        tm.setTapeAlphabet(retrieveAlphabet(id, type, conn));
                        
                    } catch (ObjNotFoundException ex) {
                        throw new NonRecoverableException("TMDAO-ERR-43 see cause", ex);
                    }

                    return tm;
                } else {
                    throw new ObjNotFoundException("Tm doesn't exist in DB " + id);
                            }
            } catch (SQLException e) {
                throw new NonRecoverableException("TMDAO-ERR-44");
            }

        } catch (SQLException e) {
           throw new NonRecoverableException("TMDAO-ERR-46");
        } finally {
            close(stmt);
        }
    }
    
    /**
     * Retrieve the states for the given Turing Machine id by loading them from
     * the database.
     * 
     * @param tmId int id of the Turing Machine whose states are returned.
     * @param conn an open connection to the DB, which is not closed.
     * @return a List of States for the given Turing Machine id
     * @throws NonRecoverableException perhaps see getCause().getErrorCode()
     */
    private ArrayList<State> retrieveStates(int tmId, Connection conn) throws NonRecoverableException {
        final String query = "SELECT Id,Name,GuiCtxId FROM TmState WHERE TmId = ?";

        ArrayList<State> states = new ArrayList<>();
        
        PreparedStatement stmt = null;
        
        int guiCtxId = Model.DEFAULT_ID;
        
        try {
            stmt = conn.prepareStatement(query);
            
            stmt.setInt(1, tmId);
   
            ResultSet rs = stmt.executeQuery();
                    
            while (rs.next()) {
                State state = new State(rs.getInt("Id"));
                state.setName(rs.getString("Name"));
                guiCtxId = rs.getInt("GuiCtxId");
                state.setGuiCtx(retrieveGuiCtx(guiCtxId, conn));
                
                states.add(state);
            }
            
            return states;
                    
        } catch (ObjNotFoundException e) {
            throw new NonRecoverableException("TMDAO-ERR-50 InconsistentDB GuiCtx " + guiCtxId + " not found for tmId: " + tmId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(stmt);
        } 
    }
    
    /**
     * Retrieve the transitions for the given Turing Machine by loading them
     * from the database (see note).
     * 
     * Note: Assumes the states of the Turing Machine have already been loaded 
     *       into the machine.
     * 
     * @param tm the Turing Machine whose transitions are retrieved
     * @param conn an open connection to the DB, which is not closed
     * @return a List of Transitions for the specified Turing Machine
     * @throws NonRecoverableException perhaps see getCause().getErrorCode()
     */
    private ArrayList<Transition> retrieveTransitions(TuringMachine tm, Connection conn) throws NonRecoverableException {
        final String query = "SELECT Id,FromStateId,ToStateId,ReadSymbol,WriteSymbol,Direction,GuiCtxId FROM TmTransition WHERE TmId = ?";

        ArrayList<Transition> transitions = new ArrayList<>();
        
        PreparedStatement stmt = null;
        
        int guiCtxId = Model.DEFAULT_ID;
        
        int tmId = tm.getId();
        
        try {
            stmt = conn.prepareStatement(query);
            
            stmt.setInt(1, tmId);
   
            ResultSet rs = stmt.executeQuery();
                    
            while (rs.next()) {
                Transition transition = new Transition(rs.getInt("Id"));
       
                int fromStateId = rs.getInt("FromStateId");
                try {
                    transition.setFromState(tm.findState(fromStateId));
                } catch (ObjNotFoundException ex) {
                    throw new NonRecoverableException("TMDAO-ERR-60 InconsistentDB FromState " + fromStateId + " not found in transition: " + rs.getInt("Id"));
                }
                
                int toStateId = rs.getInt("FromStateId");
                try {
                    transition.setToState(tm.findState(toStateId));
                } catch (ObjNotFoundException ex) {
                    throw new NonRecoverableException("TMDAO-ERR-61 InconsistentDB ToState " + toStateId + " not found in transition: " + rs.getInt("Id"));
                }
                
                transition.setRead(rs.getString("ReadSymbol").charAt(0));
                transition.setWrite(rs.getString("WriteSymbol").charAt(0));
                
                transition.setDirection(MoveKind.valueOf(rs.getString("Direction")));
                
                // We don't assume a Transition has a GuiCtx since they can be
                // positioned to connect to State anchors.
                guiCtxId = rs.getInt("GuiCtxId");
                if (guiCtxId != Model.DEFAULT_ID) {
                    transition.setGuiCtx(retrieveGuiCtx(guiCtxId, conn));
                }
                
                transitions.add(transition);
            }
            
            return transitions;
                    
        } catch (ObjNotFoundException e) {
            throw new NonRecoverableException("TMDAO-ERR-50 InconsistentDB GuiCtx " + guiCtxId + " not found for tmId: " + tmId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(stmt);
        }  
    }
    
    /**
     * Retrieve the identified GUI Context by loading it from the DB>
     * 
     * @param guiCtxId the id of the GUI context to retrieve
     * @param conn an open connection to the DB, which is not closed.
     * @return  the GuiCtx with the given id
     * @throws ObjNotFoundException the identified GuiCtx doesn't exist in the DB.
     * @throws NonRecoverableException perhaps see getCause().getErrorCode()
     */
    private GuiCtx retrieveGuiCtx(int guiCtxId, Connection conn) throws ObjNotFoundException, NonRecoverableException {
        final String query = "SELECT X,Y,Width,Height,X2,Y2 FROM GuiCtx WHERE Id = ?";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = conn.prepareStatement(query);
            
            stmt.setInt(1, guiCtxId);
   
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                GuiCtx guiCtx = new GuiCtx(guiCtxId);
                
                guiCtx.setX(rs.getInt("X"));
                guiCtx.setY(rs.getInt("Y"));
                guiCtx.setWidth(rs.getInt("Width"));
                guiCtx.setHeight(rs.getInt("Height"));
                guiCtx.setX2(rs.getInt("X2"));
                guiCtx.setY2(rs.getInt("Y2"));
                
                return guiCtx;
            } else {
                throw new ObjNotFoundException("GuiCtx not found in DB: " + guiCtxId);
            }
            
        } catch (SQLException e) {
            throw new NonRecoverableException("TMDAO-ERR-60", e);
        } finally {
            close(stmt);
        }
    }

    private int saveState(Connection conn, int machineId, State state) throws SQLException {
        if (state == null) {
            return 0;
        }
        final String sql = "INSERT INTO State(TmId, name) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, machineId);
            stmt.setString(2, state.getName());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    state.setId(id);
                    return id;
                }
            }
        }
        return 0;
    }


    ///
    ///  Transition Methods
    ///

    /*
    @Override
    public int createTransition(Transition transition) {
        Connection conn = null;
        PreparedStatement stmt = null;
        final String sql = "INSERT INTO Transition(machine_id, from_state_id, to_state_id, read_symbol, "
                + "write_symbol, move_direction) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, String.valueOf(transition.getRead()));
            stmt.setString(2, String.valueOf(transition.getWrite()));
            stmt.setString(3, transition.getDirection().name());
            stmt.setString(4, transition.getNextState().getName());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                return 0;
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error creating transition", e);
        }
        return 0;
    }
*/
    
  
/*
    @Override
    public List<Transition> getTransitionsByMachine(int machineId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        final String query = "SELECT * FROM Transition WHERE machine_id = ?";
        List<Transition> transitions = new ArrayList<>();

//        try {
//            conn = DriverManager.getConnection(URL);
//            stmt = conn.prepareStatement(query);
//
//            stmt.setInt(1, machineId);
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    transitions.add(new Transition(
//                            rs.getString("read_symbol").charAt(0),
//                            rs.getString("write_symbol").charAt(0),
//                            MoveKind.valueOf(rs.getString("move_direction")),  // must match enum
//                            new State(rs.getString("next_state")) // reconstruct State by name
//                    ));
//                }
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("Error fetching transitions by machine", e);
//        }
        return transitions;
    }
    
    */

    ///
    ///  Helper Classes
    ///

    private int saveState(Connection conn, State state) throws SQLException {
        // Not Test
        // Logic to insert a State into the `states` table and return its ID
        String sql = "INSERT INTO state (Name, TmId, GuiCtxId) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Set parameters based on the State object
            pstmt.setString(1, state.getName());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to save state.");
    }

    /**
     * Return the specified alphabet for the specified Turing Machine id.
     * 
     * @param tmId int id of the TuringMachine, whose alphabe is returned.
     * @param type the alphabet type INPUT or TAPE // ToDo Change to an enum
     * @param conn an open connection to the DB, which isn't closed
     * @return a List of chars
     * @throws ObjNotFoundException no alphabet for the tm and type exists
     * @throws NonRecoverableException perhaps see getCause().getErrorCode()
     */
    private ArrayList<Character> retrieveAlphabet(int tmId, String type, Connection conn) throws ObjNotFoundException, NonRecoverableException {
        final String sqlAlphabet = "SELECT Id FROM Alphabet WHERE TmId = ? AND type = ?";
        final String sqlSymbols = "SELECT Symbol FROM AlphabetSymbol WHERE AlphabetId = ?";
         
        ArrayList<Character> alphabet = new ArrayList<>();
        
        int alphabetId = 0;

        try {
            PreparedStatement stmt = conn.prepareStatement(sqlAlphabet);
            
            stmt.setInt(1, tmId);
            stmt.setString(2, type);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                alphabetId = rs.getInt("id");
                
                if (alphabetId != Model.DEFAULT_ID) {
                
                    try {
                        PreparedStatement stmt2 = conn.prepareStatement(sqlSymbols);
                        stmt2.setInt(1, alphabetId);
                
                        rs = stmt2.executeQuery();
                
                        while (rs.next()) {
                            alphabet.add(rs.getString("Symbol").charAt(0));
                        }
                
                    } catch (SQLException ex) {
                       throw new NonRecoverableException("TMDAO-ERR-100", ex);
                    }
                    
                    return alphabet;
                    
                } else {
                     throw new ObjNotFoundException("TMDAO-ERR-101 InconsistentDB alphabet id " + alphabetId + " not found for tmId " + tmId + " type" + type);
                }          
                   
            } else {
                throw new ObjNotFoundException("TMDAO-ERR-102 InconsistentDB alphabet does not exist for TmId: " + tmId + " type " + type);
            }
        } catch (SQLException e) {
            throw new NonRecoverableException("TMDAO-ERR-106 ", e);
        } 
    }

    /*
    private void saveAlphabet(Connection conn, int machineId, String type, ArrayList<Character> symbols) throws SQLException {
        // Logic to save symbols to the `alphabets` and `alphabet_symbols` tables
        String sqlAlphabet = "INSERT INTO alphabets (machine_id, type) VALUES (?, ?)";
        try (PreparedStatement pstmtAlphabet = conn.prepareStatement(sqlAlphabet, Statement.RETURN_GENERATED_KEYS)) {
            pstmtAlphabet.setInt(1, machineId);
            pstmtAlphabet.setString(2, type);
            pstmtAlphabet.executeUpdate();
            try (ResultSet rs = pstmtAlphabet.getGeneratedKeys()) {
                if (rs.next()) {
                    int alphabetId = rs.getInt(1);
                    String sqlSymbols = "INSERT INTO alphabet_symbols (alphabet_id, symbol) VALUES (?, ?)";
                    try (PreparedStatement pstmtSymbols = conn.prepareStatement(sqlSymbols)) {
                        for (Character symbol : symbols) {
                            pstmtSymbols.setInt(1, alphabetId);
                            pstmtSymbols.setString(2, String.valueOf(symbol));
                            pstmtSymbols.executeUpdate();
                        }
                    }
                }
            }
        }
    }
*/
}
