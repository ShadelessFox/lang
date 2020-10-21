package com.shade.lang.parser.token;

public enum TokenKind {
    /* Special */
    Symbol("symbol", false, true),
    Number("number", false, true),
    String("string", false, true),
    End("end of file", false, false),

    /* Keywords */
    Let("let", true, false),
    Def("def", true, false),
    If("if", true, false),
    Else("else", true, false),
    Return("return", true, false),
    Not("not", true, false),
    And("and", true, false),
    Or("or", true, false),

    /* Operators */
    ParenL("(", true, false),
    ParenR(")", true, false),
    BraceL("{", true, false),
    BraceR("}", true, false),
    Assign("=", true, false),
    Semicolon(";", true, false),
    Comma(",", true, false),
    Dot(".", true, false),

    Eq("==", true, false),
    NotEq("==", true, false),
    Less("<", true, false),
    LessEq("<=", true, false),
    Greater(">", true, false),
    GreaterEq(">=", true, false),

    Add("+", true, false),    AddAssign("+=", true, false),
    Sub("-", true, false),    SubAssign("-=", true, false),
    Mul("*", true, false),    MulAssign("*=", true, false),
    Div("/", true, false),    DivAssign("/=", true, false),
    BitAnd("&", true, false), BitAndAssign("&=", true, false),
    BitOr("|", true, false),  BitOrAssign("|=", true, false),
    Xor("^", true, false),    XorAssign("^=", true, false),
    Shl("<<", true, false),   ShlAssign("<<=", true, false),
    Shr(">>", true, false),   ShrAssign(">>=", true, false);

    private final String name;
    private final boolean quoted;
    private final boolean display;

    TokenKind(String name, boolean quote, boolean display) {
        this.name = name;
        this.quoted = quote;
        this.display = display;
    }

    public String getName() {
        return name;
    }

    public String getQuotedName() {
        return quoted ? "'" + name + "'" : name;
    }

    public boolean isQuoted() {
        return quoted;
    }

    public boolean isDisplay() {
        return display;
    }
}
