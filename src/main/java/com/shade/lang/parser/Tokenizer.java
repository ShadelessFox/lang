package com.shade.lang.parser;

import com.shade.lang.parser.token.Region;
import com.shade.lang.parser.token.Token;
import com.shade.lang.parser.token.TokenKind;

import java.io.IOException;
import java.io.Reader;

public class Tokenizer {
    private final Reader reader;
    private int line;
    private int column;
    private int offset;
    private char ch;

    public Tokenizer(Reader reader) throws IOException {
        this.reader = reader;
        this.line = 1;
        this.column = 1;
        this.read();
    }

    public Token next() throws ParseException, IOException {
        while (true) {
            Region.Span start = span();

            if (Character.isJavaIdentifierStart(ch)) {
                StringBuilder builder = new StringBuilder();
                while (Character.isJavaIdentifierPart(ch)) {
                    builder.append(read());
                }
                switch (builder.toString()) {
                    case "let":
                        return new Token(TokenKind.Let, new Region(start, span()));
                    case "def":
                        return new Token(TokenKind.Def, new Region(start, span()));
                    case "if":
                        return new Token(TokenKind.If, new Region(start, span()));
                    case "else":
                        return new Token(TokenKind.Else, new Region(start, span()));
                    case "return":
                        return new Token(TokenKind.Return, new Region(start, span()));
                    default:
                        return new Token(TokenKind.Symbol, new Region(start, span()), builder.toString());
                }
            }

            if (ch >= '0' && ch <= '9') {
                StringBuilder builder = new StringBuilder();
                while (ch >= '0' && ch <= '9') {
                    builder.append(read());
                }
                Region.Span end = span();
                return new Token(TokenKind.Number, new Region(start, end), builder.toString());
            }

            if (ch == 65535) {
                return new Token(TokenKind.End, new Region(start, start));
            }

            if (Character.isWhitespace(ch)) {
                while (Character.isWhitespace(ch)) {
                    read();
                }
                continue;
            }

            char ch = read();
            Region chRegion = new Region(start, span());

            switch (ch) {
                case '(':
                    return new Token(TokenKind.ParenL, chRegion);
                case ')':
                    return new Token(TokenKind.ParenR, chRegion);
                case '{':
                    return new Token(TokenKind.BraceL, chRegion);
                case '}':
                    return new Token(TokenKind.BraceR, chRegion);
                case '=':
                    return new Token(TokenKind.Assign, chRegion);
                case ';':
                    return new Token(TokenKind.Semicolon, chRegion);
                case ',':
                    return new Token(TokenKind.Comma, chRegion);
                case '.':
                    return new Token(TokenKind.Dot, chRegion);
                case '+':
                    return new Token(TokenKind.Add, chRegion);
                case '-':
                    return new Token(TokenKind.Sub, chRegion);
                case '*':
                    return new Token(TokenKind.Mul, chRegion);
                case '/':
                    return new Token(TokenKind.Div, chRegion);
                default:
                    throw new ParseException("Unknown symbol: '" + ch + "' (" + (int) ch + ")", chRegion);
            }
        }
    }

    private Region.Span span() {
        return new Region.Span(line, column, offset);
    }

    private char read() throws IOException {
        if (ch > 0) {
            if (ch == '\n') {
                line += 1;
                column = 1;
            } else {
                column += 1;
            }
        }

        char last = ch;

        ch = (char) reader.read();
        offset += 1;

        return last;
    }
}
