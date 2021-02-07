package com.shade.lang.compiler.parser;

import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.compiler.parser.token.Token;
import com.shade.lang.compiler.parser.token.TokenKind;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

public class Tokenizer {
    private static final char CHAR_EOF = '\uFFFF';
    private static final char CHAR_NEWLINE = '\n';

    private static final char NUMBER_DIGIT_SEPARATOR = '_';
    private static final char NUMBER_DECIMAL_SEPARATOR = '.';

    private final Reader reader;
    private final StringBuilder buffer;
    private final Stack<Mode> modeStack;
    private Region.Span start;
    private int line;
    private int column;
    private int offset;
    private char ch;
    private char lookahead;

    public Tokenizer(Reader reader) throws ScriptException {
        this.reader = reader;
        this.buffer = new StringBuilder();
        this.modeStack = new Stack<>();
        this.line = 1;
        this.column = 1;
        this.fill();
    }

    public Token next() throws ScriptException {
        while (true) {
            if (Character.isWhitespace(ch)) {
                while (Character.isWhitespace(ch)) {
                    read();
                }
                continue;
            }

            if (ch == '#') {
                while (ch != CHAR_EOF && ch != '\n') {
                    read();
                }
                continue;
            }

            start = tell();

            if (Character.isJavaIdentifierStart(ch)) {
                StringBuilder builder = new StringBuilder();

                while (Character.isJavaIdentifierPart(ch)) {
                    builder.append(read());
                }

                TokenKind keyword = TokenKind.KEYWORDS.get(builder.toString());

                if (keyword == null) {
                    return make(TokenKind.Symbol, builder.toString());
                } else {
                    return make(keyword);
                }
            }

            if (ch == '\'' || ch == '}' && !modeStack.empty() && modeStack.peek() == Mode.Inside) {
                if (ch == '\'') {
                    modeStack.push(Mode.Normal);
                }

                if (ch == '}') {
                    start = start.offsetBy(1);
                    modeStack.pop();
                }

                StringBuilder builder = new StringBuilder();
                read();

                while (true) {
                    char next = read();

                    if (next == CHAR_EOF || next == CHAR_NEWLINE) {
                        error("String literal is not closed");
                    }

                    if (next == '\'') {
                        modeStack.pop();
                        return make(TokenKind.String, builder.toString());
                    }

                    if (next == '\\') {
                        char escape = read();

                        switch (escape) {
                            case 'b':
                                next = '\b';
                                break;
                            case 't':
                                next = '\t';
                                break;
                            case 'n':
                                next = '\n';
                                break;
                            case 'r':
                                next = '\r';
                                break;
                            case '\"':
                                next = '\"';
                                break;
                            case '\'':
                                next = '\'';
                                break;
                            case '\\':
                                next = '\\';
                                break;
                            case 'x': {
                                int value = readEscape(2, 2, Radix.Hexadecimal);
                                if (value > 0x7F) {
                                    error("Hexadecimal escape sequence must be within the range \\x{00} .. \\x{7f}");
                                }
                                next = (char) value;
                                break;
                            }
                            case 'o': {
                                int value = readEscape(3, 3, Radix.Octal);
                                if (value > 0x7F) {
                                    error("Octal escape sequence must be within the range \\o{000} .. \\o{177}");
                                }
                                next = (char) value;
                                break;
                            }
                            case 'u': {
                                int value = readEscape(2, 6, Radix.Hexadecimal);
                                if (value > 0x10FFFF) {
                                    error("Unicode escape sequence must be within the range \\u{00} .. \\u{10ffff}");
                                }
                                if (value >= 0xD800 && 0xDFFF >= value) {
                                    error("Unicode escape sequence must not contain surrogate pairs");
                                }
                                if (value > 0x10000) {
                                    builder.append((char) ((value - 0x10000 >> 10) + 0xD800));
                                    next = (char) ((value - 0x10000 & 0x3ff) + 0xDC00);
                                } else {
                                    next = (char) value;
                                }
                                break;
                            }
                            case '{':
                                modeStack.push(Mode.Inside);
                                return make(TokenKind.StringPart, start, tell().offsetBy(-2), builder.toString());
                            default:
                                error("Unknown string literal escape sequence: \\%c", escape);
                        }
                    }

                    builder.append(next);
                }
            }

            if (ch >= '0' && ch <= '9') {
                StringBuilder builder = new StringBuilder();
                boolean seenDecimalSeparator = false;
                boolean seenScientificNotation = false;
                Radix radix = Radix.Decimal;

                if (ch == '0') {
                    read();

                    if (ch >= '0' && ch <= '9') {
                        error("Zero digit must not be followed by any other digits");
                    }

                    boolean foundRadixPrefix = false;

                    switch (ch) {
                        case 'b':
                            radix = Radix.Binary;
                            foundRadixPrefix = true;
                            break;
                        case 'o':
                            radix = Radix.Octal;
                            foundRadixPrefix = true;
                            break;
                        case 'x':
                            radix = Radix.Hexadecimal;
                            foundRadixPrefix = true;
                            break;
                        default:
                            builder.append('0');
                    }

                    if (foundRadixPrefix) {
                        read();

                        if (!isDigit(ch, radix.base)) {
                            error("%s numbers must contain at least one %s digit", radix, radix.toString().toLowerCase());
                        }
                    }
                } else {
                    builder.append(read());
                }

                while (true) {
                    if (ch == NUMBER_DIGIT_SEPARATOR) {
                        read();

                        if (!isDigit(ch, radix.base)) {
                            error("Digit separator must be followed by a digit");
                        }

                        continue;
                    }

                    if (radix == Radix.Decimal && ch == NUMBER_DECIMAL_SEPARATOR) {
                        if (seenDecimalSeparator) {
                            break;
                        }

                        if (lookahead < '0' || lookahead > '9') {
                            break;
                        }

                        seenDecimalSeparator = true;

                        read();
                        builder.append('.');

                        continue;
                    }

                    if (radix == Radix.Decimal && (ch == 'e' || ch == 'E')) {
                        if (seenScientificNotation) {
                            break;
                        }

                        seenScientificNotation = true;

                        read();
                        builder.append('e');

                        if (ch == '+' || ch == '-') {
                            builder.append(read());
                        }

                        if (ch <= '0' || ch > '9') {
                            error("Digit is required after exponent notation");
                        }

                        continue;
                    }

                    if (isDigit(ch, radix.base)) {
                        builder.append(read());
                        continue;
                    } else if (isDigit(ch, 16)) {
                        error("Number digits must be %s but found digit %c", radix.toString().toLowerCase(), ch);
                    }

                    break;
                }

                if (seenDecimalSeparator || seenScientificNotation) {
                    return make(TokenKind.Number, start, tell(), Float.parseFloat(builder.toString()));
                } else {
                    return make(TokenKind.Number, start, tell(), Integer.parseInt(builder.toString(), radix.base));
                }
            }

            if (ch == CHAR_EOF) {
                return make(TokenKind.End, start, start, null);
            }

            char next = read();

            switch (next) {
                case '(':
                    return make(TokenKind.ParenL);
                case ')':
                    return make(TokenKind.ParenR);
                case '{':
                    return make(TokenKind.BraceL);
                case '}':
                    return make(TokenKind.BraceR);
                case '[':
                    return make(TokenKind.BracketL);
                case ']':
                    return make(TokenKind.BracketR);
                case ';':
                    return make(TokenKind.Semicolon);
                case ':':
                    return make(TokenKind.Colon);
                case ',':
                    return make(TokenKind.Comma);
                case '.':
                    return consume('.') ? consume('.') ? make(TokenKind.Ellipsis) : consume('=') ? make(TokenKind.RangeInc) : make(TokenKind.Range) : make(TokenKind.Dot);
                case '+':
                    return consume('=') ? make(TokenKind.AddAssign) : make(TokenKind.Add);
                case '-':
                    return consume('=') ? make(TokenKind.SubAssign) : make(TokenKind.Sub);
                case '*':
                    return consume('=') ? make(TokenKind.MulAssign) : make(TokenKind.Mul);
                case '/':
                    return consume('=') ? make(TokenKind.DivAssign) : make(TokenKind.Div);
                case '<':
                    return consume('=') ? make(TokenKind.LessEq) : make(TokenKind.Less);
                case '>':
                    return consume('=') ? make(TokenKind.GreaterEq) : make(TokenKind.Greater);
                case '=':
                    return consume('=') ? make(TokenKind.Eq) : make(TokenKind.Assign);
                case '?':
                    return make(TokenKind.Question);
                case '!':
                    if (consume('=')) {
                        return make(TokenKind.NotEq);
                    }
                default:
                    error("Unknown symbol: '%c' (%#04x)", next, (int) next);
            }
        }
    }

