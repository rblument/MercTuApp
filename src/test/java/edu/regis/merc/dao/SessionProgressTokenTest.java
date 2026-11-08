package edu.regis.merc.dao;

import static org.junit.jupiter.api.Assertions.*;
import edu.regis.merc.model.*;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** A delayed progress save must not restore a revoked or superseded token. */
class SessionProgressTokenTest {
    @Test void progressUpdateTouchesOnlyProgressAndUsesTheSessionRowId() throws Exception {
        for (String storedToken : new String[] {"", "new-login-token"}) {
            Map<Integer, Integer> parameters = new HashMap<>();
            PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {PreparedStatement.class}, (object, method, args) -> {
                        switch (method.getName()) {
                            case "setInt": parameters.put((Integer) args[0], (Integer) args[1]); return null;
                            case "executeUpdate": return 1;
                            case "close": return null;
                            default: throw new AssertionError(method.getName());
                        }
                    });
            Connection connection = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {Connection.class}, (object, method, args) -> {
                        assertEquals("prepareStatement", method.getName());
                        assertEquals("UPDATE TutoringSession SET CourseId = ?, UnitId = ?, ProblemId = ? WHERE Id = ?", args[0]);
                        return statement;
                    });
            TutoringSession stale = new TutoringSession(new Student(new Account("student@example.com")));
            stale.setId(42);
            stale.setSecurityToken("old-token");
            stale.setCourse(new CourseDigest(3));
            stale.setUnit(new UnitDigest(4));
            stale.setProblem(new Problem(7));
            java.lang.reflect.Method update = SessionDAO.class.getDeclaredMethod("update", TutoringSession.class, Connection.class);
            update.setAccessible(true);
            update.invoke(new SessionDAO(), stale, connection);
            assertEquals(Map.of(1, 3, 2, 4, 3, 7, 4, 42), parameters);
            assertNotEquals(storedToken, stale.getSecurityToken());
        }
    }
}
