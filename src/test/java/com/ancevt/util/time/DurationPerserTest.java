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

package com.ancevt.util.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DurationPerserTest {
    @Test
    @DisplayName("Parse seconds")
    void testSeconds() {
        assertEquals(1000, DurationParser.parseMillis("1s"));
        assertEquals(5000, DurationParser.parseMillis("5s"));
        assertEquals(1, DurationParser.parseSeconds("1s"));
    }

    @Test
    @DisplayName("Parse minutes and hours")
    void testMinutesAndHours() {
        assertEquals(60_000, DurationParser.parseMillis("1m"));
        assertEquals(3_600_000, DurationParser.parseMillis("1h"));
        assertEquals(5400, DurationParser.parseSeconds("1h30m"));
    }

    @Test
    @DisplayName("Parse days")
    void testDays() {
        assertEquals(86_400_000, DurationParser.parseMillis("1d"));
        assertEquals(2 * 86_400_000, DurationParser.parseMillis("2d"));
    }

    @Test
    @DisplayName("Parse milliseconds explicitly")
    void testMilliseconds() {
        assertEquals(123, DurationParser.parseMillis("123ms"));
        assertEquals(0, DurationParser.parseMillis("ms")); // no number → 0
    }

    @Test
    @DisplayName("Parse combination of units")
    void testCombination() {
        assertEquals(3_600_000 + 30_000, DurationParser.parseMillis("1h30s"));
        assertEquals(2 * 86_400_000 + 5 * 3_600_000 + 20_000,
                DurationParser.parseMillis("2d5h20s"));
    }

    @Test
    @DisplayName("Parse without suffix defaults to millis")
    void testWithoutSuffix() {
        assertEquals(5000, DurationParser.parseMillis("5000"));
    }

    @Test
    @DisplayName("Case insensitive parsing")
    void testCaseInsensitive() {
        assertEquals(1000, DurationParser.parseMillis("1S"));
        assertEquals(3600000, DurationParser.parseMillis("1H"));
        assertEquals(60000, DurationParser.parseMillis("1M")); // minute
    }

    @Test
    @DisplayName("Invalid inputs should throw")
    void testInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parseMillis(null));
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parseMillis(""));
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parseMillis("10x"));
    }

    @Test
    @DisplayName("Zero values should work")
    void testZeroValues() {
        assertEquals(0, DurationParser.parseMillis("0s"));
        assertEquals(0, DurationParser.parseMillis("0h"));
        assertEquals(0, DurationParser.parseMillis("0"));
    }
}
