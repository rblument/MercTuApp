package edu.regis.merc.svc;

import java.util.IdentityHashMap;
import java.util.Map;

/** Keeps exception types and stack traces without messages that may contain request data or configuration values. */
final class ConnectionDiagnostics {
    private ConnectionDiagnostics() { }

    static Throwable safeCause(Throwable failure) {
        return safeCause(failure, new IdentityHashMap<>());
    }

    private static Throwable safeCause(Throwable failure, Map<Throwable, Throwable> seen) {
        if (seen.containsKey(failure)) return seen.get(failure);
        Throwable safe = new Throwable(failure.getClass().getName());
        safe.setStackTrace(failure.getStackTrace());
        seen.put(failure, safe);
        if (failure.getCause() != null) safe.initCause(safeCause(failure.getCause(), seen));
        for (Throwable suppressed : failure.getSuppressed()) {
            if (suppressed != failure) safe.addSuppressed(safeCause(suppressed, seen));
        }
        return safe;
    }
}
