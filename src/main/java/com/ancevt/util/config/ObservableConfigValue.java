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

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a typed accessor for a configuration value stored as a string.
 * <p>
 * This class provides safe parsing methods for various primitive types and common Java objects,
 * returning a default value if parsing fails or the underlying value is {@code null}.
 * <p>
 * Typically used in conjunction with {@link ObservableConfig#get(String)} to provide type-safe access
 * to configuration properties.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * Value value = config.get("timeout");
 * int timeout = value.asInt(30); // default = 30 if parsing fails
 * }</pre>
 *
 */
public class ObservableConfigValue {

    private final String value;

    /**
     * Constructs a {@code Value} wrapper around the given string.
     *
     * @param value the underlying raw string value (may be {@code null})
     */
    public ObservableConfigValue(String value) {
        this.value = value;
    }

    /**
     * Parses the value as an {@code int}, or returns the default if parsing fails.
     *
     * @param defaultValue the fallback value
     * @return the parsed int, or {@code defaultValue} if parsing fails
     */
    public int asInt(int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses the value as a {@code long}, or returns the default if parsing fails.
     *
     * @param defaultValue the fallback value
     * @return the parsed long, or {@code defaultValue} if parsing fails
     */
    public long asLong(long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses the value as a {@code boolean}, or returns the default if parsing fails.
     * Accepted truthy values: "true", "1", "yes", "on" (case-insensitive).
     *
     * @param defaultValue the fallback value
     * @return the parsed boolean, or {@code defaultValue} if parsing fails
     */
    public boolean asBoolean(boolean defaultValue) {
        if (value == null) return defaultValue;
        String v = value.toLowerCase(Locale.ROOT);
        return v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("on");
    }

    /**
     * Parses the value as a {@code double}, or returns the default if parsing fails.
     *
     * @param defaultValue the fallback value
     * @return the parsed double, or {@code defaultValue} if parsing fails
     */
    public double asDouble(double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses the value as a {@code float}, or returns the default if parsing fails.
     *
     * @param defaultValue the fallback value
     * @return the parsed float, or {@code defaultValue} if parsing fails
     */
    public float asFloat(float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses the value as a {@code short}, or returns the default if parsing fails.
     *
     * @param defaultValue the fallback value
     * @return the parsed short, or {@code defaultValue} if parsing fails
     */
    public short asShort(short defaultValue) {
        try {
            return Short.parseShort(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses the value as a {@code byte}, or returns the default if parsing fails.
     *
     * @param defaultValue the fallback value
     * @return the parsed byte, or {@code defaultValue} if parsing fails
     */
    public byte asByte(byte defaultValue) {
        try {
            return Byte.parseByte(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Returns the first character of the value, or the default if the value is empty or null.
     *
     * @param defaultValue the fallback character
     * @return the first character of the string, or {@code defaultValue}
     */
    public char asChar(char defaultValue) {
        try {
            return value.charAt(0);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses the value as an enum constant of the given enum class.
     * The string is converted to upper-case before lookup.
     *
     * @param enumClass    the enum type
     * @param defaultValue the fallback enum constant
     * @param <E>          the enum type
     * @return the corresponding enum constant, or {@code defaultValue}
     */
    public <E extends Enum<E>> E asEnum(Class<E> enumClass, E defaultValue) {
        try {
            return Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses the value as a {@link UUID}, or returns the default if parsing fails.
     *
     * @param defaultValue the fallback UUID
     * @return the parsed UUID, or {@code defaultValue}
     */
    public UUID asUUID(UUID defaultValue) {
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses the value as a {@link Path}, or returns the default if parsing fails.
     *
     * @param defaultValue the fallback path
     * @return the parsed path, or {@code defaultValue}
     */
    public Path asPath(Path defaultValue) {
        try {
            return Paths.get(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses the value as a {@link URI}, or returns the default if parsing fails.
     *
     * @param defaultValue the fallback URI
     * @return the parsed URI, or {@code defaultValue}
     */
    public URI asURI(URI defaultValue) {
        try {
            return URI.create(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses the value as a {@link URL}, or returns the default if parsing fails.
     *
     * @param defaultValue the fallback URL
     * @return the parsed URL, or {@code defaultValue}
     */
    public URL asURL(URL defaultValue) {
        try {
            return new URL(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Parses the value as a {@link BigDecimal}, or returns the default if parsing fails.
     *
     * @param defaultValue the fallback decimal value
     * @return the parsed BigDecimal, or {@code defaultValue}
     */
    public BigDecimal asBigDecimal(BigDecimal defaultValue) {
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Returns the raw string value as-is.
     *
     * @return the underlying value (may be {@code null})
     */
    public String asString() {
        return value;
    }

    /**
     * Returns whether the value is {@code null} or empty.
     *
     * @return {@code true} if the value is empty or {@code null}; {@code false} otherwise
     */
    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }

    /**
     * Returns whether the value is present and not empty.
     *
     * @return {@code true} if the value is not {@code null} or empty
     */
    public boolean isPresent() {
        return !isEmpty();
    }

    /**
     * Returns the value as an {@link Optional}, which is empty if the value is {@code null} or blank.
     *
     * @return an Optional of the value
     */
    public Optional<String> asOptional() {
        return Optional.ofNullable(value).filter(v -> !v.isEmpty());
    }

    /**
     * Returns the string representation of this value.
     *
     * @return the raw value
     */
    @Override
    public String toString() {
        return value;
    }
}
