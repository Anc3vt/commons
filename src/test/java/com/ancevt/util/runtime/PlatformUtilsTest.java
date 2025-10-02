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

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class PlatformUtilsTest {

    @Test
    void testGetOperatingSystemReturnsValidEnum() {
        PlatformUtils.OperatingSystem os = PlatformUtils.getOperatingSystem();
        assertNotNull(os);
    }

    @Test
    void testGetApplicationDataPathNotNull() {
        Path path = PlatformUtils.getApplicationDataPath();
        assertNotNull(path);
        assertFalse(path.toString().isEmpty());
    }

    @Test
    void testGetConfigPathNotNull() {
        Path path = PlatformUtils.getConfigPath();
        assertNotNull(path);
        assertFalse(path.toString().isEmpty());
    }

    @Test
    void testGetTempPathExists() {
        Path path = PlatformUtils.getTempPath();
        assertNotNull(path);

        Path expected = Paths.get(System.getProperty("java.io.tmpdir"));

        assertEquals(expected.normalize().toAbsolutePath(), path.normalize().toAbsolutePath());

        assertTrue(Files.exists(path), "Temp directory should exist");
    }


    @Test
    void testGetArchNotEmpty() {
        String arch = PlatformUtils.getArch();
        assertNotNull(arch);
        assertFalse(arch.isEmpty());
    }

    @Test
    void testGetLocaleMatchesDefault() {
        Locale locale = PlatformUtils.getLocale();
        assertEquals(Locale.getDefault(), locale);
    }

    @Test
    void testGetTimezoneMatchesDefault() {
        ZoneId tz = PlatformUtils.getTimezone();
        assertEquals(TimeZone.getDefault().toZoneId(), tz);
    }

    @Test
    void testGetJvmVersionNotEmpty() {
        String version = PlatformUtils.getJvmVersion();
        assertNotNull(version);
        assertFalse(version.isEmpty());
        assertEquals(System.getProperty("java.version"), version);
    }

    @Test
    void testGetJvmNameNotEmpty() {
        String name = PlatformUtils.getJvmName();
        assertNotNull(name);
        assertFalse(name.isEmpty());
        assertEquals(System.getProperty("java.vm.name"), name);
    }

    @Test
    void testGetOsNameNotEmpty() {
        String osName = PlatformUtils.getOsName();
        assertNotNull(osName);
        assertFalse(osName.isEmpty());
        assertEquals(System.getProperty("os.name"), osName);
    }

    @Test
    void testGetEnvExistingVariable() {
        String path = PlatformUtils.getEnv("PATH");
        assertNotNull(path);
        assertFalse(path.isEmpty());
    }

    @Test
    void testGetEnvNonExistingVariable() {
        String val = PlatformUtils.getEnv("PLATFORMUTILS_TEST_UNKNOWN_ENV");
        assertNull(val);
    }
}
