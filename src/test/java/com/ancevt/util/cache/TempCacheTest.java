package com.ancevt.util.cache;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TempCache}.
 */
class TempCacheTest {

    @Test
    void testPutAndGet() {
        TempCache<String, String> cache = new TempCache<>(5000, 10);
        cache.put("key", "value");

        assertEquals("value", cache.get("key"));
        assertEquals(1, cache.size());
    }

    @Test
    void testExpiration() throws InterruptedException {
        TempCache<String, String> cache = new TempCache<>(100, 10);
        cache.put("foo", "bar");

        assertEquals("bar", cache.get("foo"));

        Thread.sleep(150); // wait for TTL to expire

        assertNull(cache.get("foo"), "Value should expire after TTL");
        assertEquals(0, cache.size());
    }

    @Test
    void testNoExpirationWhenTtlZero() throws InterruptedException {
        TempCache<String, String> cache = new TempCache<>(0, 10);
        cache.put("foo", "bar");

        Thread.sleep(100);
        assertEquals("bar", cache.get("foo"), "Value should not expire when TTL <= 0");
    }

    @Test
    void testInvalidate() {
        TempCache<String, String> cache = new TempCache<>(1000, 10);
        cache.put("a", "b");
        cache.invalidate("a");

        assertNull(cache.get("a"));
        assertEquals(0, cache.size());
    }

    @Test
    void testClear() {
        TempCache<String, String> cache = new TempCache<>(1000, 10);
        cache.put("a", "b");
        cache.put("c", "d");

        assertEquals(2, cache.size());

        cache.clear();

        assertEquals(0, cache.size());
        assertNull(cache.get("a"));
    }

    @Test
    void testLruEviction() {
        TempCache<Integer, String> cache = new TempCache<>(1000, 3);

        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");
        cache.get(1); // access 1 to make it most recently used
        cache.put(4, "four"); // should evict 2 (least recently used)

        assertNull(cache.get(2), "Least recently used entry should be evicted");
        assertNotNull(cache.get(1));
        assertNotNull(cache.get(3));
        assertNotNull(cache.get(4));

        assertEquals(3, cache.size());
    }

    @Test
    void testCleanupRemovesExpiredEntries() throws InterruptedException {
        TempCache<String, String> cache = new TempCache<>(50, 10);
        cache.put("a", "b");
        cache.put("c", "d");

        Thread.sleep(100);
        cache.size(); // triggers cleanup

        assertEquals(0, cache.size());
        assertNull(cache.get("a"));
        assertNull(cache.get("c"));
    }

    @Test
    void testThreadSafetyUnderConcurrentAccess() throws InterruptedException {
        TempCache<Integer, Integer> cache = new TempCache<>(1000, 1000);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        AtomicInteger counter = new AtomicInteger();

        Runnable writer = () -> {
            for (int i = 0; i < 1000; i++) {
                cache.put(i, i);
                counter.incrementAndGet();
            }
        };

        Runnable reader = () -> {
            for (int i = 0; i < 1000; i++) {
                cache.get(ThreadLocalRandom.current().nextInt(1000));
            }
        };

        for (int i = 0; i < 4; i++) executor.submit(writer);
        for (int i = 0; i < 4; i++) executor.submit(reader);

        executor.shutdown();
        assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS));

        assertTrue(cache.size() <= 1000, "Cache should not exceed max size");
        assertTrue(counter.get() >= 4000, "Writers should execute fully without exception");
    }

    @Test
    void testSizeReflectsCleanup() throws InterruptedException {
        TempCache<Integer, String> cache = new TempCache<>(50, 10);

        cache.put(1, "a");
        cache.put(2, "b");

        Thread.sleep(100);
        int size = cache.size();

        assertEquals(0, size, "Expired entries should not count toward size()");
    }

    @Test
    void testOverwriteUpdatesEntry() {
        TempCache<String, String> cache = new TempCache<>(1000, 10);
        cache.put("x", "1");
        cache.put("x", "2");

        assertEquals("2", cache.get("x"), "Value should be overwritten");
        assertEquals(1, cache.size());
    }

    @Test
    void testAccessOrderAffectsEviction() {
        TempCache<Integer, String> cache = new TempCache<>(1000, 3);

        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");

        // Access 1, so it becomes most recently used
        cache.get(1);

        // Add another entry; should evict 2 (least recently used)
        cache.put(4, "four");

        assertNull(cache.get(2), "Key 2 should be evicted");
        assertNotNull(cache.get(1), "Key 1 should remain");
        assertNotNull(cache.get(3));
        assertNotNull(cache.get(4));
    }
}
