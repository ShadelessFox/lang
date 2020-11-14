package com.shade.lang.parser.token;

public class Token {
    private final TokenKind kind;
    private final Region region;
    private final Object value;

    public Token(TokenKind kind, Region region, Object value) {
        this.kind = kind;
        this.region = region;
        this.value = value;
    }

    public Token(TokenKind kind, Region region) {
        this.kind = kind;
        this.region = region;
        this.value = null;
    }

    public TokenKind getKind() {
        return kind;
    }

    public Region getRegion() {
        return region;
    }

    public Object getValue() {
        return value;
    }

    public String getStringValue() {
        return (String) value;
    }

    public Integer getIntegerValue() {
        return (Integer) value;
    }

    @Override
    public String toString() {
        return "Token{" +
            "kind=" + kind +
            ", region=" + region +
            ", value='" + value + '\'' +
            '}';
    }
}
