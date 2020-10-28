package com.shade.lang.parser.token;

import java.util.Objects;

public class Region {
    private final Span begin;
    private final Span end;

    public Region(Span begin, Span end) {
        this.begin = begin;
        this.end = end;
    }

    public Region(Span begin) {
        this.begin = begin;
        this.end = null;
    }

    public Region from(Region begin) {
        return new Region(begin.begin, end);
    }

    public Region until(Region end) {
        return new Region(begin, end.end);
    }

    public String of(String contents) {
        Objects.requireNonNull(end);
        return contents.substring(begin.offset, end.offset);
    }

    public Span getBegin() {
        return begin;
    }

    public Span getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return begin + "-" + end;
    }

    public static class Span {
        private final int line;
        private final int column;
        private final int offset;

        public Span(int line, int column, int offset) {
            this.line = line;
            this.column = column;
            this.offset = offset;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        public int getOffset() {
            return offset;
        }

        public Region from(Span start) {
            return new Region(start, this);
        }

        public Region until(Span end) {
            return new Region(this, end);
        }

        public Span offsetBy(int count) {
            return new Span(line, column + count, offset + count);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Span span = (Span) o;
            return line == span.line &&
                    column == span.column &&
                    offset == span.offset;
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, column, offset);
        }

        @Override
        public String toString() {
            return line + ":" + column;
        }
    }
}
