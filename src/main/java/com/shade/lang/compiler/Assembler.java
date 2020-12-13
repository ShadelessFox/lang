package com.shade.lang.compiler;

import com.shade.lang.parser.token.Region;

import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.shade.lang.compiler.Opcode.*;

public class Assembler {
    private final ByteBuffer buffer;
    private final List<Label> labels;
    private final List<Object> constants;
    private final Map<Integer, Region.Span> traceLines;
    private final Map<Region.Span, Integer> debugLines;

    public Assembler(int capacity) {
        this.buffer = ByteBuffer.allocate(capacity);
        this.labels = new ArrayList<>();
        this.constants = new ArrayList<>();
        this.traceLines = new LinkedHashMap<>();
        this.debugLines = new LinkedHashMap<>();
    }

    public void imm8(int imm) {
        buffer.put((byte) (imm & 0xff));
    }

    public void imm16(int imm) {
        buffer.putShort((short) (imm & 0xffff));
    }

    public void imm32(int imm) {
        buffer.putInt(imm);
    }

    public void imm64(long imm) {
        buffer.putLong(imm);
    }

    public Label jump(byte opcode) {
        imm8(opcode);
        Label label = new Label(buffer.position());
        labels.add(label);
        buffer.putInt(0xcafebabe);
        return label;
    }

    public void bind(Label label) {
        bind(label, buffer.position());
    }

    public void bind(Label label, int offset) {
        if (label == null) {
            return;
        }

        if (!labels.remove(label)) {
            throw new IllegalArgumentException("Invalid label");
        }

        buffer.putInt(label.position, offset - label.position);
    }

    public ByteBuffer build() {
        return (ByteBuffer) ByteBuffer
            .allocate(buffer.position())
            .put(buffer.array(), 0, buffer.position())
            .position(0);
    }

