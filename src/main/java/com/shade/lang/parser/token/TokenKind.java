package com.shade.lang.parser.token;

public enum TokenKind {
    /* Special */
    Symbol("symbol", 0, TokenFlag.DISPLAY),
    Number("number", 0, TokenFlag.DISPLAY),
    String("string", 0, TokenFlag.DISPLAY),
    StringPart("string part", 0, TokenFlag.DISPLAY),
    End("end of file", 0, TokenFlag.DISPLAY),

    /* Keywords */
    Let("let", 0, TokenFlag.QUOTED),
    Def("def", 0, TokenFlag.QUOTED),
    If("if", 0, TokenFlag.QUOTED),
    Else("else", 0, TokenFlag.QUOTED),
    Return("return", 0, TokenFlag.QUOTED),
    Not("not", 0, TokenFlag.QUOTED),
    Import("import", 0, TokenFlag.QUOTED),
    Assert("assert", 0, TokenFlag.QUOTED),
    Try("try", 0, TokenFlag.QUOTED),
    Recover("recover", 0, TokenFlag.QUOTED),
    Loop("loop", 0, TokenFlag.QUOTED),
    While("while", 0, TokenFlag.QUOTED),
    Continue("continue", 0, TokenFlag.QUOTED),
    Break("break", 0, TokenFlag.QUOTED),

    /* Operators */
    ParenL("(", 0, TokenFlag.QUOTED),
    ParenR(")", 0, TokenFlag.QUOTED),
    BraceL("{", 0, TokenFlag.QUOTED),
    BraceR("}", 0, TokenFlag.QUOTED),
    Semicolon(";", 0, TokenFlag.QUOTED),
    Comma(",", 0, TokenFlag.QUOTED),
    Dot(".", 0, TokenFlag.QUOTED),

    Assign("=", 1, TokenFlag.QUOTED | TokenFlag.ASSIGNMENT | TokenFlag.RIGHT_ASSOCIATIVE),
    AddAssign("+=", 1, TokenFlag.QUOTED | TokenFlag.ASSIGNMENT | TokenFlag.RIGHT_ASSOCIATIVE),
    SubAssign("-=", 1, TokenFlag.QUOTED | TokenFlag.ASSIGNMENT | TokenFlag.RIGHT_ASSOCIATIVE),
    MulAssign("*=", 1, TokenFlag.QUOTED | TokenFlag.ASSIGNMENT | TokenFlag.RIGHT_ASSOCIATIVE),
    DivAssign("/=", 1, TokenFlag.QUOTED | TokenFlag.ASSIGNMENT | TokenFlag.RIGHT_ASSOCIATIVE),

    Or("or", 1, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.LOGICAL),
    And("and", 2, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.LOGICAL),

    Eq("==", 3, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.RELATIONAL),
    NotEq("!=", 3, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.RELATIONAL),
    Less("<", 3, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.RELATIONAL),
    LessEq("<=", 3, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.RELATIONAL),
    Greater(">", 3, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.RELATIONAL),
    GreaterEq(">=", 3, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.RELATIONAL),

    Add("+", 4, TokenFlag.QUOTED | TokenFlag.BINARY),
    Sub("-", 4, TokenFlag.QUOTED | TokenFlag.BINARY),
    Mul("*", 5, TokenFlag.QUOTED | TokenFlag.BINARY),
    Div("/", 5, TokenFlag.QUOTED | TokenFlag.BINARY);

    private final String name;
    private final int precedence;
    private final int flags;

    TokenKind(String name, int precedence, int flags) {
        this.name = name;
        this.precedence = precedence;
        this.flags = flags;
    }

    public String getName() {
        return name;
    }

    public int getPrecedence() {
        return precedence;
    }

    public String getQuotedName() {
        return hasFlag(TokenFlag.QUOTED) ? "'" + name + "'" : name;
    }

    public int getFlags() {
        return flags;
    }

    public boolean hasFlag(int flag) {
        return (flags & flag) > 0;
    }
}
