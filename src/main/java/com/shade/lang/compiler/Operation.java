package com.shade.lang.compiler;

import java.util.function.Function;

/**
 * This class contains information about each operation,
 * such as required operands and affect to program stack
 * to prevent corruption (removed elements, added elements).
 */
public enum Operation {
    PUSH(OperationCode.OP_PUSH, new OperandType[]{OperandType.CONSTANT}, 0, 1),
    DUP(OperationCode.OP_DUP, new OperandType[]{}, 1, 2),
    DUP_AT(OperationCode.OP_DUP_AT, new OperandType[]{OperandType.IMM_8}, 1, 2),
    POP(OperationCode.OP_POP, new OperandType[]{}, 0, 1),

    GET_GLOBAL(OperationCode.OP_GET_GLOBAL, new OperandType[]{OperandType.CONSTANT}, 0, 1),
    SET_GLOBAL(OperationCode.OP_SET_GLOBAL, new OperandType[]{OperandType.CONSTANT}, 1, 0),
    GET_LOCAL(OperationCode.OP_GET_LOCAL, new OperandType[]{OperandType.IMM_8}, 0, 1),
    SET_LOCAL(OperationCode.OP_SET_LOCAL, new OperandType[]{OperandType.IMM_8}, 1, 0),
    GET_ATTRIBUTE(OperationCode.OP_GET_ATTRIBUTE, new OperandType[]{OperandType.CONSTANT}, 1, 1),
    SET_ATTRIBUTE(OperationCode.OP_SET_ATTRIBUTE, new OperandType[]{OperandType.CONSTANT}, 2, 0),
    GET_INDEX(OperationCode.OP_GET_INDEX, new OperandType[0], 2, 1),
    SET_INDEX(OperationCode.OP_SET_INDEX, new OperandType[0], 3, 0),

    ADD(OperationCode.OP_ADD, new OperandType[]{}, 2, 1),
    SUB(OperationCode.OP_SUB, new OperandType[]{}, 2, 1),
    MUL(OperationCode.OP_MUL, new OperandType[]{}, 2, 1),
    DIV(OperationCode.OP_DIV, new OperandType[]{}, 2, 1),
    AND(OperationCode.OP_AND, new OperandType[]{}, 2, 1),
    OR(OperationCode.OP_OR, new OperandType[]{}, 2, 1),
    XOR(OperationCode.OP_XOR, new OperandType[]{}, 2, 1),
    SHL(OperationCode.OP_SHL, new OperandType[]{}, 2, 1),
    SHR(OperationCode.OP_SHR, new OperandType[]{}, 2, 1),
    NOT(OperationCode.OP_NOT, new OperandType[]{}, 1, 1),

    BIND(OperationCode.OP_BIND, new OperandType[]{OperandType.IMM_8}, 2, 0),
    CALL(OperationCode.OP_CALL, new OperandType[]{OperandType.IMM_8}, ops -> ops[0].getImm8() + 1, ops -> 1),
    RETURN(OperationCode.OP_RETURN, new OperandType[0], 1, 0),

    JUMP(OperationCode.OP_JUMP, new OperandType[]{OperandType.IMM_16}, 0, 0),
    JUMP_IF_TRUE(OperationCode.OP_JUMP_IF_TRUE, new OperandType[]{OperandType.IMM_16}, 1, 0),
    JUMP_IF_FALSE(OperationCode.OP_JUMP_IF_FALSE, new OperandType[]{OperandType.IMM_16}, 1, 0),

    CMP_EQ(OperationCode.OP_CMP_EQ, new OperandType[]{}, 2, 1),
    CMP_NE(OperationCode.OP_CMP_NE, new OperandType[]{}, 2, 1),
    CMP_LT(OperationCode.OP_CMP_LT, new OperandType[]{}, 2, 1),
    CMP_LE(OperationCode.OP_CMP_LE, new OperandType[]{}, 2, 1),
    CMP_GT(OperationCode.OP_CMP_GT, new OperandType[]{}, 2, 1),
    CMP_GE(OperationCode.OP_CMP_GE, new OperandType[]{}, 2, 1),

    ASSERT(OperationCode.OP_ASSERT, new OperandType[]{OperandType.CONSTANT, OperandType.CONSTANT}, 1, 0),
    IMPORT(OperationCode.OP_IMPORT, new OperandType[]{OperandType.CONSTANT, OperandType.IMM_8}, 0, 0),
    NEW(OperationCode.OP_NEW, new OperandType[]{}, 1, 1);

    private final byte opcode;
    private final OperandType[] operands;
    private final Function<Operand[], Integer> stackPop;
    private final Function<Operand[], Integer> stackPush;

    Operation(byte opcode, OperandType[] operands, Function<Operand[], Integer> stackPop, Function<Operand[], Integer> stackPush) {
        this.opcode = opcode;
        this.operands = operands;
        this.stackPop = stackPop;
        this.stackPush = stackPush;
    }

    Operation(byte opcode, OperandType[] operands, int stackPop, int stackPush) {
        this(opcode, operands, ops -> stackPop, ops -> stackPush);
    }

    public boolean isJump() {
        return this == JUMP || this == JUMP_IF_TRUE || this == JUMP_IF_FALSE;
    }

    public byte getOpcode() {
        return opcode;
    }

    public OperandType[] getOperands() {
        return operands;
    }

    public int getStackPop(Operand[] operands) {
        return stackPop.apply(operands);
    }

    public int getStackPush(Operand[] operands) {
        return stackPush.apply(operands);
    }
}
