package edu.regis.merc.svc;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.function.LongSupplier;

/**
 * Remembers which credentials passed the preliminary lookup. A hit still needs
 * to match the token on the session freshly loaded from the database.
 */
final class TokenValidationCache {
    static final int MAX_ENTRIES = 1024;
    static final Duration RETENTION = Duration.ofMinutes(1);

    private final LinkedHashMap<String, Entry> entries = new LinkedHashMap<>(16, 0.75f, true);
    private final int maxEntries;
    private final long retentionNanos;
    private final LongSupplier ticker;
    private long revision;

    TokenValidationCache(int maxEntries, Duration retention, LongSupplier ticker) {
        this.maxEntries = maxEntries;
        retentionNanos = retention.toNanos();
        this.ticker = ticker;
    }

    synchronized boolean contains(String userId, String fingerprint) {
        Entry entry = entries.get(userId);
        if (entry == null) return false;
        if (ticker.getAsLong() - entry.writtenAt() >= retentionNanos) {
            entries.remove(userId);
            return false;
        }
        return SecurityTokens.matches(entry.fingerprint(), fingerprint);
    }

    synchronized long revision() {
        return revision;
    }

    synchronized void remember(String userId, String fingerprint, long observedRevision) {
        // Validation may have started before a token mutation committed.
        if (observedRevision != revision) return;
        entries.put(userId, new Entry(fingerprint, ticker.getAsLong()));
        if (entries.size() > maxEntries) entries.pollFirstEntry();
    }

    synchronized void invalidate(String userId) {
        revision++;
        entries.remove(userId);
    }

    private record Entry(String fingerprint, long writtenAt) { }
}
