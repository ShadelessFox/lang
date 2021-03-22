package com.shade.lang.compiler.assembler;

import com.shade.lang.util.annotations.NotNull;

public class Verifier {
    private final Instruction[] instructions;

    public Verifier(@NotNull Instruction[] instructions) {
        this.instructions = instructions;
    }

    public void verify() throws VerificationException {
        verifyJumpDestinations();
        verifyStackFrameSize();
        verifyConstantTypes();
    }

    private void verifyJumpDestinations() throws VerificationException {
        int position = 0;
        for (Instruction instruction : instructions) {
            final Operation operation = instruction.getOperation();
            if (operation.isJump()) {
                final int relativePosition = instruction.getOperands()[0].getImm16();
                final int absolutePosition = position + relativePosition + instruction.getSize();
                if (!hasInstructionAt(absolutePosition)) {
                    throw new VerificationException(String.format(
                        "Jump instruction '%s' at position %2$d (%2$#04x) points in-between instruction at position %3$d (%3$#04x)",
                        operation,
                        position,
                        absolutePosition
                    ));
                }
            }
            position += instruction.getSize();
        }
    }

    private void verifyStackFrameSize() throws VerificationException {
        int currentStackSize = 0;
        int maxStackSize = 0;

        for (Instruction instruction : instructions) {
            final Operation operation = instruction.getOperation();
            final int stackPopAffect = operation.getStackPop(instruction.getOperands());
            final int stackPushAffect = operation.getStackPush(instruction.getOperands());

            if (currentStackSize - stackPopAffect < 0) {
                throw new VerificationException(String.format(
                    "Reached negative stack size on instruction '%s': required %d element(-s) on the stack but only %d is available",
                    operation,
                    stackPopAffect,
                    currentStackSize
                ));
            }

            if (operation == Operation.DUP_AT) {
                final int relativePosition = instruction.getOperands()[0].getImm8();
                final int absolutePosition = currentStackSize + relativePosition;
                if (absolutePosition < 0 || absolutePosition > currentStackSize) {
                    throw new VerificationException(String.format(
                        "Instruction '%s' attempted to access stack at position %2$d (%2$#04x) but current stack size is %3$d",
                        operation,
                        absolutePosition,
                        currentStackSize
                    ));
                }
            }

            currentStackSize -= stackPopAffect;
            currentStackSize += stackPushAffect;
            maxStackSize = Math.max(maxStackSize, currentStackSize);
        }
    }

    private void verifyConstantTypes() throws VerificationException {
        for (Instruction instruction : instructions) {
            final Operation operation = instruction.getOperation();
            final Operand[] operands = instruction.getOperands();
            switch (operation) {
                case GET_GLOBAL:
                case SET_GLOBAL:
                case GET_ATTRIBUTE:
                case SET_ATTRIBUTE:
                case IMPORT:
                    if (!(operands[0].getConstant() instanceof String)) {
                        throw new VerificationException(String.format(
                            "Constant symbol operand of instruction '%s' expected to be string but found %s (%s)",
                            instruction,
                            operands[0].getType(),
                            operands[0].getValue()
                        ));
                    }
                    break;
            }
        }
    }

    private boolean hasInstructionAt(int requiredPosition) {
        int position = 0;
        for (Instruction instruction : instructions) {
            if (position == requiredPosition) {
                return true;
            }
            position += instruction.getSize();
        }
        return false;
    }

    public static class VerificationException extends Exception {
        public VerificationException(@NotNull String message) {
            super(message);
        }
    }
}
