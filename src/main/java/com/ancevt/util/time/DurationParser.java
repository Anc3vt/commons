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

import java.time.Duration;

/**
 * Utility class for parsing human-readable time patterns into {@link Duration}.
 * <p>
 * Supported suffixes (case-insensitive):
 * <ul>
 *     <li>{@code d}  - days</li>
 *     <li>{@code h}  - hours</li>
 *     <li>{@code m}  - minutes (unless followed by {@code s})</li>
 *     <li>{@code s}  - seconds</li>
 *     <li>{@code ms} - milliseconds</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <pre>
 * DurationParser.parse("10s")      → 10 seconds
 * DurationParser.parse("1h30m")    → 1 hour 30 minutes
 * DurationParser.parse("2d5h20s")  → 2 days, 5 hours, 20 seconds
 * DurationParser.parse("5000")     → 5000 milliseconds
 * </pre>
 *
 * <p>Invalid inputs throw {@link IllegalArgumentException}.</p>
 */
public final class DurationParser {

    private static final long MILLIS = 1;
    private static final long SECOND = 1000 * MILLIS;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;

    private DurationParser() {
        // utility class
    }

    /**
     * Parses a human-readable time string into a {@link Duration}.
     *
     * @param input time pattern (e.g. "2h30m", "5000", "1d12h")
     * @return parsed {@link Duration}
     * @throws IllegalArgumentException if input is null, empty, or invalid
     */
    public static Duration parse(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }

        long millis = 0L;
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = Character.toLowerCase(input.charAt(i));

            if (Character.isDigit(c)) {
                number.append(c);
                continue;
            }

            if (c == 'm' && i < input.length() - 1 && Character.toLowerCase(input.charAt(i + 1)) == 's') {
                millis += parseNumber(number) * MILLIS;
                i++; // skip 's'
            } else if (c == 's') {
                millis += parseNumber(number) * SECOND;
            } else if (c == 'm') {
                millis += parseNumber(number) * MINUTE;
            } else if (c == 'h') {
                millis += parseNumber(number) * HOUR;
            } else if (c == 'd') {
                millis += parseNumber(number) * DAY;
            } else {
                throw new IllegalArgumentException("Invalid time suffix at position " + i + ": " + c);
            }
        }

        // If input had only digits → treat as millis
        if (millis == 0 && number.length() > 0) {
            millis = parseNumber(number);
        }

        return Duration.ofMillis(millis);
    }

    private static long parseNumber(StringBuilder sb) {
        if (sb.length() == 0) return 0L;
        long value = Long.parseLong(sb.toString());
        sb.setLength(0);
        return value;
    }

    /**
     * Convenience method to parse string directly into milliseconds.
     *
     * @param input time string
     * @return total milliseconds
     */
    public static long parseMillis(String input) {
        return parse(input).toMillis();
    }

    /**
     * Convenience method to parse string directly into seconds.
     *
     * @param input time string
     * @return total seconds
     */
    public static long parseSeconds(String input) {
        return parse(input).getSeconds();
    }
}
