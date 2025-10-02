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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;

class MainClassNameExtractorTest {

    @Test
    void testGetMainClassNameInSeparateProcess() throws Exception {
        // Запускаем вспомогательный класс в отдельном процессе
        Process process = new ProcessBuilder(
                "java",
                "-cp",
                System.getProperty("java.class.path"),
                "com.ancevt.util.runtime.MainClassNameExtractorTest$HelperMain"
        ).redirectErrorStream(true).start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String output = reader.readLine();
            int exitCode = process.waitFor();

            assertEquals(0, exitCode);
            assertTrue(output.contains("HelperMain"), "Expected output to contain HelperMain, got: " + output);
        }
    }

    /**
     * Вспомогательный класс с методом main для теста
     */
    public static class HelperMain {
        public static void main(String[] args) {
            try {
                String mainClass = MainClassNameExtractor.getMainClassName();
                System.out.println(mainClass);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
