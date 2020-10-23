package com.shade.lang.parser.token;

public class TokenFlag {
    public static final int QUOTED     = 1 << 1;
    public static final int DISPLAY    = 1 << 2;
    public static final int BINARY     = 1 << 3;
    public static final int LOGICAL    = 1 << 4;
    public static final int RELATIONAL = 1 << 5;

    private TokenFlag() {
    }
}
