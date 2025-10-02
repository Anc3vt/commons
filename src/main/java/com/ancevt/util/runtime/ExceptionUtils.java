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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for working with exceptions.
 */
public class ExceptionUtils {

    /**
     * Converts a {@link Throwable}'s full stack trace to a {@link String}.
     *
     * @param throwable the throwable to convert
     * @return the full stack trace as a string
     */
    public static String stackTraceToString(Throwable throwable) {
        if (throwable == null) return "";
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Converts a {@link Throwable}'s full stack trace to a string
     * including all causes without collapsing shared stack frames
     * (i.e. avoids "... N more").
     *
     * @param throwable the throwable to convert
     * @return the full, expanded stack trace
     */
    public static String unsquashedStackTraceToString(Throwable throwable) {
        if (throwable == null) return "";

        StringBuilder sb = new StringBuilder();
        Set<Throwable> seen = new HashSet<>();

        while (throwable != null && !seen.contains(throwable)) {
            seen.add(throwable);

            sb.append(throwable.toString()).append("\n");
            for (StackTraceElement element : throwable.getStackTrace()) {
                sb.append("\tat ").append(element).append("\n");
            }

            throwable = throwable.getCause();
            if (throwable != null && !seen.contains(throwable)) {
                sb.append("Caused by: ");
            }
        }

        return sb.toString();
    }

    private ExceptionUtils() {
    }

    public static void main(String[] args) {
        testSquashed();
        System.out.println("----------");
        testUnsquashed();
    }

    private static void testSquashed() {
        try {
            causeError();
        } catch (Exception e) {
            System.out.println("Squashed:");
            System.out.println(stackTraceToString(e));
        }
    }

    private static void testUnsquashed() {
        try {
            causeError();
        } catch (Exception e) {
            System.out.println("Unsquashed:");
            System.out.println(unsquashedStackTraceToString(e));
        }
    }

    private static void causeError() {
        try {
            throw new IllegalArgumentException("Inner");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Outer", e);
        }
    }
}
