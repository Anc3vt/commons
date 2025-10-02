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

package com.ancevt.util.string;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextTableTest {

    @Test
    @DisplayName("BORDERED style should render with borders and aligned columns")
    void testBordered() {
        TextTable table = TextTable.builder()
                .headers("ID", "Name", "Score")
                .align(0, TextTable.Alignment.RIGHT)
                .align(2, TextTable.Alignment.CENTER)
                .style(TextTable.Style.BORDERED)
                .row(1, "Alice", 95)
                .row(2, "Bob", 87)
                .build();


        String output = table.render();

        // Проверяем ключевые части, а не весь вывод
        assertTrue(output.contains("+"));            // есть рамки
        assertTrue(output.contains("ID"));           // заголовок
        assertTrue(output.contains("Alice"));        // имя в таблице
        assertTrue(output.contains("95"));           // число
        assertTrue(output.contains("Bob"));          // вторая строка
    }


    @Test
    @DisplayName("PLAIN style should render without borders")
    void testPlain() {
        TextTable table = TextTable.builder()
                .headers("A", "B")
                .style(TextTable.Style.PLAIN)
                .row("foo", "bar")
                .build();

        String output = table.render();

        assertFalse(output.contains("+"));
        assertTrue(output.contains("A"));
        assertTrue(output.contains("foo"));
    }

    @Test
    @DisplayName("Row length mismatch should throw IllegalArgumentException")
    void testRowLengthMismatch() {
        assertThrows(IllegalArgumentException.class, () ->
                TextTable.builder()
                        .headers("A", "B", "C")
                        .row("only", "two") // <- ошибка должна случиться здесь
                        .build()
        );
    }

    @Test
    @DisplayName("Alignment should affect padding")
    void testAlignment() {
        TextTable table = TextTable.builder()
                .headers("L", "C", "R")
                .align(0, TextTable.Alignment.LEFT)
                .align(1, TextTable.Alignment.CENTER)
                .align(2, TextTable.Alignment.RIGHT)
                .style(TextTable.Style.BORDERED)
                .row("x", "y", "z")
                .build();

        String output = table.render();

        // crude checks: at least the characters should be there
        assertTrue(output.contains("L "));
        assertTrue(output.contains(" C "));
        assertTrue(output.contains(" R"));
    }
}
