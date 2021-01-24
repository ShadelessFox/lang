package com.shade.lang.compiler.assembler;

import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Assembler {
    /*
     * List of emitted instructions.
     */
    private final List<Instruction> instructions = new ArrayList<>();

    /*
     * List of instructions' constants.
     */
    private final List<Object> constants = new ArrayList<>();

    /*
     * Set of unresolved jump labels. Labels
     * are used to keep track of all emitted
     * jump instructions and resolve them
     * with relative offsets upon bind.
     */
    private final Set<Label> labels = new HashSet<>();

    /*
     * Map of instruction's offsets to its location
     * inside code's source file.
     */
    private final Map<Integer, Region.Span> locations = new HashMap<>();

    /*
     * Current imaginable stack size. This
     * value is updated every time a new
     * instruction is emitted.
     */
    private int currentStackSize;

    /*
     * Max peak of imaginable stack size.
     * This value is updated every time
     * when currentStackSize is greater
     * than current peak.
     */
    private int maxStackSize;

    /**
     * Appends given operation with supplied operands to internal buffer.
     * <p>
     * This method makes sure that given list of {@code operands}
     * is applicable to {@code operation}.
     * For example, this method checks the affect of push/pop on the
     * virtual operand stack. If {@link Operation#isJump()} returns
     * {@code true} for the given {@code operation}, then
     * current state is pushed onto internal jump stack. Note that
     * only forward jumps (e.g. the relative offset is positive) are
     * checked, backward jumps (loops, etc.) are not checked.
     *
     * @param operation the operation a.k.a. opcode of the instruction
     * @param operands  the list of operands suitable for given {@code operation}
     * @return emitted instruction with specified operation and list of operands
     */
    public Instruction emit(@NotNull Operation operation, @NotNull Operand... operands) {
        if (operation.getOperands().length != operands.length) {
            throw new IllegalArgumentException("Operation '" + operation + "' expects " + operation.getOperands().length + " operand(-s)");
        }

        for (int index = 0; index < operation.getOperands().length; index++) {
            if (operation.getOperands()[index] != operands[index].getType()) {
                throw new IllegalArgumentException("Operand at index " + index + " expected to be of type " + operation.getOperands()[index]);
            }
        }

        int stackPopAffect = operation.getStackPop(operands);
        int stackPushAffect = operation.getStackPush(operands);

        if (currentStackSize - stackPopAffect < 0) {
            throw new IllegalStateException("Reached negative stack size on operand '" + operation + "': required " + stackPopAffect + " element(-s) on the stack but " + currentStackSize + " is available");
        }

        currentStackSize -= stackPopAffect;
        currentStackSize += stackPushAffect;

        Instruction instruction;

        if (operation.isJump()) {
            instruction = new JumpInstruction(operation);
        } else {
            instruction = new Instruction(operation, operands);
        }

        instructions.add(instruction);

        if (currentStackSize > maxStackSize) {
            maxStackSize = currentStackSize;
        }

        return instruction;
    }

    /**
     * Appends unresolved jump operation to internal stack.
     * Returns label that refers to emitted jump instruction
     * in order to resolve its operand when label will be
     * bound using method {@link Assembler#bind(Label)}.
     * This is useful for forward jumps where resulting offset
     * is unknown.
     *
     * @param operation jump operation to be emitted
     * @return unresolved label that must be bound using {@link Assembler#bind(Label)}
     * @throws IllegalArgumentException if provided {@code operation} is not a jump operation
     * @see Assembler#bind(Label)
     * @see Operation#isJump()
     */
    public Label jump(@NotNull Operation operation) {
        if (!operation.isJump()) {
            throw new IllegalArgumentException("Operation expected to be a jump");
        }

        JumpInstruction instruction = (JumpInstruction) emit(operation, Operand.imm16(0xffff));

        Label label = new Label(instruction, currentStackSize);
        labels.add(label);

        return label;
    }

    /**
     * Binds referred instruction to offset between
     * this instruction and current assembler's position.
     * After label is bound and thus resolved, it cannot
     * be bound again.
     * <p>
     * Note that jump offset is limited to {@code 2^16 (65535)} bytes.
     *
     * @param label label returned from {@link #jump(Operation)}
     *              to be bound and resolved
     * @throws IllegalArgumentException if label is already resolved,
     *                                  or it was created by another assembler
     * @throws IllegalStateException    if resolved jump offset is too big
     *                                  or it was already resolved
     */
    public void bind(@Nullable Label label) {
        if (label == null) {
            return;
        }

        if (!labels.remove(label)) {
            throw new IllegalArgumentException("Cannot bind orphan label");
        }

        JumpInstruction jump = label.instruction;

        if (jump.isResolved()) {
            throw new IllegalStateException("Jump already resolved");
        }

        int offset = instructions
            .stream()
            .skip(instructions.indexOf(jump))
            .mapToInt(Instruction::getSize)
            .sum();

        // Decrement offset by instruction size because
        // jump will happen after instruction is read.

        offset -= jump.getSize();

        if (offset > 0xffff) {
            throw new IllegalStateException("Jump offset is too big (max 65535)");
        }

        currentStackSize = label.stack;

        jump.resolve((short) (offset & 0xffff));
    }

    /**
     * Binds referred instruction to offset
     * specified by {@code position} that lays
     * between assembler's start and end.
     * After label is bound and thus resolved, it cannot
     * be bound again.
     * <p>
     * Note that jump offset is limited to {@code 2^16 (65535)} bytes.
     *
     * @param label    label returned from {@link #jump(Operation)}
     *                 to be bound and resolved
     * @param position position that is returned from {@link Assembler#getPosition()}.
     *                 This is an index of the destination instruction inside internal buffer.
     * @throws IllegalArgumentException if label is already resolved,
     *                                  or it was created by another assembler,
     *                                  or position is invalid
     * @throws IllegalStateException    if resolved jump offset is too big
     *                                  or it was already resolved
     */
    public void bind(@Nullable Label label, int position) {
        if (label == null) {
            return;
        }

        if (instructions.size() < position) {
            throw new IllegalArgumentException("Invalid position value: " + position);
        }

        if (!labels.remove(label)) {
            throw new IllegalArgumentException("Cannot bind orphan label");
        }

        JumpInstruction jump = label.instruction;

        if (jump.isResolved()) {
            throw new IllegalStateException("Jump already resolved");
        }

        int offset = instructions
            .stream()
            .limit(instructions.indexOf(jump) + 1)
            .skip(position)
            .mapToInt(Instruction::getSize)
            .sum();

        if (offset > 0xffff) {
            throw new IllegalStateException("Jump offset is too big (max 65535)");
        }

        currentStackSize = label.stack;

        // Because we're limited to the index of already
        // emitted instructions, position will always point
        // past current position, so offset must be negated.

        jump.resolve((short) -(offset & 0xffff));
    }

    /**
     * Assembles (encodes) all instructions into one long {@link ByteBuffer}.
     *
     * @return byte buffer with encoded instructions
     * @throws IllegalStateException if not all jumps were resolved
     * @see Instruction#emit(Assembler, ByteBuffer)
     * @see Operand#emit(Assembler, ByteBuffer)
     */
    public ByteBuffer assemble() {
        if (!labels.isEmpty()) {
            throw new IllegalStateException("Not all jumps were resolved");
        }

        ByteBuffer buffer = ByteBuffer
            .allocate(instructions
                .stream()
                .mapToInt(Instruction::getSize)
                .sum());

        for (Instruction instruction : instructions) {
            instruction.emit(this, buffer);
        }

        return (ByteBuffer) ByteBuffer
            .allocate(buffer.position())
            .put(buffer.array(), 0, buffer.position())
            .position(0);
    }

    /**
     * Prints human-friendly assembly dump with
     * instructions' mnemonics, operands and jump
     * labels to the standard output.
     */
    public void print(PrintWriter writer) {
        Map<Integer, Integer> labels = new HashMap<>();

        boolean isLabelsPresent = instructions.stream()
            .anyMatch(x -> x.getOperation().isJump());

        int instructionMaxName = instructions.stream()
            .mapToInt(x -> x.getOperation().toString().length())
            .max()
            .orElse(0);

        int instructionMaxSize = instructions
            .stream()
            .mapToInt(Instruction::getSize)
            .max()
            .orElse(0) * 3 - 1;

        int instructionOffset = 0;

        for (Instruction instruction : instructions) {
            ByteBuffer buffer = ByteBuffer.allocate(instruction.getSize());
            instruction.emit(this, buffer);

            StringBuilder builder = new StringBuilder();

            if (isLabelsPresent) {
                if (labels.containsKey(instructionOffset)) {
                    builder.append(String.format("\033[" + 37 + "m#L%02d\033[0m ", labels.get(instructionOffset)));
                } else {
                    builder.append("     ");
                }
            }

            builder.append(String.format(
                "%08x \033[37m%-" + instructionMaxSize + "s\033[0m %-" + instructionMaxName + "s %s",
                instructionOffset,
                IntStream.range(0, buffer.capacity())
                    .mapToObj(x -> String.format("%02x", buffer.get(x)))
                    .collect(Collectors.joining(" ")),
                instruction.getOperation(),
                Arrays.stream(instruction.getOperands())
                    .map(Operand::toDisplayString)
                    .collect(Collectors.joining(" "))
            ));

            instructionOffset += instruction.getSize();

            if (instruction.getOperation().isJump()) {
                final int value = instructionOffset + instruction.getOperands()[0].getImm16();
                labels.putIfAbsent(value, labels.size() + 1);
                builder.append(String.format(" \033[37m(#L%02d)\033[0m", labels.get(value)));
            }

            writer.println(builder.toString());
        }
    }

    /**
     * Associates current instruction with specified
     * {@code line} and {@code column}
     * position inside source file that can be used
     * for analysis purposes.
     *
     * @param span span inside source file
     */
    public void addLocation(@NotNull Region.Span span) {
        locations.put(instructions.size(), span);
    }

    /**
     * Obtains zero-based index of supplied constant.
     * Stores constant if no such constant was saved before.
     *
     * @param constant constant to get index of
     * @return index of supplied constant
     */
    public int getConstantIndex(@NotNull Object constant) {
        if (!constants.contains(constant)) {
            constants.add(constant);
        }
        return constants.indexOf(constant);
    }

    /**
     * Returns unmodifiable list of emitted
     * instructions of this assembler.
     *
     * @return list of instructions
     */
    public List<Instruction> getInstructions() {
        return Collections.unmodifiableList(instructions);
    }

    /**
     * Returns unmodifiable list of loaded
     * constant values of this assembler.
     *
     * @return list of constants
     */
    public List<Object> getConstants() {
        return Collections.unmodifiableList(constants);
    }

    /**
     * Returns mappings of instruction indices
     * to its location inside source file.
     *
     * @return instruction source file mappings
     */
    public Map<Integer, Region.Span> getLocations() {
        return locations;
    }

    /**
     * Returns computed mappings of instruction
     * offsets to its location inside source file.
     * <p>
     * Unlike {@link #getLocations()}, this method
     * computes byte offset of an instruction instead
     * of its index.
     *
     * @return instruction source file mappings.
     */
    public Map<Integer, Region.Span> getComputedLocations() {
        return locations.entrySet().stream().collect(Collectors.toMap(
            e -> getOffset(e.getKey()),
            Map.Entry::getValue
        ));
    }

    /**
     * Returns the current stack size. This value
     * is updated every time a new instruction is
     * emitted. It cannot be negative.
     *
     * @return current size of internal stack pointer
     */
    public int getCurrentStackSize() {
        return currentStackSize;
    }

    /**
     * Returns the max peak of stack size. This value
     * is updated every time when {@code currentStackSize}
     * is greater than current peak. This could be useful
     * if the execution frame must know exact amount of
     * stack elements it requires.
     *
     * @return max peak size of internal stack pointer
     */
    public int getMaxStackSize() {
        return maxStackSize;
    }

    /**
     * Returns current position inside internal instruction buffer.
     *
     * @return current position inside instruction buffer
     */
    public int getPosition() {
        return instructions.size();
    }

    /**
     * Returns byte offset for instruction at desired position.
     *
     * @param position position of desired instruction
     * @return byte offset for instruction
     * @throws IllegalArgumentException IF position is invalid
     */
    public int getOffset(int position) {
        if (instructions.size() < position) {
            throw new IllegalArgumentException("Invalid position value: " + position);
        }

        return instructions
            .stream()
            .limit(position + 1)
            .mapToInt(Instruction::getSize)
            .sum();
    }

    /**
     * This class is used to store reference to the
     * jump instruction with some additional information
     * that is useful for stack analysis. It cannot be
     * constructed directly, use {@link Assembler#jump(Operation)}.
     */
    public static class Label {
        private final JumpInstruction instruction;
        private final int stack;

        private Label(@NotNull JumpInstruction instruction, int stack) {
            this.instruction = instruction;
            this.stack = stack;
        }
    }

    private static class JumpInstruction extends Instruction {
        private boolean resolved;

        public JumpInstruction(@NotNull Operation operation) {
            super(operation, new Operand[]{Operand.imm16(0xffff)});
        }

        @Override
        public void emit(@NotNull Assembler assembler, @NotNull ByteBuffer buffer) {
            if (!resolved) {
                throw new IllegalStateException("Jump offset is not resolved");
            }
            super.emit(assembler, buffer);
        }

        public void resolve(short offset) {
            getOperands()[0] = Operand.imm16(offset);
            resolved = true;
        }

        public boolean isResolved() {
            return resolved;
        }

        @Override
        public boolean equals(Object o) {
            return this == o;
        }
    }
}
