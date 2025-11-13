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

import edu.regis.merc.model.MoveKind;
import edu.regis.merc.model.State;
import edu.regis.merc.model.Transition;
import edu.regis.merc.model.TuringMachine;
import edu.regis.merc.svc.TuringMachingSvc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TuringMachineDAO extends MySqlDAO implements TuringMachingSvc {
    public TuringMachineDAO() {
        super();
    }

    ///
    /// Machine Methods
    ///

    @Override
    public int createMachine(TuringMachine machine) {
        final String insertMachineSql =
                "INSERT INTO TuringMachine(name, description) VALUES (?, ?)";

        final String updateMachineSql =
                "UPDATE TuringMachine SET start_state_id=?, accept_state_id=?, reject_state_id=? WHERE machine_id=?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(insertMachineSql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, machine.getTitle());
            stmt.setString(2, machine.getDescription());
            stmt.executeUpdate();

            int machineId;
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                machineId = rs.getInt(1);
                machine.setId(machineId);
            }


            // 1. Save States and get their IDs
            int startStateId = saveState(conn, machineId, machine.getStartState());


            for (State s : machine.getStates()) {
                if (s != machine.getStartState() && s != machine.getAcceptState() && s != machine.getRejectState()) {
                    saveState(conn, machineId, s);
                }
            }

            PreparedStatement updateStmt = conn.prepareStatement(updateMachineSql);
            updateStmt.setInt(1, startStateId);
            if (machine.getAcceptState() == null) {
                updateStmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                int acceptStateId = saveState(conn, machine.getId(), machine.getAcceptState());
                updateStmt.setInt(2, acceptStateId);
            }

            if (machine.getRejectState() == null) {
                updateStmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                int rejectStateId = saveState(conn, machine.getId(), machine.getRejectState());
                updateStmt.setInt(3, rejectStateId);
            }
            updateStmt.setInt(4, machineId);
            updateStmt.executeUpdate();


            saveAlphabet(conn, machineId, "input", machine.getInputAlphabet());
            saveAlphabet(conn, machineId, "tape", machine.getTapeAlphabet());

            return machineId;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TuringMachine getMachineById(int id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        String query = "Select machine_id, name, description, start_state_id, accept_state_id, reject_state_id FROM TuringMachine WHERE machine_id = ?";

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("name");
                        String description = rs.getString("description");

                        State start = getStateById(conn, rs.getInt("start_state_id"));
                        State accept = getStateById(conn, rs.getInt("accept_state_id"));
                        State reject = getStateById(conn, rs.getInt("reject_state_id"));

                        ArrayList<Character> inputAlphabet = getAlphabet(conn, id, "input");
                        ArrayList<Character> tapeAlphabet = getAlphabet(conn, id, "tape");

                        return new TuringMachine(id, name, description, inputAlphabet, tapeAlphabet, start, accept, reject);
                    }
            }catch (SQLException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            close(conn, stmt);
        }


        return null;
    }


    @Override
    public List<TuringMachine> getAllMachines() {
        List<TuringMachine> machines = new ArrayList<>();
        final String sql = "SELECT machine_id FROM TuringMachine";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();

             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TuringMachine tm = getMachineById(rs.getInt("id"));
                if (tm != null) machines.add(tm);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return machines;
    }


    @Override
    public boolean deleteMachine(int id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = "DELETE FROM TuringMachine WHERE machine_id=?";

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
            throw new RuntimeException(e);
        }

    }



    ///
    /// State Methods
    ///

    @Override
    public int createState(State state) {
        Connection conn = null;
        PreparedStatement stmt = null;
        final String sql = "INSERT INTO State(machine_id, name, is_start, is_accept) VALUES (?, ?, ?, ?)";

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

//            stmt.setInt(1, state.getMachineId());
            stmt.setString(2, state.getName());

            int affected = stmt.executeUpdate();
            if (affected == 0) return 0;

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }catch (SQLException e) {
                throw new RuntimeException("Error creating state", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public int updateState(State state) {
        Connection conn = null;
        PreparedStatement stmt = null;
        final String sql = "UPDATE State SET name = ?, is_start = ?, is_accept = ? WHERE state_id = ?";


        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, state.getName());
//            stmt.setInt(4, state.getStateId());

            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating state", e);
        }
    }

    private int saveState(Connection conn, int machineId, State state) throws SQLException {
        if (state == null) return 0;
        final String sql = "INSERT INTO State(machine_id, name) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, machineId);
            stmt.setString(2, state.getName());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) { int id = rs.getInt(1);
                    state.setId(id); return id;
                }
            }
        }
        return 0;
    }

    public List<State> getStatesByMachine(int machineId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        final String query = "SELECT * FROM State WHERE machine_id = ?";
        List<State> states = new ArrayList<>();

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(query);

            stmt.setInt(1, machineId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    states.add(new State(
                            rs.getInt("state_id"),
                            rs.getInt("machine_id"),
                            rs.getString("name"))


                    );

                }
            }
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return states;
    }

    private State getStateById(Connection conn, int stateId) throws SQLException {
        final String sql = "SELECT * FROM State WHERE state_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stateId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                State s = new State(rs.getString("name"));
                s.setId(rs.getInt("state_id"));
                return s;
            }
            //else throw inconsistent db error

        }
        return null;
    }



    ///
    ///  Transition Methods
    ///

    @Override
    public int createTransition(Transition transition) {
        Connection conn = null;
        PreparedStatement stmt = null;
        final String sql = "INSERT INTO Transition(machine_id, read, write, NextStateID,DirectionID) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, transition.getId());
            stmt.setString(2, String.valueOf(transition.getRead()));
            stmt.setString(3, String.valueOf(transition.getWrite()));
            stmt.setString(5, transition.getDirection().name());
            stmt.setInt(4, transition.getNextState().getStateId());

            int affected = stmt.executeUpdate();
            if (affected == 0) return 0;

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error creating transition", e);
        }
        return 0;
    }


    @Override
    public List<Transition> getTransitionsByMachine(int machineId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        final String query = "SELECT * FROM Transition WHERE machine_id = ?";
        List<Transition> transitions = new ArrayList<>();


        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.prepareStatement(query);

            stmt.setInt(1, machineId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transitions.add(new Transition(
                            rs.getString("read").charAt(0),
                            rs.getString("write").charAt(0),
                            MoveKind.valueOf(rs.getString("move_direction")),
                            new State(rs.getInt("NextStateID"))
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching transitions by machine", e);
        }
        return transitions;
    }



    ///
    ///  Helper Classes
    ///

//    private int saveState(Connection conn, State state) throws SQLException {
//        // Logic to insert a State into the `states` table and return its ID
//        System.out.println("state info" + state.getStateId());
//        String sql = "INSERT INTO state (name, is_start, is_accept, is_reject) VALUES (?, ?, ?, ?)";
//        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            // Set parameters based on the State object
//            pstmt.setString(1, state.getName());
//            pstmt.executeUpdate();
//            try (ResultSet rs = pstmt.getGeneratedKeys()) {
//                if (rs.next()) {
//                    return rs.getInt(1);
//                }
//            }
//        }
//        throw new SQLException("Failed to save state.");
//    }

    private ArrayList<Character> getAlphabet(Connection conn, int machineId, String type) throws SQLException {
        ArrayList<Character> alphabet = new ArrayList<>();
        // 1. Find the alphabet_id from the alphabets table
        final String sqlAlphabetId = "SELECT id FROM alphabets WHERE machine_id = ? AND type = ?";
        int alphabetId = 0;

        try (PreparedStatement pstmtAlphabetId = conn.prepareStatement(sqlAlphabetId)) {
            pstmtAlphabetId.setInt(1, machineId);
            pstmtAlphabetId.setString(2, type);
            try (ResultSet rsId = pstmtAlphabetId.executeQuery()) {
                if (rsId.next()) {
                    alphabetId = rsId.getInt("id");
                }
            }
        }

        // 2. Retrieve symbols using the alphabet_id
        if (alphabetId > 0) {
            final String sqlSymbols = "SELECT symbol FROM alphabet_symbols WHERE alphabet_id = ?";
            try (PreparedStatement pstmtSymbols = conn.prepareStatement(sqlSymbols)) {
                pstmtSymbols.setInt(1, alphabetId);
                try (ResultSet rsSymbols = pstmtSymbols.executeQuery()) {
                    while (rsSymbols.next()) {
                        String symbolStr = rsSymbols.getString("symbol");
                        // Assuming the symbol is a single character
                        if (symbolStr != null && symbolStr.length() == 1) {
                            alphabet.add(symbolStr.charAt(0));
                        }
                    }
                }
            }
        }

        return alphabet;
    }

    private void saveAlphabet(Connection conn, int machineId, String type, ArrayList<Character> symbols) throws SQLException {
        if (symbols == null || symbols.isEmpty()) {
            return;
        }

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
}
