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

package com.ancevt.util.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ObservableConfigTest {

    private ObservableConfig config;

    @BeforeEach
    void setUp() {
        config = new ObservableConfig();
    }

    @Test
    void testPutAndGet() {
        config.put("key1", "value1");
        assertEquals("value1", config.getAsString("key1"));
        assertEquals("value1", config.get("key1").asString());
    }

    @Test
    void testPutAll() {
        Map<String, String> source = new HashMap<>();
        source.put("a", "1");
        source.put("b", "2");

        config.putAll(source);

        assertEquals("1", config.getAsString("a"));
        assertEquals("2", config.getAsString("b"));
    }

    @Test
    void testRemove() {
        config.put("key", "value");
        assertTrue(config.containsKey("key"));

        config.remove("key");

        assertFalse(config.containsKey("key"));
        assertNull(config.getAsString("key"));
    }

    @Test
    void testClear() {
        config.put("x", "1");
        config.put("y", "2");
        config.clear();
        assertTrue(config.toMap().isEmpty());
    }

    @Test
    void testChangeListenerIsTriggered() {
        AtomicInteger counter = new AtomicInteger(0);

        config.addChangeListener((key, oldVal, newVal) -> counter.incrementAndGet());

        config.put("a", "1");
        config.put("a", "2"); // triggers again
        config.remove("a");

        assertEquals(3, counter.get());
    }

    @Test
    void testChangeListenerNotTriggeredWhenSuppressed() {
        AtomicInteger counter = new AtomicInteger(0);

        config.addChangeListener((key, oldVal, newVal) -> counter.incrementAndGet());

        config.setSuppressEvents(true);
        config.put("a", "1");
        config.remove("a");
        config.setSuppressEvents(false);

        assertEquals(0, counter.get());
    }

    @Test
    void testGetAsStringWithDefault() {
        assertEquals("default", config.getAsString("missing", "default"));
    }

    @Test
    void testToMapIsUnmodifiable() {
        config.put("key", "value");
        Map<String, String> map = config.toMap();

        assertThrows(UnsupportedOperationException.class, () -> map.put("x", "y"));
    }
}
