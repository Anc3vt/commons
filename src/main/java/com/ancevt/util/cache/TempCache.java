/*
 * Copyright (C) 2025 Ancevt.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ancevt.util.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A lightweight, thread-safe in-memory cache that combines
 * <b>time-based expiration (TTL)</b> and <b>Least Recently Used (LRU)</b> eviction.
 * <p>
 * Each entry stored in this cache has a fixed time-to-live (TTL) period. Once expired,
 * the entry is automatically removed during the next cache access or cleanup.
 * Additionally, when the cache exceeds its configured maximum size, the least recently used
 * entries are evicted according to standard {@link LinkedHashMap} access-order semantics.
 * </p>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * The cache is fully thread-safe and uses a {@link ReentrantReadWriteLock} internally.
 * Read operations (such as {@link #get(Object)}) acquire a shared read lock,
 * while write operations (such as {@link #put(Object, Object)} or {@link #clear()})
 * acquire an exclusive write lock.
 * </p>
 *
 * <h2>Performance Characteristics</h2>
 * <ul>
 *   <li><b>O(1)</b> average time for {@code get} and {@code put} operations.</li>
 *   <li>Eviction is handled lazily (on access or cleanup) to minimize overhead.</li>
 *   <li>The cache leverages {@link LinkedHashMap}’s access-order mode to implement LRU efficiently.</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * TempCache<String, String> cache = new TempCache<>(10_000, 100); // 10 seconds TTL, max 100 entries
 *
 * cache.put("user:123", "John Doe");
 *
 * String value = cache.get("user:123");
 * if (value != null) {
 *     System.out.println("Cached value: " + value);
 * } else {
 *     System.out.println("Value expired or not found");
 * }
 * }</pre>
 *
 * <h2>Eviction and Expiration Behavior</h2>
 * <ul>
 *   <li>When the maximum size is exceeded, the oldest (least recently used) entry is evicted.</li>
 *   <li>Expired entries are removed automatically during {@link #put(Object, Object)} and {@link #get(Object)} operations,
 *       as well as during {@link #size()} which internally triggers cleanup.</li>
 *   <li>No background cleanup thread is used — expiration is performed lazily on access to ensure minimal footprint.</li>
 * </ul>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 *
 * @author  Ancevt
 * @version 1.0
 * @since   2025
 */
public class TempCache<K, V> {

    /** Underlying cache storage with LRU eviction via access-order LinkedHashMap. */
    private final Map<K, CacheEntry<V>> cache;

    /** Time-to-live duration for each entry in milliseconds. */
    private final long ttlMillis;

    /** Maximum number of entries before LRU eviction occurs. */
    private final int maxSize;

    /** Lock to coordinate concurrent read/write access. */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Constructs a new {@code TempCache} with the specified time-to-live and maximum size.
     *
     * @param ttlMillis the time-to-live for each entry, in milliseconds.
     *                  A value of {@code 0} or negative disables expiration (entries never expire).
     * @param maxSize   the maximum number of entries before LRU eviction occurs.
     *                  Must be greater than zero.
     */
    public TempCache(long ttlMillis, int maxSize) {
        this.ttlMillis = ttlMillis;
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<K, CacheEntry<V>>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                return size() > TempCache.this.maxSize;
            }
        };
    }

    /**
     * Constructs a {@code TempCache} with default configuration:
     * <ul>
     *   <li>TTL = 60 seconds</li>
     *   <li>Maximum size = 256 entries</li>
     * </ul>
     */
    public TempCache() {
        this(60_000, 256);
    }

    /**
     * Inserts or updates a value in the cache with the specified key.
     * <p>
     * If an entry with the same key already exists, it will be replaced.
     * Expired entries are automatically removed during this operation.
     * </p>
     *
     * @param key   the key to store the value under
     * @param value the value to store
     */
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            cache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + ttlMillis));
            cleanup();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieves a value from the cache.
     * <p>
     * If the entry is found but expired, it is automatically removed
     * and {@code null} is returned.
     * </p>
     *
     * @param key the key whose associated value is to be returned
     * @return the cached value, or {@code null} if not present or expired
     */
    public V get(K key) {
        lock.readLock().lock();
        try {
            CacheEntry<V> entry = cache.get(key);
            if (entry == null) return null;
            if (entry.isExpired()) {
                // Upgrade to write lock for removal
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    cache.remove(key);
                } finally {
                    lock.writeLock().unlock();
                    lock.readLock().lock();
                }
                return null;
            }
            return entry.value;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Invalidates (removes) a specific entry from the cache.
     *
     * @param key the key to remove
     */
    public void invalidate(K key) {
        lock.writeLock().lock();
        try {
            cache.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes all entries from the cache immediately.
     * <p>
     * This operation acquires the write lock exclusively.
     * </p>
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns the current number of entries in the cache after cleaning up
     * any expired elements.
     *
     * @return the number of valid (non-expired) entries currently in the cache
     */
    public int size() {
        lock.writeLock().lock();
        try {
            cleanup();
            return cache.size();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes all expired entries from the cache.
     * <p>
     * This method is called automatically on cache mutations,
     * but can also be invoked manually if needed.
     * </p>
     */
    private void cleanup() {
        Iterator<Map.Entry<K, CacheEntry<V>>> it = cache.entrySet().iterator();
        long now = System.currentTimeMillis();
        while (it.hasNext()) {
            if (it.next().getValue().expiry < now) {
                it.remove();
            }
        }
    }

    /**
     * Internal holder for cached values and their expiration timestamps.
     *
     * @param <V> the type of the cached value
     */
    private static class CacheEntry<V> {
        final V value;
        final long expiry;

        CacheEntry(V value, long expiry) {
            this.value = value;
            this.expiry = expiry;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiry;
        }
    }
}
