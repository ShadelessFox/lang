package com.shade.lang.compiler;

public class Opcode {
    // @formatter:off

    /* Data constants */
    public static final byte PUSH_CONST    = 0x01;
    public static final byte PUSH_INT      = 0x02;

    /* Data exchange */
    public static final byte GET_GLOBAL    = 0x03;
    public static final byte SET_GLOBAL    = 0x04;
    public static final byte GET_LOCAL     = 0x05;
    public static final byte SET_LOCAL     = 0x06;
    public static final byte GET_ATTRIBUTE = 0x07;
    public static final byte SET_ATTRIBUTE = 0x08;

    /* Common operators */
    public static final byte ADD           = 0x09;
    public static final byte SUB           = 0x0A;
    public static final byte MUL           = 0x0B;
    public static final byte DIV           = 0x0C;

    /* Bitwise operators */
    public static final byte NOT           = 0x0D;
    public static final byte AND           = 0x0E;
    public static final byte OR            = 0x0F;
    public static final byte XOR           = 0x10;
    public static final byte SHL           = 0x11;
    public static final byte SHR           = 0x12;

    /* Branching */
    public static final byte JUMP          = 0x13;
    public static final byte JUMP_IF_TRUE  = 0x14;
    public static final byte JUMP_IF_FALSE = 0x15;
    public static final byte CMP_EQ        = 0x16;
    public static final byte CMP_NE        = 0x17;
    public static final byte CMP_LT        = 0x18;
    public static final byte CMP_LE        = 0x19;
    public static final byte CMP_GT        = 0x1A;
    public static final byte CMP_GE        = 0x1B;

    /* Invocation */
    public static final byte CALL          = 0x1C;
    public static final byte RET           = 0x1D;
    public static final byte POP           = 0x1E;

    /* Debugging */
    public static final byte ASSERT        = 0x1F;
    public static final byte BREAKPOINT    = 0x20;


    // @formatter:on

    private Opcode() {
    }
}