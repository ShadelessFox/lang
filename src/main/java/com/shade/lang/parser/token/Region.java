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

    public Region from(Span begin) {
        return new Region(begin, end);
    }

    public Region from(Region begin) {
        return new Region(begin.begin, end);
    }

    public Region until(Span end) {
        return new Region(begin, end);
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

        @Override
        public String toString() {
            return line + ":" + column;
        }
    }
}
