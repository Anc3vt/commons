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
