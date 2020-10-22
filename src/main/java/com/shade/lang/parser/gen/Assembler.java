package com.shade.lang.parser.gen;

import com.shade.lang.parser.token.Region;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Supplier;

import static com.shade.lang.parser.gen.Opcode.*;

public class Assembler {
    private final ByteBuffer buffer;
    private final Set<Label> labels;
    private final List<String> constants;
    private final Map<Integer, Region.Span> lines;

    public Assembler(int capacity) {
        this.buffer = ByteBuffer.allocate(capacity);
        this.labels = new HashSet<>();
        this.constants = new ArrayList<>();
        this.lines = new HashMap<>();
    }

    public void imm8(byte imm) {
        buffer.put(imm);
    }

    public void imm8(boolean imm) {
        buffer.put(imm ? (byte) 1 : (byte) 0);
    }

    public void imm16(short imm) {
        buffer.putShort(imm);
    }

    public void imm32(int imm) {
        buffer.putInt(imm);
    }

    public void imm64(long imm) {
        buffer.putLong(imm);
    }

    public void span(Region.Span span) {
        lines.put(buffer.position(), span);
    }

    public int constant(String value) {
        if (!constants.contains(value)) {
            constants.add(value);
        }
        return constants.indexOf(value);
    }

    public Label jump(byte opcode) {
        imm8(opcode);

        Label label = new Label(buffer.position());
        labels.add(label);
        buffer.putInt(0xcafebabe);
        return label;
    }

    public void bind(Label label) {
        if (label == null) {
            return;
        }

        if (!labels.remove(label)) {
            throw new IllegalArgumentException("Invalid label");
        }

        buffer.putInt(label.position, buffer.position() - label.position);
    }

    public void dump(PrintStream stream) {
        final ByteBuffer buffer = getBuffer();
        final Map<Integer, Integer> labels = new HashMap<>();

        final Supplier<String> formatConstant = () -> {
            int index = buffer.getInt();
            return String.format("%d '%s'", index, constants.get(index));
        };

        final Supplier<String> formatLabel = () -> {
            int offset = buffer.position() + buffer.getInt();
            if (!labels.containsKey(offset)) {
                labels.put(offset, labels.size());
            }
            return String.format(".L%d (%04x, +%d)", labels.get(offset), offset, offset - buffer.position() + 4);
        };

        while (buffer.remaining() >= 0) {
            final int offset = buffer.position();

            Integer label = labels.get(offset);
            if (label != null) {
                System.out.printf(".L%d%n", label);
            }

            if (!buffer.hasRemaining())
                break;

            switch (buffer.get()) {
                // @formatter:off
                case PUSH_CONST:    stream.printf("%04x: PUSH_CONST    %s%n", offset, formatConstant.get()); break;
                case PUSH_INT:      stream.printf("%04x: PUSH_INT      %#x%n", offset, buffer.getInt()); break;
                case GET_GLOBAL:    stream.printf("%04x: GET_GLOBAL    %s%n", offset, formatConstant.get()); break;
                case SET_GLOBAL:    stream.printf("%04x: SET_GLOBAL    %s%n", offset, formatConstant.get()); break;
                case GET_LOCAL:     stream.printf("%04x: GET_LOCAL     %d%n", offset, buffer.get()); break;
                case SET_LOCAL:     stream.printf("%04x: SET_LOCAL     %d%n", offset, buffer.get()); break;
                case GET_ATTRIBUTE: stream.printf("%04x: GET_ATTRIBUTE %s%n", offset, formatConstant.get()); break;
                case SET_ATTRIBUTE: stream.printf("%04x: SET_ATTRIBUTE %s%n", offset, formatConstant.get()); break;
                case ADD:           stream.printf("%04x: ADD%n", offset); break;
                case SUB:           stream.printf("%04x: SUB%n", offset); break;
                case MUL:           stream.printf("%04x: MUL%n", offset); break;
                case DIV:           stream.printf("%04x: DIV%n", offset); break;
                case JUMP:          stream.printf("%04x: JUMP          %s%n", offset, formatLabel.get()); break;
                case IF_EQ:         stream.printf("%04x: IF_EQ         %s%n", offset, formatLabel.get()); break;
                case IF_NE:         stream.printf("%04x: IF_NE         %s%n", offset, formatLabel.get()); break;
                case IF_LT:         stream.printf("%04x: IF_LT         %s%n", offset, formatLabel.get()); break;
                case IF_LE:         stream.printf("%04x: IF_LE         %s%n", offset, formatLabel.get()); break;
                case IF_GT:         stream.printf("%04x: IF_GT         %s%n", offset, formatLabel.get()); break;
                case IF_GE:         stream.printf("%04x: IF_GE         %s%n", offset, formatLabel.get()); break;
                case CALL:          stream.printf("%04x: CALL          %d%n", offset, buffer.get()); break;
                case RET:           stream.printf("%04x: RET%n", offset); break;
                case POP:           stream.printf("%04x: POP%n", offset); break;
                case NOT:           stream.printf("%04x: NOT%n", offset); break;
                case IMPORT:        stream.printf("%04x: IMPORT        %s %s%n", offset, formatConstant.get(), buffer.get() > 0 ? "<path>" : ""); break;
                default: throw new RuntimeException(String.format("Unknown opcode: %x", buffer.get(buffer.position() - 1)));
                // @formatter:on
            }
        }
    }

    public ByteBuffer getBuffer() {
        return (ByteBuffer) ByteBuffer
            .allocate(buffer.position())
            .put(buffer.array(), 0, buffer.position())
            .position(0);
    }

    public String[] getConstants() {
        return constants.toArray(new String[0]);
    }

    public Map<Integer, Region.Span> getLines() {
        return lines;
    }

    public static class Label {
        private final int position;

        private Label(int position) {
            this.position = position;
        }
    }
}