    public String getBuffer() {
        return buffer.toString();
    }

    private Token make(TokenKind kind) {
        return new Token(kind, start.until(tell()));
    }

    private Token make(TokenKind kind, Object value) {
        return new Token(kind, start.until(tell()), value);
    }

    private Token make(TokenKind kind, Region.Span start, Region.Span end, Object value) {
        return new Token(kind, start.until(end), value);
    }

    private void error(String message, Object... args) throws ScriptException {
        error(tell(), message, args);
    }

    private void error(Region.Span span, String message, Object... args) throws ScriptException {
        error(span.until(span), message, args);
    }

    private void error(Region region, String message, Object... args) throws ScriptException {
        throw new ScriptException(String.format(message, args), region);
    }

    private Region.Span tell() {
        return new Region.Span(line, column, offset);
    }

    private boolean consume(char expect) throws ScriptException {
        if (ch == expect) {
            read();
            return true;
        }

        return false;
    }

    private char read() throws ScriptException {
        char previous = ch;

        buffer.append(lookahead);

        try {
            ch = lookahead;
            lookahead = (char) reader.read();
        } catch (IOException e) {
            throw new ScriptException("Internal exception", e, tell().until(tell()));
        }

        if (previous == '\n') {
            line += 1;
            column = 1;
        } else {
            column += 1;
        }

        offset += 1;

        return previous;
    }

