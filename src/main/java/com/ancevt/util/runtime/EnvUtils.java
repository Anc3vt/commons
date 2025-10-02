package com.ancevt.util.runtime;

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

import java.util.Locale;

/**
 * Utility class for accessing environment variables in a type-safe and convenient way.
 * <p>
 * Supports typed fallback values and enum parsing.
 */
public final class EnvUtils {

    private EnvUtils() {
    }

    /**
     * Returns the raw value of the environment variable or {@code null} if not defined.
     *
     * @param name the name of the environment variable
     * @return the string value or {@code null}
     */
    public static String get(String name) {
        return System.getenv(name);
    }

    /**
     * Returns the value of the environment variable, or the given default if not defined.
     *
     * @param name         the name of the environment variable
     * @param defaultValue the fallback value
     * @return the value or {@code defaultValue}
     */
    public static String get(String name, String defaultValue) {
        String value = get(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Returns the integer value of the environment variable, or the default if missing or invalid.
     *
     * @param name         the name of the environment variable
     * @param defaultValue the fallback value
     * @return parsed int or {@code defaultValue}
     */
    public static int getInt(String name, int defaultValue) {
        String value = get(name);
        try {
            return value != null ? Integer.parseInt(value.trim()) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Returns the boolean value of the environment variable.
     * Accepts {@code true, 1, yes, on} as truthy values (case-insensitive).
     *
     * @param name         the name of the environment variable
     * @param defaultValue fallback if not found or invalid
     * @return parsed boolean or {@code defaultValue}
     */
    public static boolean getBoolean(String name, boolean defaultValue) {
        String value = get(name);
        if (value == null) return defaultValue;
        String v = value.trim().toLowerCase(Locale.ROOT);
        return v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("on");
    }

    /**
     * Returns the enum value of the environment variable.
     *
     * @param name         the name of the environment variable
     * @param enumClass    the enum class type
     * @param defaultValue fallback if not found or invalid
     * @param <E>          enum type
     * @return parsed enum constant or {@code defaultValue}
     */
    public static <E extends Enum<E>> E getEnum(String name, Class<E> enumClass, E defaultValue) {
        String value = get(name);
        if (value == null) return defaultValue;
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Returns the long value of the environment variable, or the default if missing or invalid.
     *
     * @param name         the name of the environment variable
     * @param defaultValue fallback value
     * @return parsed long or {@code defaultValue}
     */
    public static long getLong(String name, long defaultValue) {
        String value = get(name);
        try {
            return value != null ? Long.parseLong(value.trim()) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Returns the double value of the environment variable, or the default if missing or invalid.
     *
     * @param name         the name of the environment variable
     * @param defaultValue fallback value
     * @return parsed double or {@code defaultValue}
     */
    public static double getDouble(String name, double defaultValue) {
        String value = get(name);
        try {
            return value != null ? Double.parseDouble(value.trim()) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

