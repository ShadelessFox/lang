package com.shade.lang.compiler.assembler;

/**
 * This class contains constant indices (op-codes) for each operation.
 *
 * @see Operation
 */
public class OperationCode {
    private OperationCode() {
    }

    // @formatter:off
    public static final byte OP_PUSH            = 0x01;
    public static final byte OP_DUP             = 0x02;
    public static final byte OP_DUP_AT          = 0x03;
    public static final byte OP_POP             = 0x04;

    public static final byte OP_GET_GLOBAL      = 0x05;
    public static final byte OP_SET_GLOBAL      = 0x06;
    public static final byte OP_GET_LOCAL       = 0x07;
    public static final byte OP_SET_LOCAL       = 0x08;
    public static final byte OP_GET_ATTRIBUTE   = 0x09;
    public static final byte OP_SET_ATTRIBUTE   = 0x0A;
    public static final byte OP_GET_INDEX       = 0x0B;
    public static final byte OP_SET_INDEX       = 0x0C;

    public static final byte OP_ADD             = 0x0D;
    public static final byte OP_SUB             = 0x0E;
    public static final byte OP_MUL             = 0x0F;
    public static final byte OP_DIV             = 0x10;
    public static final byte OP_AND             = 0x11;
    public static final byte OP_OR              = 0x12;
    public static final byte OP_XOR             = 0x13;
    public static final byte OP_SHL             = 0x14;
    public static final byte OP_SHR             = 0x15;
    public static final byte OP_NOT             = 0x16;

    public static final byte OP_BIND            = 0x17;
    public static final byte OP_CALL            = 0x18;
    public static final byte OP_RETURN          = 0x19;

    public static final byte OP_JUMP            = 0x1A;
    public static final byte OP_JUMP_IF_TRUE    = 0x1B;
    public static final byte OP_JUMP_IF_FALSE   = 0x1C;

    public static final byte OP_CMP_EQ          = 0x1D;
    public static final byte OP_CMP_NE          = 0x1E;
    public static final byte OP_CMP_LT          = 0x1F;
    public static final byte OP_CMP_LE          = 0x20;
    public static final byte OP_CMP_GT          = 0x21;
    public static final byte OP_CMP_GE          = 0x22;

    public static final byte OP_ASSERT          = 0x23;
    public static final byte OP_IMPORT          = 0x24;
    public static final byte OP_NEW             = 0x25;
    public static final byte OP_SUPER           = 0x26;
    public static final byte OP_INSTANCE_OF     = 0x27;

    public static final byte OP_MAKE_FUNCTION   = 0x28;
    public static final byte OP_MAKE_CLASS      = 0x29;

    public static final byte OP_THROW           = 0x2A;

    public static final byte OP_JUMP_IF_TRUE_OR_POP     = 0x2B;
    public static final byte OP_JUMP_IF_FALSE_OR_POP    = 0x2C;
    // @formatter:on

}