    private void fill() throws ScriptException {
        try {
            ch = (char) reader.read();
            lookahead = (char) reader.read();
        } catch (IOException e) {
            throw new ScriptException("Internal exception", e, tell().until(tell()));
        }

        buffer.append(ch);
    }

    private int readEscape(int minLength, int maxLength, Radix radix) throws ScriptException {
        int index = 0;
        int value = 0;
        if (read() != '{') {
            error("Expected an opening brace");
        }
        for (; index < maxLength; index++) {
            if (isDigit(ch, radix.base)) {
                value = value * radix.base + toDigit(ch, radix.base);
                read();
            } else {
                break;
            }
        }
        if (read() != '}') {
            error("Expected a closing brace");
        }
        if (index < minLength) {
            error(tell().offsetBy(-index - 1).until(tell().offsetBy(-1)), "Expected at least %s %s digits", minLength, radix.toString().toLowerCase());
        }
        return value;
    }

    private static boolean isDigit(char ch, int radix) {
        int index = "0123456789abcdefABCDEF".indexOf(ch);
        return 0 <= index && (index <= 16 && index < radix || index >= 16 && index - 6 < radix);
    }

    private static byte toDigit(char ch, int radix) {
        if (ch >= '0' && ch <= '0' + Math.min(10, radix)) {
            return (byte) (ch - '0');
        }
        if (radix > 10 && ch >= 'a' && ch <= 'a' + radix - 10) {
            return (byte) (ch - 'a' + 10);
        }
        if (radix > 10 && ch >= 'A' && ch <= 'A' + radix - 10) {
            return (byte) (ch - 'A' + 10);
        }
        throw new IllegalArgumentException("Cannot convert character '" + ch + "' to digit with radix " + radix);
    }

    private enum Mode {
        Normal,
        Inside
    }

    private enum Radix {
        Binary(2),
        Octal(8),
        Decimal(10),
        Hexadecimal(16);

        private final int base;

        Radix(int base) {
            this.base = base;
        }
    }
}
