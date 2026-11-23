package edu.regis.merc.svc;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenValidationCacheTest {
    private final AtomicLong now = new AtomicLong();
    private final TokenValidationCache cache = new TokenValidationCache(2, Duration.ofSeconds(10), now::get);

    @Test void entriesAreScopedToTheUserAndExpireEvenOnHits() {
        cache.remember("alice", "digest", cache.revision());
        assertTrue(cache.contains("alice", "digest"));
        assertFalse(cache.contains("bob", "digest"));
        assertFalse(cache.contains("alice", "different"));
        now.set(Duration.ofSeconds(9).toNanos());
        assertTrue(cache.contains("alice", "digest"));
        now.set(Duration.ofSeconds(10).toNanos());
        assertFalse(cache.contains("alice", "digest"));
    }

    @Test void evictsTheLeastRecentlyUsedEntryAtTheSizeLimit() {
        cache.remember("alice", "a", cache.revision());
        cache.remember("bob", "b", cache.revision());
        assertTrue(cache.contains("alice", "a"));
        cache.remember("carol", "c", cache.revision());
        assertFalse(cache.contains("bob", "b"));
        assertTrue(cache.contains("alice", "a"));
        assertTrue(cache.contains("carol", "c"));
    }

    @Test void invalidationWinsOverAConcurrentValidationThatStartedEarlier() throws Exception {
        CountDownLatch validationStarted = new CountDownLatch(1);
        CountDownLatch mutationCommitted = new CountDownLatch(1);
        try (var executor = Executors.newSingleThreadExecutor()) {
            var stale = executor.submit(() -> {
                long revision = cache.revision();
                validationStarted.countDown();
                assertTrue(mutationCommitted.await(5, TimeUnit.SECONDS));
                cache.remember("alice", "old", revision);
                return cache.contains("alice", "old");
            });
            try {
                assertTrue(validationStarted.await(5, TimeUnit.SECONDS));
                cache.invalidate("alice");
                cache.remember("alice", "new", cache.revision());
            } finally {
                mutationCommitted.countDown();
            }
            assertFalse(stale.get(5, TimeUnit.SECONDS));
            assertTrue(cache.contains("alice", "new"));
        }
    }
}
