package com.shade.lang.parser.token;

import java.util.HashMap;
import java.util.Map;

public enum TokenKind {
    /* Special */
    Symbol("symbol", 0, TokenFlag.DISPLAY),
    Number("number", 0, TokenFlag.DISPLAY),
    String("string", 0, TokenFlag.DISPLAY),
    StringPart("string part", 0, TokenFlag.DISPLAY),
    End("end of file", 0, TokenFlag.DISPLAY),

    /* Keywords */
    Let("let", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Def("def", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    If("if", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Else("else", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Return("return", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Not("not", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Import("import", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Assert("assert", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Try("try", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Recover("recover", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    For("for", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Loop("loop", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    While("while", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Continue("continue", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Break("break", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Use("use", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Class("class", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    New("new", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Super("super", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    Constructor("constructor", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),
    In("in", 0, TokenFlag.QUOTED | TokenFlag.KEYWORD),

    /* Operators */
    ParenL("(", 0, TokenFlag.QUOTED),
    ParenR(")", 0, TokenFlag.QUOTED),
    BraceL("{", 0, TokenFlag.QUOTED),
    BraceR("}", 0, TokenFlag.QUOTED),
    Semicolon(";", 0, TokenFlag.QUOTED),
    Colon(":", 0, TokenFlag.QUOTED),
    Comma(",", 0, TokenFlag.QUOTED),
    Dot(".", 0, TokenFlag.QUOTED),
    Range("..", 0, TokenFlag.QUOTED),
    RangeInc("..=", 0, TokenFlag.QUOTED),

    Assign("=", 1, TokenFlag.QUOTED | TokenFlag.ASSIGNMENT | TokenFlag.RIGHT_ASSOCIATIVE),
    AddAssign("+=", 1, TokenFlag.QUOTED | TokenFlag.ASSIGNMENT | TokenFlag.RIGHT_ASSOCIATIVE),
    SubAssign("-=", 1, TokenFlag.QUOTED | TokenFlag.ASSIGNMENT | TokenFlag.RIGHT_ASSOCIATIVE),
    MulAssign("*=", 1, TokenFlag.QUOTED | TokenFlag.ASSIGNMENT | TokenFlag.RIGHT_ASSOCIATIVE),
    DivAssign("/=", 1, TokenFlag.QUOTED | TokenFlag.ASSIGNMENT | TokenFlag.RIGHT_ASSOCIATIVE),

    Or("or", 1, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.LOGICAL | TokenFlag.KEYWORD),
    And("and", 2, TokenFlag.QUOTED | TokenFlag.BINARY | TokenFlag.LOGICAL | TokenFlag.KEYWORD),

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

    public static final Map<String, TokenKind> KEYWORDS = new HashMap<>();

    static {
        for (TokenKind kind : values()) {
            if (kind.hasFlag(TokenFlag.KEYWORD)) {
                KEYWORDS.put(kind.name, kind);
            }
        }
    }

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
