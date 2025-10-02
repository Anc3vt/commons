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

package com.ancevt.util.runtime;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class for detecting platform-specific properties.
 * <p>
 * This includes operating system, paths for config/data/temp directories,
 * system architecture, locale, timezone, and JVM details.
 * <p>
 * All methods are static and the class is non-instantiable.
 */
public final class PlatformUtils {

    /**
     * Enumeration of common operating systems.
     */
    public enum OperatingSystem {
        WINDOWS, MACOS, UNIX_LIKE, SOLARIS, FREEBSD, HPUX, OTHER
    }

    /**
     * Detects the current operating system.
     *
     * @return the current {@link OperatingSystem}
     */
    public static OperatingSystem getOperatingSystem() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) return OperatingSystem.WINDOWS;
        if (osName.contains("mac")) return OperatingSystem.MACOS;
        if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix"))
            return OperatingSystem.UNIX_LIKE;
        if (osName.contains("sunos")) return OperatingSystem.SOLARIS;
        if (osName.contains("freebsd")) return OperatingSystem.FREEBSD;
        if (osName.contains("hp-ux")) return OperatingSystem.HPUX;
        return OperatingSystem.OTHER;
    }

    /**
     * Returns the OS-specific path for application data.
     *
     * @return platform-specific application data path
     */
    public static Path getApplicationDataPath() {
        OperatingSystem os = getOperatingSystem();
        String userHome = System.getProperty("user.home");

        switch (os) {
            case WINDOWS:
                String localAppData = System.getenv("LOCALAPPDATA");
                if (localAppData != null) return Paths.get(localAppData);
                String appData = System.getenv("APPDATA");
                if (appData != null) return Paths.get(appData);
                return Paths.get(userHome, "AppData", "Local");

            case MACOS:
                return Paths.get(userHome, "Library", "Application Support");

            case UNIX_LIKE:
                String xdgDataHome = System.getenv("XDG_DATA_HOME");
                if (xdgDataHome != null) return Paths.get(xdgDataHome);
                return Paths.get(userHome, ".local", "share");

            default:
                return Paths.get(userHome);
        }
    }

    /**
     * Returns the platform-specific path for storing configuration files.
     *
     * @return path to user-specific configuration directory
     */
    public static Path getConfigPath() {
        String userHome = System.getProperty("user.home");

        switch (getOperatingSystem()) {
            case WINDOWS:
                String appData = System.getenv("APPDATA");
                if (appData != null) return Paths.get(appData);
                return Paths.get(userHome, "AppData", "Roaming");

            case MACOS:
                return Paths.get(userHome, "Library", "Preferences");

            case UNIX_LIKE:
                String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
                if (xdgConfigHome != null) return Paths.get(xdgConfigHome);
                return Paths.get(userHome, ".config");

            default:
                return getApplicationDataPath();
        }
    }

    /**
     * Returns the default temporary directory.
     *
     * @return path to the temp directory
     */
    public static Path getTempPath() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Returns the system architecture.
     *
     * @return architecture name (e.g., x86_64, arm64)
     */
    public static String getArch() {
        return System.getProperty("os.arch");
    }

    /**
     * Returns the default locale of the system.
     *
     * @return system {@link Locale}
     */
    public static Locale getLocale() {
        return Locale.getDefault();
    }

    /**
     * Returns the system's default time zone.
     *
     * @return {@link ZoneId} of the system
     */
    public static ZoneId getTimezone() {
        return TimeZone.getDefault().toZoneId();
    }

    /**
     * Returns the current JVM version.
     *
     * @return JVM version string
     */
    public static String getJvmVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Retrieves the value of the specified environment variable.
     *
     * @param name the name of the environment variable
     * @return the value or {@code null} if not found
     */
    public static String getEnv(String name) {
        return System.getenv(name);
    }

    /**
     * Returns the name of the current Java Virtual Machine.
     *
     * @return JVM name string (e.g., OpenJDK, Oracle)
     */
    public static String getJvmName() {
        return System.getProperty("java.vm.name");
    }

    /**
     * Returns the name of the underlying operating system.
     *
     * @return raw OS name from system properties
     */
    public static String getOsName() {
        return System.getProperty("os.name");
    }

    private PlatformUtils() {
    }
}
