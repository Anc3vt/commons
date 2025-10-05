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

import java.util.Map;
import java.util.Objects;

/**
 * Utility class for extracting the name of the application's main class.
 * <p>
 * This class attempts to determine the fully qualified name of the class
 * that contains the {@code public static void main(String[] args)} method
 * by inspecting the stack trace of the main thread (thread with ID 1).
 * </p>
 *
 * <h2>Use Cases:</h2>
 * <ul>
 *     <li>Logging the entry point class during application bootstrap</li>
 *     <li>Diagnostics and debugging tools</li>
 *     <li>Generic CLI or utility applications</li>
 * </ul>
 *
 * <h2>Limitations:</h2>
 * <ul>
 *     <li>This method is not guaranteed to work in all environments.</li>
 *     <li>It relies on the JVM retaining stack traces of the main thread, which
 *     may not be the case in some containers (e.g., application servers), or in tests,
 *     or if the {@code main} method has already exited.</li>
 *     <li>May not work in frameworks that override or mask the main entry point
 *     (e.g., Spring Boot's {@code JarLauncher}).</li>
 * </ul>
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * try {
 *     String mainClassName = MainClassNameExtractorException.getMainClassName();
 *     System.out.println("Application started by: " + mainClassName);
 * } catch (MainClassNameExtractorException.MainClassNameExtractorException e) {
 *     e.printStackTrace();
 * }
 * }</pre>
 *
 */
public class MainClassNameExtractor {

    /**
     * Attempts to extract the fully qualified name of the class containing the {@code main} method.
     *
     * @return the class name containing the {@code main} method
     * @throws MainClassNameExtractorException if unable to reliably determine the main class
     */
    public static String getMainClassName() throws MainClassNameExtractorException {
        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> entry : map.entrySet()) {
            Thread thread = entry.getKey();
            if (thread.getId() == 1) {
                StackTraceElement[] stackTraceElements = entry.getValue();
                for (int i = stackTraceElements.length - 1; i >= 0; i--) {
                    StackTraceElement stackTraceElement = stackTraceElements[i];
                    if (Objects.equals(stackTraceElement.getMethodName(), "main")) {
                        return stackTraceElement.getClassName();
                    }
                }
            }
        }
        throw new MainClassNameExtractorException("Unable to extract main class name");
    }

    /**
     * Thrown when the main class name cannot be determined.
     */
    public static class MainClassNameExtractorException extends RuntimeException {

        /**
         * Constructs a new exception with the specified message.
         *
         * @param message the detail message
         */
        public MainClassNameExtractorException(String message) {
            super(message);
        }
    }

    private MainClassNameExtractor() {
    }
}
