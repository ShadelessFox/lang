package com.shade.lang.parser.token;

public class TokenFlag {
    public static final int QUOTED = 1 << 1;
    public static final int DISPLAY   = 1 << 2;
    public static final int BRANCHING = 1 << 3;

    private TokenFlag() {
    }
}
