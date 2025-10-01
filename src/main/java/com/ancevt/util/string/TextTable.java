package com.ancevt.util.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility for rendering textual tables in different styles (bordered, plain, markdown).
 * <p>
 * Example:
 * <pre>{@code
 * TextTable table = TextTable.builder()
 *     .headers("ID", "Name", "Score")
 *     .align(0, TextTable.Alignment.RIGHT)
 *     .align(2, TextTable.Alignment.CENTER)
 *     .style(TextTable.Style.BORDERED)
 *     .build()
 *     .row(1, "Alice", 95)
 *     .row(2, "Bob", 87)
 *     .row(3, "Charlie", 100);
 *
 * System.out.println(table.render());
 * }</pre>
 */
public class TextTable {
    /**
     * Table rendering style.
     */
    public enum Style {BORDERED, PLAIN, MARKDOWN}

    /**
     * Column alignment options.
     */
    public enum Alignment {LEFT, CENTER, RIGHT}

    private final List<String> headers;
    private final List<Alignment> alignments;
    private final List<List<Object>> rows = new ArrayList<>();
    private final Style style;

    private TextTable(List<String> headers, List<Alignment> alignments, Style style) {
        this.headers = headers;
        this.alignments = alignments;
        this.style = style;
    }

    /**
     * Creates a new builder for constructing a {@link TextTable}.
     *
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    /**
     * Builder class for {@link TextTable}.
     */
    public static class Builder {
        private final List<String> headers = new ArrayList<>();
        private final List<Alignment> alignments = new ArrayList<>();
        private Style style = Style.BORDERED;
        /**
         * Sets the table headers.
         * All columns will be initialized with {@link Alignment#LEFT}.
         *
         * @param headers column header names
         * @return this builder
         */
        public Builder headers(String... headers) {
            this.headers.addAll(Arrays.asList(headers));
            for (int i = 0; i < headers.length; i++) {
                alignments.add(Alignment.LEFT);
            }
            return this;
        }
        /**
         * Sets alignment for a specific column.
         *
         * @param columnIndex column index (0-based)
         * @param alignment   desired alignment
         * @return this builder
         * @throws IllegalArgumentException if the index is invalid
         */
        public Builder align(int columnIndex, Alignment alignment) {
            if (columnIndex >= alignments.size()) {
                throw new IllegalArgumentException("Invalid column index: " + columnIndex);
            }
            alignments.set(columnIndex, alignment);
            return this;
        }
        /**
         * Sets the rendering style of the table.
         *
         * @param style rendering style
         * @return this builder
         */
        public Builder style(Style style) {
            this.style = style;
            return this;
        }
        /**
         * Builds the {@link TextTable} instance.
         *
         * @return configured table
         */
        public TextTable build() {
            return new TextTable(headers, alignments, style);
        }
    }

    // ---------- Public API ----------

    /**
     * Adds a new row to the table.
     *
     * @param data row values (must match header count)
     * @return this table for chaining
     * @throws IllegalArgumentException if data length does not match headers count
     */
    public TextTable row(Object... data) {
        if (data.length != headers.size()) {
            throw new IllegalArgumentException("Row length must match headers count");
        }
        rows.add(Arrays.asList(data));
        return this;
    }
    /**
     * Renders the table as a string using the selected style.
     *
     * @return rendered table string
     */
    public String render() {
        int[] colWidths = calcWidths();
        switch (style) {
            case BORDERED:
                return renderBordered(colWidths);
            case PLAIN:
                return renderPlain(colWidths);
            case MARKDOWN:
                return renderMarkdown(colWidths);
            default:
                throw new IllegalStateException("Unknown style: " + style);
        }
    }

    private String renderBordered(int[] widths) {
        StringBuilder sb = new StringBuilder();
        horizontalLine(sb, widths);
        rowLine(sb, headers, widths);
        horizontalLine(sb, widths);
        for (List<Object> row : rows) {
            rowLine(sb, row, widths);
        }
        horizontalLine(sb, widths);
        return sb.toString();
    }

    private String renderPlain(int[] widths) {
        StringBuilder sb = new StringBuilder();
        rowLine(sb, headers, widths);
        for (List<Object> row : rows) {
            rowLine(sb, row, widths);
        }
        return sb.toString();
    }

    private String renderMarkdown(int[] widths) {
        StringBuilder sb = new StringBuilder();
        rowLine(sb, headers, widths);
        sb.append("|");
        for (int i = 0; i < widths.length; i++) {
            sb.append(repeat("-", widths[i] + 2)).append("|");
        }
        sb.append("\n");
        for (List<Object> row : rows) {
            rowLine(sb, row, widths);
        }
        return sb.toString();
    }

    private void horizontalLine(StringBuilder sb, int[] widths) {
        sb.append("+");
        for (int w : widths) {
            sb.append(repeat("-", w + 2)).append("+");
        }
        sb.append("\n");
    }

    private void rowLine(StringBuilder sb, List<?> row, int[] widths) {
        sb.append("|");
        for (int i = 0; i < row.size(); i++) {
            String text = String.valueOf(row.get(i));
            sb.append(" ").append(pad(text, widths[i], alignments.get(i))).append(" |");
        }
        sb.append("\n");
    }

    private String pad(String text, int width, Alignment alignment) {
        int padding = width - text.length();
        switch (alignment) {
            case LEFT:
                return text + repeat(" ", padding);
            case RIGHT:
                return repeat(" ", padding) + text;
            case CENTER:
                int left = padding / 2;
                int right = padding - left;
                return repeat(" ", left) + text + repeat(" ", right);
            default:
                return text;
        }
    }

    private int[] calcWidths() {
        int cols = headers.size();
        int[] widths = new int[cols];
        for (int i = 0; i < cols; i++) {
            int max = headers.get(i).length();
            for (List<Object> row : rows) {
                max = Math.max(max, String.valueOf(row.get(i)).length());
            }
            widths[i] = max;
        }
        return widths;
    }

    private static String repeat(String s, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        TextTable table = TextTable.builder()
                .headers("ID", "Name", "Score")
                .align(0, Alignment.RIGHT)
                .align(2, Alignment.CENTER)
                .style(Style.BORDERED)
                .build()
                .row(1, "Alice", 95)
                .row(2, "Bob", 87)
                .row(3, "Charlie", 100);

        System.out.println(table.render());
    }
}
