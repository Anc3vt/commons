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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility for rendering textual tables in different styles (ASCII, Unicode, Markdown).
 */
public class TextTable {

    public enum Style {BORDERED, PLAIN}

    public enum Alignment {LEFT, CENTER, RIGHT}

    /**
     * Defines border characters for a given style
     */
    public static class Borders {

        public static final Borders ASCII =
                new Borders("+", "+", "+",
                        "+", "+", "+",
                        "+", "+", "+",
                        "-", "|");

        public static final Borders UNICODE =
                new Borders("┌", "┬", "┐",
                        "├", "┼", "┤",   // оставили кресты, как ты хотел
                        "└", "┴", "┘",
                        "─", "│");

        final String topLeft, topMid, topRight;
        final String midLeft, midMid, midRight;
        final String bottomLeft, bottomMid, bottomRight;
        final String horizontal, vertical;

        public Borders(String topLeft, String topMid, String topRight,
                       String midLeft, String midMid, String midRight,
                       String bottomLeft, String bottomMid, String bottomRight,
                       String horizontal, String vertical) {
            this.topLeft = topLeft;
            this.topMid = topMid;
            this.topRight = topRight;
            this.midLeft = midLeft;
            this.midMid = midMid;
            this.midRight = midRight;
            this.bottomLeft = bottomLeft;
            this.bottomMid = bottomMid;
            this.bottomRight = bottomRight;
            this.horizontal = horizontal;
            this.vertical = vertical;
        }


    }

    private final List<String> headers;
    private final List<Alignment> alignments;
    private final List<List<Object>> rows = new ArrayList<>();
    private final Style style;
    private final Borders borders;
    private int[] cachedWidths = null; // ← кэш


    private TextTable(List<String> headers, List<Alignment> alignments, Style style, Borders borders) {
        this.headers = headers;
        this.alignments = alignments;
        this.style = style;
        this.borders = borders;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<String> headers = new ArrayList<>();
        private final List<Alignment> alignments = new ArrayList<>();
        private final List<List<Object>> rows = new ArrayList<>();
        private Style style = Style.BORDERED;
        private Borders borders = Borders.ASCII;

        public Builder headers(String... headers) {
            this.headers.addAll(Arrays.asList(headers));
            for (int i = 0; i < headers.length; i++) {
                alignments.add(Alignment.LEFT);
            }
            return this;
        }

        public Builder align(int columnIndex, Alignment alignment) {
            if (columnIndex >= alignments.size()) {
                throw new IllegalArgumentException("Invalid column index: " + columnIndex);
            }
            alignments.set(columnIndex, alignment);
            return this;
        }

        public Builder style(Style style) {
            this.style = style;
            return this;
        }

        public Builder border(Borders borders) {
            this.borders = borders;
            return this;
        }

        public Builder row(Object... data) {
            if (data.length != headers.size()) {
                throw new IllegalArgumentException("Row length != headers count");
            }
            rows.add(Arrays.asList(data));
            return this;
        }

        public TextTable build() {
            TextTable t = new TextTable(headers, alignments, style, borders);
            t.rows.addAll(rows);
            return t;
        }
    }

    public TextTable row(Object... data) {
        if (data.length != headers.size()) {
            throw new IllegalArgumentException("Row length != headers count");
        }
        rows.add(Arrays.asList(data));
        cachedWidths = null; // invalidate cache
        return this;
    }

    public String render() {
        int[] widths = getWidths();
        switch (style) {
            case BORDERED:
                return renderBordered(widths);
            case PLAIN:
                return renderPlain(widths);
            default:
                throw new IllegalStateException("Unknown style");
        }
    }

    private int[] getWidths() {
        if (cachedWidths == null) {
            cachedWidths = calcWidths();
        }
        return cachedWidths;
    }

    private String renderBordered(int[] widths) {
        StringBuilder sb = new StringBuilder();
        line(sb, borders.topLeft, borders.topMid, borders.topRight, widths);
        rowLine(sb, headers, widths);
        line(sb, borders.midLeft, borders.midMid, borders.midRight, widths);
        for (List<Object> row : rows) {
            rowLine(sb, row, widths);
        }
        line(sb, borders.bottomLeft, borders.bottomMid, borders.bottomRight, widths);
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

    private void line(StringBuilder sb, String left, String mid, String right, int[] widths) {
        sb.append(left);
        for (int i = 0; i < widths.length; i++) {
            sb.append(repeat(borders.horizontal, widths[i] + 2));
            sb.append(i == widths.length - 1 ? right : mid);
        }
        sb.append("\n");
    }

    private void rowLine(StringBuilder sb, List<?> row, int[] widths) {
        sb.append(borders.vertical);
        for (int i = 0; i < row.size(); i++) {
            String text = String.valueOf(row.get(i));
            sb.append(" ").append(pad(text, widths[i], alignments.get(i))).append(" ").append(borders.vertical);
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
                return repeat(" ", left) + text + repeat(" ", padding - left);
            default:
                return text;
        }
    }

    private int[] calcWidths() {
        int[] widths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            int max = headers.get(i).length();
            for (List<Object> row : rows) {
                max = Math.max(max, String.valueOf(row.get(i)).length());
            }
            widths[i] = max;
        }
        return widths;
    }

    private static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }

    public static void main(String[] args) {
        TextTable table = TextTable.builder()
                .headers("ID", "Name", "Score")
                .align(0, Alignment.RIGHT)
                .align(2, Alignment.CENTER)
                .style(Style.BORDERED)
                .border(Borders.UNICODE)
                .row(1, "Alice", 95)
                .row(2, "Bob", 87)
                .row(3, "Charlie", 100)
                .build();

        System.out.println(table.render());
    }
}
