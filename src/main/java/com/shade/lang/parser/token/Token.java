package com.shade.lang.parser.token;

public class Token {
    private final TokenKind kind;
    private final Region region;
    private final String value;

    public Token(TokenKind kind, Region region, String value) {
        this.kind = kind;
        this.region = region;
        this.value = value;
    }

    public Token(TokenKind kind, Region region) {
        this.kind = kind;
        this.region = region;
        this.value = "";
    }

    public TokenKind getKind() {
        return kind;
    }

    public Region getRegion() {
        return region;
    }

    public String getValue() {
        return value;
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
