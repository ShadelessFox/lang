package com.shade.lang.compiler.assembler;

import com.shade.lang.util.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Disassembler {
    private static final Map<Byte, Operation> OPERATION_LOOKUP_TABLE = Arrays.stream(Operation.values())
        .collect(Collectors.toMap(
            Operation::getOpcode,
            operation -> operation
        ));

    private final ByteBuffer buffer;
    private final Function<Short, Object> constantSupplier;

    public Disassembler(@NotNull ByteBuffer buffer, @NotNull Function<Short, Object> constantSupplier) {
        this.buffer = buffer;
        this.constantSupplier = constantSupplier;
    }

    @NotNull
    public Optional<Instruction> next() throws DisassemblerException {
        if (!buffer.hasRemaining()) {
            return Optional.empty();
        }

        final int position = buffer.position();
        final byte opcode = buffer.get();
        final Operation operation = OPERATION_LOOKUP_TABLE.get(opcode);

        if (operation == null) {
            throw new DisassemblerException(String.format("Invalid opcode %#02x at position %2$d (%2$#04x)", opcode, position));
        }

        final OperandType[] operandTypes = operation.getOperands();
        final Operand[] operands = new Operand[operandTypes.length];

        for (int index = 0; index < operandTypes.length; index++) {
            final OperandType type = operandTypes[index];
            switch (type) {
                case IMM_8:
                    operands[index] = Operand.imm8(buffer.get());
                    break;
                case IMM_16:
                    operands[index] = Operand.imm16(buffer.getShort());
                    break;
                case IMM_32:
                    operands[index] = Operand.imm32(buffer.getInt());
                    break;
                case IMM_64:
                    operands[index] = Operand.imm64(buffer.getLong());
                    break;
                case CONSTANT: {
                    final short constantIndex = buffer.getShort();
                    final Object constant = constantSupplier.apply(constantIndex);
                    if (constant == null) {
                        throw new DisassemblerException(String.format("Invalid constant index %1$d (%1$#04x)", constantIndex));
                    }
                    operands[index] = Operand.constant(constant);
                }
            }
        }

        return Optional.of(new Instruction(operation, operands));
    }

    public static class DisassemblerException extends Exception {
        public DisassemblerException(@NotNull String message) {
            super(message);
        }
    }
}
