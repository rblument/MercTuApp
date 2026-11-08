package edu.regis.merc.dao;

import edu.regis.merc.err.NonRecoverableException;
import edu.regis.merc.err.ObjNotFoundException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Exercise the actual DAO token methods against a recording JDBC boundary. */
class SessionTokenDAOTest {
    private final class Database extends SessionDAO {
        private String sql;
        private String token = "old-token";
        private boolean fail;
        private int calls;
        private final Map<Integer, String> params = new HashMap<>();

        @Override Connection tokenConnection() throws SQLException {
            if (fail) throw new SQLException("Sensitive database diagnostic");
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Connection.class},
                    (object, method, args) -> {
                        if (method.getName().equals("prepareStatement")) {
                            sql = (String) args[0];
                            params.clear();
                            calls++;
                            return statement();
                        }
                        if (method.getName().equals("close")) return null;
                        throw new AssertionError(method.getName());
                    });
        }

        private PreparedStatement statement() {
            return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {PreparedStatement.class},
                    (object, method, args) -> {
                        switch (method.getName()) {
                            case "setString": params.put((Integer) args[0], (String) args[1]); return null;
                            case "close": return null;
                            case "executeUpdate":
                                if (sql.contains("BINARY SecurityToken")) {
                                    if (!token.equals(params.get(2))) return 0;
                                    token = "";
                                } else {
                                    token = params.get(1);
                                }
                                return 1;
                            default: throw new AssertionError(method.getName());
                        }
                    });
        }
    }

    @Test void revocationIsConditionalCaseSensitiveAndTouchesOnlyToken() throws Exception {
        Database db = new Database();
        assertFalse(db.revokeSecurityToken("student@example.com", "OLD-TOKEN"));
        assertEquals("old-token", db.token);
        assertTrue(db.revokeSecurityToken("student@example.com", "old-token"));
        assertEquals("", db.token);
        assertEquals("UPDATE TutoringSession SET SecurityToken = '' WHERE UserId = ? AND BINARY SecurityToken = BINARY ?", db.sql);
        assertEquals("student@example.com", db.params.get(1));
        assertEquals("old-token", db.params.get(2));
    }

    @Test void rotationUpdatesOnlyTokenAndDelayedLogoutCannotReplaceIt() throws Exception {
        Database db = new Database();
        db.replaceSecurityToken("student@example.com", "new-token");
        assertEquals("UPDATE TutoringSession SET SecurityToken = ? WHERE UserId = ?", db.sql);
        assertEquals("new-token", db.token);
        assertFalse(db.revokeSecurityToken("student@example.com", "old-token"));
        assertEquals("new-token", db.token);
    }

    @Test void blankTokensCannotRevokeOrBeInstalled() throws Exception {
        Database db = new Database();
        assertFalse(db.revokeSecurityToken("student@example.com", ""));
        assertThrows(IllegalArgumentException.class, () -> db.replaceSecurityToken("student@example.com", " "));
        assertEquals(0, db.calls);
    }

    @Test void databaseErrorsAreReportedWithoutSensitiveDiagnostics() {
        Database db = new Database();
        db.fail = true;
        NonRecoverableException error = assertThrows(NonRecoverableException.class,
                () -> db.revokeSecurityToken("student@example.com", "old-token"));
        assertFalse(error.getMessage().contains("Sensitive"));
        assertEquals("old-token", db.token);
        assertThrows(NonRecoverableException.class, () -> db.replaceSecurityToken("student@example.com", "new-token"));
    }
}
