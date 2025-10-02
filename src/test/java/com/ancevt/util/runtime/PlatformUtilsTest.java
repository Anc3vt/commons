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
