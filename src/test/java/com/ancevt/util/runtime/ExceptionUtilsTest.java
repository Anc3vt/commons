package com.ancevt.util.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionUtilsTest {

    @Test
    void testStackTraceToStringNullReturnsEmpty() {
        assertEquals("", ExceptionUtils.stackTraceToString(null));
    }

    @Test
    void testStackTraceToStringContainsClassAndMessage() {
        Exception ex = new IllegalArgumentException("bad argument");
        String trace = ExceptionUtils.stackTraceToString(ex);

        assertTrue(trace.contains("IllegalArgumentException"));
        assertTrue(trace.contains("bad argument"));
        assertTrue(trace.contains("ExceptionUtilsTest")); // в стеке должно быть имя теста
    }

    @Test
    void testUnsquashedStackTraceToStringNullReturnsEmpty() {
        assertEquals("", ExceptionUtils.unsquashedStackTraceToString(null));
    }

    @Test
    void testUnsquashedStackTraceContainsCause() {
        Exception inner = new IllegalArgumentException("inner");
        Exception outer = new RuntimeException("outer", inner);

        String trace = ExceptionUtils.unsquashedStackTraceToString(outer);

        // должно содержать оба уровня
        assertTrue(trace.contains("RuntimeException: outer"));
        assertTrue(trace.contains("IllegalArgumentException: inner"));
        assertTrue(trace.contains("Caused by:"));
    }

    @Test
    void testUnsquashedDoesNotRepeatSameThrowable() {
        Exception root = new IllegalStateException("root");
        Exception loop = new RuntimeException("loop", root);
        root.initCause(loop); // зацикливаем cause, чтобы проверить защиту от рекурсии

        String trace = ExceptionUtils.unsquashedStackTraceToString(root);

        // trace не должен быть бесконечным
        assertTrue(trace.contains("IllegalStateException: root"));
        assertTrue(trace.contains("RuntimeException: loop"));
        // но повторяться не должен
        int firstIndex = trace.indexOf("IllegalStateException: root");
        int lastIndex = trace.lastIndexOf("IllegalStateException: root");
        assertEquals(firstIndex, lastIndex);
    }
}
