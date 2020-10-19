package com.shade.lang.parser.token;

public enum TokenKind {
    /* Special */
    Symbol("symbol", false, true),
    Number("number", false, true),
    End("end of file", false, true),

    /* Keywords */
    Let("let", true, false),
    Def("def", true, false),
    If("if", true, false),
    Else("else", true, false),
    Return("return", true, false),

    /* Operators */
    ParenL("(", true, false),
    ParenR(")", true, false),
    BraceL("{", true, false),
    BraceR("}", true, false),
    Assign("=", true, false),
    Semicolon(";", true, false),
    Comma(",", true, false),
    Dot(".", true, false),

    Add("+", true, false),
    Sub("-", true, false),
    Mul("*", true, false),
    Div("/", true, false);

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
