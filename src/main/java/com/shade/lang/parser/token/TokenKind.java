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
    And("and", 0, TokenFlag.QUOTED | TokenFlag.BRANCHING),
    Or("or", 0, TokenFlag.QUOTED | TokenFlag.BRANCHING),
    Import("import", 0, TokenFlag.QUOTED),

    /* Operators */
    ParenL("(", 0, TokenFlag.QUOTED),
    ParenR(")", 0, TokenFlag.QUOTED),
    BraceL("{", 0, TokenFlag.QUOTED),
    BraceR("}", 0, TokenFlag.QUOTED),
    Assign("=", 0, TokenFlag.QUOTED),
    Semicolon(";", 0, TokenFlag.QUOTED),
    Comma(",", 0, TokenFlag.QUOTED),
    Dot(".", 0, TokenFlag.QUOTED),

    Eq("==", 0, TokenFlag.QUOTED | TokenFlag.BRANCHING),
    NotEq("!=", 0, TokenFlag.QUOTED | TokenFlag.BRANCHING),
    Less("<", 0, TokenFlag.QUOTED | TokenFlag.BRANCHING),
    LessEq("<=", 0, TokenFlag.QUOTED | TokenFlag.BRANCHING),
    Greater(">", 0, TokenFlag.QUOTED | TokenFlag.BRANCHING),
    GreaterEq(">=", 0, TokenFlag.QUOTED | TokenFlag.BRANCHING),

    Add("+", 0, TokenFlag.QUOTED), AddAssign("+=", 0, TokenFlag.QUOTED),
    Sub("-", 0, TokenFlag.QUOTED), SubAssign("-=", 0, TokenFlag.QUOTED),
    Mul("*", 0, TokenFlag.QUOTED), MulAssign("*=", 0, TokenFlag.QUOTED),
    Div("/", 0, TokenFlag.QUOTED), DivAssign("/=", 0, TokenFlag.QUOTED),
    BitAnd("&", 0, TokenFlag.QUOTED), BitAndAssign("&=", 0, TokenFlag.QUOTED),
    BitOr("|", 0, TokenFlag.QUOTED), BitOrAssign("|=", 0, TokenFlag.QUOTED),
    Xor("^", 0, TokenFlag.QUOTED), XorAssign("^=", 0, TokenFlag.QUOTED),
    Shl("<<", 0, TokenFlag.QUOTED), ShlAssign("<<=", 0, TokenFlag.QUOTED),
    Shr(">>", 0, TokenFlag.QUOTED), ShrAssign(">>=", 0, TokenFlag.QUOTED);

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
