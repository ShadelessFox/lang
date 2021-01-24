package com.shade.lang.runtime.objects.function;

public class Guard {
    private final int start;
    private final int end;
    private final int offset;
    private final int slot;

    public Guard(int start, int end, int offset, int slot) {
        this.start = start;
        this.end = end;
        this.offset = offset;
        this.slot = slot;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getOffset() {
        return offset;
    }

    public int getSlot() {
        return slot;
    }

    public boolean hasSlot() {
        return slot >= 0;
    }
}
