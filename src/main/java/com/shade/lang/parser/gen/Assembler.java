package com.shade.lang.parser.gen;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.*;

import static com.shade.lang.parser.gen.Opcode.*;

public class Assembler {
    private final ByteBuffer buffer;
    private final Set<Label> labels;
    private final List<String> constants;
    private final Map<Integer, Integer> lines;

    public Assembler(int capacity) {
        this.buffer = ByteBuffer.allocate(capacity);
        this.labels = new HashSet<>();
        this.constants = new ArrayList<>();
        this.lines = new HashMap<>();
    }

    public void imm8(byte imm) {
        buffer.put(imm);
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

    public void line(int line) {
        lines.put(buffer.position(), line);
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
        ByteBuffer buffer = getBuffer();

        // @formatter:off
        while (buffer.hasRemaining()){
            final int offset = buffer.position();
            switch (buffer.get()) {
                case PUSH_CONST:    stream.printf("%04x: PUSH_CONST    %x%n", offset, buffer.getInt()); break;
                case PUSH_INT:      stream.printf("%04x: PUSH_INT      %#x%n", offset, buffer.getInt()); break;
                case GET_GLOBAL:    stream.printf("%04x: GET_GLOBAL    '%s'%n", offset, constants.get(buffer.getInt())); break;
                case SET_GLOBAL:    stream.printf("%04x: SET_GLOBAL    '%s'%n", offset, constants.get(buffer.getInt())); break;
                case GET_ATTRIBUTE: stream.printf("%04x: GET_ATTRIBUTE '%s'%n", offset, constants.get(buffer.getInt())); break;
                case SET_ATTRIBUTE: stream.printf("%04x: SET_ATTRIBUTE '%s'%n", offset, constants.get(buffer.getInt())); break;
                case ADD:           stream.printf("%04x: ADD%n", offset); break;
                case SUB:           stream.printf("%04x: SUB%n", offset); break;
                case MUL:           stream.printf("%04x: MUL%n", offset); break;
                case DIV:           stream.printf("%04x: DIV%n", offset); break;
                case JUMP:          stream.printf("%04x: JUMP          %d%n", offset, buffer.getInt()); break;
                case IF_EQ:         stream.printf("%04x: IF_EQ         %d%n", offset, buffer.getInt()); break;
                case IF_NE:         stream.printf("%04x: IF_NE         %d%n", offset, buffer.getInt()); break;
                case IF_LT:         stream.printf("%04x: IF_LT         %d%n", offset, buffer.getInt()); break;
                case IF_LE:         stream.printf("%04x: IF_LE         %d%n", offset, buffer.getInt()); break;
                case IF_GT:         stream.printf("%04x: IF_GT         %d%n", offset, buffer.getInt()); break;
                case IF_GE:         stream.printf("%04x: IF_GE         %d%n", offset, buffer.getInt()); break;
                case CALL:          stream.printf("%04x: CALL          %d%n", offset, buffer.get()); break;
                case RET:           stream.printf("%04x: RET%n", offset); break;
                case POP:           stream.printf("%04x: POP%n", offset); break;
            }
        }
        // @formatter:on
    }

    public ByteBuffer getBuffer() {
        return (ByteBuffer) ByteBuffer
            .allocate(buffer.position())
            .put(buffer.array(), 0, buffer.position())
            .position(0);
    }

    public List<String> getConstants() {
        return constants;
    }

    public Map<Integer, Integer> getLines() {
        return lines;
    }

    public static class Label {
        private final int position;

        private Label(int position) {
            this.position = position;
        }
    }
}
