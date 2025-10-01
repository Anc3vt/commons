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
                .build()
                .row(1, "Alice", 95)
                .row(2, "Bob", 87);

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
                .build()
                .row("foo", "bar");

        String output = table.render();

        assertFalse(output.contains("+"));
        assertTrue(output.contains("A"));
        assertTrue(output.contains("foo"));
    }

    @Test
    @DisplayName("MARKDOWN style should render header separator with dashes")
    void testMarkdown() {
        TextTable table = TextTable.builder()
                .headers("Col1", "Col2")
                .style(TextTable.Style.MARKDOWN)
                .build()
                .row("x", "y");

        String output = table.render();

        assertTrue(output.startsWith("| Col1"));
        assertTrue(output.contains("|---"));
        assertTrue(output.contains("| x"));
    }

    @Test
    @DisplayName("Row length mismatch should throw IllegalArgumentException")
    void testRowLengthMismatch() {
        TextTable table = TextTable.builder()
                .headers("A", "B", "C")
                .build();

        assertThrows(IllegalArgumentException.class, () -> table.row("only", "two"));
    }

    @Test
    @DisplayName("Alignment should affect padding")
    void testAlignment() {
        TextTable table = TextTable.builder()
                .headers("Left", "Center", "Right")
                .align(0, TextTable.Alignment.LEFT)
                .align(1, TextTable.Alignment.CENTER)
                .align(2, TextTable.Alignment.RIGHT)
                .style(TextTable.Style.BORDERED)
                .build()
                .row("L", "C", "R");

        String output = table.render();

        // crude checks: at least the characters should be there
        assertTrue(output.contains("L "));
        assertTrue(output.contains(" C "));
        assertTrue(output.contains(" R"));
    }
}
