package com.shade.lang.parser.token;

public enum TokenKind {
    /* Special */
    Symbol("symbol", 0, TokenFlag.DISPLAY),
    Number("number", 0, TokenFlag.DISPLAY),
    String("string", 0, TokenFlag.DISPLAY),
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
    And("and", 1, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.LOGICAL),
    Or("or", 1, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.LOGICAL),

    /* Operators */
    ParenL("(", 0, TokenFlag.QUOTED),
    ParenR(")", 0, TokenFlag.QUOTED),
    BraceL("{", 0, TokenFlag.QUOTED),
    BraceR("}", 0, TokenFlag.QUOTED),
    Assign("=", 0, TokenFlag.QUOTED),
    Semicolon(";", 0, TokenFlag.QUOTED),
    Comma(",", 0, TokenFlag.QUOTED),
    Dot(".", 0, TokenFlag.QUOTED),

    Eq("==", 2, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.RELATIONAL),
    NotEq("!=", 2, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.RELATIONAL),
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

    public int getFlags() {
        return flags;
    }

    public String getQuotedName() {
        return hasFlag(TokenFlag.QUOTED) ? "'" + name + "'" : name;
    }

    public boolean hasFlag(int flag) {
        return (flags & flag) > 0;
    }
}
