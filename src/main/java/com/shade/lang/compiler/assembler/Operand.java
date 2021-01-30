package com.shade.lang.compiler.assembler;

import com.shade.lang.util.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class represents typed value of an {@link Instruction}.
 */
public class Operand {
    public static final int UNDEFINED = -1;

    private final OperandType type;
    private final Object value;

    private Operand(@NotNull OperandType type, @NotNull Object value) {
        this.type = type;
        this.value = value;
    }

    public static Operand imm8(int value) {
        if (value > 0xff) {
            throw new IllegalArgumentException("Argument is too big for operand of type Imm8");
        }
        return new Operand(OperandType.IMM_8, (byte) (value & 0xff));
    }

    public static Operand imm16(int value) {
        if (value > 0xffff) {
            throw new IllegalArgumentException("Argument is too big for operand of type Imm16");
        }
        return new Operand(OperandType.IMM_16, (short) (value & 0xffff));
    }

    public static Operand imm32(int value) {
        return new Operand(OperandType.IMM_32, value);
    }

    public static Operand imm64(long value) {
        return new Operand(OperandType.IMM_64, value);
    }

    public static Operand constant(Object value) {
        return new Operand(OperandType.CONSTANT, value);
    }

    public void emit(@NotNull Assembler assembler, @NotNull ByteBuffer buffer) {
        switch (type) {
            case IMM_8:
                buffer.put((byte) value);
                break;
            case IMM_16:
                buffer.putShort((short) value);
                break;
            case IMM_32:
                buffer.putInt((int) value);
                break;
            case IMM_64:
                buffer.putLong((long) value);
                break;
            case CONSTANT: {
                int index = assembler.getConstantIndex(value);
                if (index > 0xffff) {
                    throw new IllegalStateException("Constant index is too big (max 65535)");
                }
                buffer.putShort((short) index);
            }
        }
    }

    public byte getImm8() {
        if (type != OperandType.IMM_8) {
            throw new IllegalStateException("Cannot get value as Imm8 because type of operand is " + type);
        }
        return (byte) value;
    }

    public short getImm16() {
        if (type != OperandType.IMM_16) {
            throw new IllegalStateException("Cannot get value as Imm16 because type of operand is " + type);
        }
        return (short) value;
    }

    public int getImm32() {
        if (type != OperandType.IMM_32) {
            throw new IllegalStateException("Cannot get value as Imm32 because type of operand is " + type);
        }
        return (int) value;
    }

    public long getImm64() {
        if (type != OperandType.IMM_64) {
            throw new IllegalStateException("Cannot get value as Imm64 because type of operand is " + type);
        }
        return (long) value;
    }

    public Object getConstant() {
        if (type != OperandType.CONSTANT) {
            throw new IllegalStateException("Cannot get value as Const because type of operand is " + type);
        }
        return value;
    }

    public OperandType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    @SuppressWarnings("MalformedFormatString")
    public String toDisplayString() {
        switch (type) {
            case IMM_8:
            case IMM_16:
            case IMM_32:
            case IMM_64:
                return String.format("%d", value);
            default:
                if (value instanceof String) {
                    return String.format("'%s'", ((String) value).chars()
                        .mapToObj(x -> x <= 27 ? String.format("\\x%02x", x) : String.valueOf((char) x))
                        .collect(Collectors.joining()));
                }
                return String.valueOf(value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operand operand = (Operand) o;
        return type == operand.type && Objects.equals(value, operand.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
