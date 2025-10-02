package com.ancevt.util.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvUtilsTest {

    enum Mode {DEBUG, RELEASE}

    @Test
    void testGetReturnsNullIfMissing() {
        // переменной точно нет
        assertNull(EnvUtils.get("ENVUTILS_TEST_UNKNOWN"));
    }

    @Test
    void testGetWithDefault() {
        assertEquals("fallback", EnvUtils.get("ENVUTILS_TEST_UNKNOWN", "fallback"));
    }

    @Test
    void testGetIntValid() {
        assertEquals(123, EnvUtils.getInt("ENVUTILS_INT_VALID", 0));
    }

    @Test
    void testGetIntInvalidFallsBack() {
        assertEquals(42, EnvUtils.getInt("ENVUTILS_INT_INVALID", 42));
    }

    @Test
    void testGetIntMissingFallsBack() {
        assertEquals(-1, EnvUtils.getInt("ENVUTILS_INT_MISSING", -1));
    }

    @Test
    void testGetBooleanTruthyValues() {
        assertTrue(EnvUtils.getBoolean("ENVUTILS_BOOL_TRUE", false));
        assertTrue(EnvUtils.getBoolean("ENVUTILS_BOOL_ONE", false));
        assertTrue(EnvUtils.getBoolean("ENVUTILS_BOOL_YES", false));
        assertTrue(EnvUtils.getBoolean("ENVUTILS_BOOL_ON", false));
    }

    @Test
    void testGetBooleanFalseValues() {
        assertFalse(EnvUtils.getBoolean("ENVUTILS_BOOL_FALSE", true));
        assertFalse(EnvUtils.getBoolean("ENVUTILS_BOOL_ZERO", true));
    }

    @Test
    void testGetBooleanMissingUsesDefault() {
        assertTrue(EnvUtils.getBoolean("ENVUTILS_BOOL_MISSING", true));
        assertFalse(EnvUtils.getBoolean("ENVUTILS_BOOL_MISSING", false));
    }

    @Test
    void testGetEnumValid() {
        assertEquals(Mode.DEBUG, EnvUtils.getEnum("ENVUTILS_ENUM_DEBUG", Mode.class, Mode.RELEASE));
    }

    @Test
    void testGetEnumInvalidFallsBack() {
        assertEquals(Mode.RELEASE, EnvUtils.getEnum("ENVUTILS_ENUM_INVALID", Mode.class, Mode.RELEASE));
    }

    @Test
    void testGetEnumMissingFallsBack() {
        assertEquals(Mode.RELEASE, EnvUtils.getEnum("ENVUTILS_ENUM_MISSING", Mode.class, Mode.RELEASE));
    }

    @Test
    void testGetLongValid() {
        assertEquals(9876543210L, EnvUtils.getLong("ENVUTILS_LONG_VALID", 0L));
    }

    @Test
    void testGetLongInvalidFallsBack() {
        assertEquals(123L, EnvUtils.getLong("ENVUTILS_LONG_INVALID", 123L));
    }

    @Test
    void testGetDoubleValid() {
        assertEquals(3.1415, EnvUtils.getDouble("ENVUTILS_DOUBLE_VALID", 0.0), 1e-6);
    }

    @Test
    void testGetDoubleInvalidFallsBack() {
        assertEquals(2.71, EnvUtils.getDouble("ENVUTILS_DOUBLE_INVALID", 2.71), 1e-6);
    }
}
