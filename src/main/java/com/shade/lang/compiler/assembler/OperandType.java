package com.shade.lang.compiler.assembler;

public enum OperandType {
    IMM_8(1),
    IMM_16(2),
    IMM_32(4),
    IMM_64(8),
    CONSTANT(2);

    private final int size;

    OperandType(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
