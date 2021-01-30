package com.shade.lang.compiler.assembler;

import com.shade.lang.util.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class represents glued {@link Operation} with array of bound {@link Operand}.
 */
public class Instruction {
    private final Operation operation;
    private final Operand[] operands;
    private final int size;

    public Instruction(@NotNull Operation operation, @NotNull Operand[] operands) {
        this.operation = operation;
        this.operands = operands;
        this.size = Arrays.stream(operands).mapToInt(x -> x.getType().getSize()).sum() + 1;
    }

    /**
     * Assembles operation with operands and emits
     * them to supplied {@code buffer}. This method
     * must be called only by assembler.
     *
     * @param assembler parent assembler of this instruction
     * @param buffer    buffer this instruction must be written to
     */
    public void emit(@NotNull Assembler assembler, @NotNull ByteBuffer buffer) {
        buffer.put(operation.getOpcode());
        for (Operand operand : operands) {
            operand.emit(assembler, buffer);
        }
    }

    /**
     * Returns operation of this instruction.
     *
     * @return instruction operation
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Returns array of operands of this instruction.
     *
     * @return instruction operands
     */
    public Operand[] getOperands() {
        return operands;
    }

    /**
     * Returns calculated size this instruction,
     * including all its operands.
     *
     * @return instruction size
     */
    public int getSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instruction that = (Instruction) o;
        return operation == that.operation && Arrays.equals(operands, that.operands);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(operation);
        result = 31 * result + Arrays.hashCode(operands);
        return result;
    }
}