    public void dump(PrintWriter stream) {
        final ByteBuffer buffer = build();
        final Map<Integer, Integer> labels = new HashMap<>();

        final Supplier<String> formatConstant = () -> {
            Object constant = constants.get(buffer.getInt());

            if (constant == null) {
                return null;
            }
            if (constant instanceof String) {
                return String.format("'%s'", ((String) constant).chars()
                    .mapToObj(x -> x <= 27 ? String.format("\\x%02x", x) : String.valueOf((char) x))
                    .collect(Collectors.joining()));
            } else if (constant == Void.TYPE) {
                return "none";
            } else {
                return constant.toString();
            }
        };

        final Supplier<String> formatLabel = () -> {
            int offset = buffer.position() + buffer.getInt();
            if (!labels.containsKey(offset)) {
                labels.put(offset, labels.size());
            }
            return String.format("%04x    (#L%d)", offset, labels.get(offset));
        };

        final Supplier<String> line = () -> {
            int offset = buffer.position() - 1;
            Integer label = labels.get(offset);
            if (label != null) {
                return String.format("#L%-2d %04x", label, offset);
            } else {
                return String.format("     %04x", offset);
            }
        };

        while (buffer.remaining() >= 0) {
            if (!buffer.hasRemaining())
                break;

            switch (buffer.get()) {
                // @formatter:off
                case PUSH_CONST:    stream.printf("%s: PUSH_CONST    %s%n", line.get(), formatConstant.get()); break;
                case GET_GLOBAL:    stream.printf("%s: GET_GLOBAL    %s%n", line.get(), formatConstant.get()); break;
                case SET_GLOBAL:    stream.printf("%s: SET_GLOBAL    %s%n", line.get(), formatConstant.get()); break;
                case GET_LOCAL:     stream.printf("%s: GET_LOCAL     %d%n", line.get(), buffer.get()); break;
                case SET_LOCAL:     stream.printf("%s: SET_LOCAL     %d%n", line.get(), buffer.get()); break;
                case GET_ATTRIBUTE: stream.printf("%s: GET_ATTRIBUTE %s%n", line.get(), formatConstant.get()); break;
                case SET_ATTRIBUTE: stream.printf("%s: SET_ATTRIBUTE %s%n", line.get(), formatConstant.get()); break;
                case GET_INDEX:     stream.printf("%s: GET_INDEX%n", line.get()); break;
                case SET_INDEX:     stream.printf("%s: SET_INDEX%n", line.get()); break;
                case ADD:           stream.printf("%s: ADD%n", line.get()); break;
                case SUB:           stream.printf("%s: SUB%n", line.get()); break;
                case MUL:           stream.printf("%s: MUL%n", line.get()); break;
                case DIV:           stream.printf("%s: DIV%n", line.get()); break;
                case NOT:           stream.printf("%s: NOT%n", line.get()); break;
                case AND:           stream.printf("%s: AND%n", line.get()); break;
                case OR:            stream.printf("%s: OR%n", line.get()); break;
                case XOR:           stream.printf("%s: XOR%n", line.get()); break;
                case SHL:           stream.printf("%s: SHL%n", line.get()); break;
                case SHR:           stream.printf("%s: SHR%n", line.get()); break;
                case JUMP:          stream.printf("%s: JUMP          %s%n", line.get(), formatLabel.get()); break;
                case JUMP_IF_TRUE:  stream.printf("%s: JUMP_IF_TRUE  %s%n", line.get(), formatLabel.get()); break;
                case JUMP_IF_FALSE: stream.printf("%s: JUMP_IF_FALSE %s%n", line.get(), formatLabel.get()); break;
                case CMP_EQ:        stream.printf("%s: CMP_EQ%n", line.get()); break;
                case CMP_NE:        stream.printf("%s: CMP_NE%n", line.get()); break;
                case CMP_LT:        stream.printf("%s: CMP_LT%n", line.get()); break;
                case CMP_LE:        stream.printf("%s: CMP_LE%n", line.get()); break;
                case CMP_GT:        stream.printf("%s: CMP_GT%n", line.get()); break;
                case CMP_GE:        stream.printf("%s: CMP_GE%n", line.get()); break;
                case ASSERT:        stream.printf("%s: ASSERT        %s %s%n", line.get(), formatConstant.get(), formatConstant.get()); break;
                case IMPORT:        stream.printf("%s: IMPORT        %s %d%n", line.get(), formatConstant.get(), buffer.get()); break;
                case NEW:           stream.printf("%s: NEW%n", line.get()); break;
                case CALL:          stream.printf("%s: CALL          %d%n", line.get(), buffer.get()); break;
                case RET:           stream.printf("%s: RET%n", line.get()); break;
                case POP:           stream.printf("%s: POP%n", line.get()); break;
                case DUP:           stream.printf("%s: DUP%n", line.get()); break;
                case DUP_AT:        stream.printf("%s: DUP_AT        %d%n", line.get(), buffer.get()); break;
                case BIND:          stream.printf("%s: BIND          %d%n", line.get(), buffer.get()); break;
                default: throw new RuntimeException(String.format("Unknown opcode: %x", buffer.get(buffer.position() - 1)));
                    // @formatter:on
            }
        }
    }

    public Object[] getConstants() {
        return constants.toArray(new Object[0]);
    }

    public int addConstant(Object value) {
        if (!constants.contains(value)) {
            constants.add(value);
        }
        return constants.indexOf(value);
    }

    public Map<Integer, Region.Span> getTraceLines() {
        return traceLines;
    }

    public void addTraceLine(Region.Span span) {
        traceLines.put(buffer.position(), span);
    }

    public Map<Region.Span, Integer> getDebugLines() {
        return debugLines;
    }

    public void addDebugLine(Region.Span span, String note) {
        debugLines.put(span, buffer.position());
    }

    public int getPosition() {
        return buffer.position();
    }

    public int getRemaining() {
        return buffer.remaining();
    }

    public static class Label {
        private final int position;

        private Label(int position) {
            this.position = position;
        }
    }

    public static class Guard {
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
}
