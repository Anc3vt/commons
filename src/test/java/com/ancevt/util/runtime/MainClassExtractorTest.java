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
