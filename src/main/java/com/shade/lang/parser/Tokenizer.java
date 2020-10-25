package com.shade.lang.parser;

import com.shade.lang.parser.token.Region;
import com.shade.lang.parser.token.Token;
import com.shade.lang.parser.token.TokenKind;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

public class Tokenizer {
    private static final char CHAR_EOF = '\uFFFF';
    private static final char CHAR_NEWLINE = '\n';

    private final Reader reader;
    private final StringBuilder buffer;
    private final Stack<Mode> modeStack;
    private Region.Span start;
    private int line;
    private int column;
    private int offset;
    private char ch;

    public Tokenizer(Reader reader) throws IOException {
        this.reader = reader;
        this.buffer = new StringBuilder();
        this.modeStack = new Stack<>();
        this.line = 1;
        this.column = 1;
        this.read();
    }

    public Token next() throws ScriptException, IOException {
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

                switch (builder.toString()) {
                    case "let":
                        return make(TokenKind.Let);
                    case "def":
                        return make(TokenKind.Def);
                    case "if":
                        return make(TokenKind.If);
                    case "else":
                        return make(TokenKind.Else);
                    case "return":
                        return make(TokenKind.Return);
                    case "and":
                        return make(TokenKind.And);
                    case "or":
                        return make(TokenKind.Or);
                    case "not":
                        return make(TokenKind.Not);
                    case "import":
                        return make(TokenKind.Import);
                    case "assert":
                        return make(TokenKind.Assert);
                    case "try":
                        return make(TokenKind.Try);
                    case "recover":
                        return make(TokenKind.Recover);
                    default:
                        return make(TokenKind.Symbol, builder.toString());
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
                            case 'x':
                                next = (char) (consumeHex() * 16 + consumeHex());
                                break;
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

                while (ch >= '0' && ch <= '9') {
                    builder.append(read());
                }

                return new Token(TokenKind.Number, start.until(tell()), builder.toString());
            }

            if (ch == CHAR_EOF) {
                return new Token(TokenKind.End, start.until(start));
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
                case ';':
                    return make(TokenKind.Semicolon);
                case ',':
                    return make(TokenKind.Comma);
                case '.':
                    return make(TokenKind.Dot);
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

    private Token make(TokenKind kind, String value) {
        return new Token(kind, start.until(tell()), value);
    }

    private Token make(TokenKind kind, Region.Span start, Region.Span end, String value) {
        return new Token(kind, start.until(end), value);
    }

    private void error(String message, Object... args) throws ScriptException {
        throw new ScriptException(String.format(message, args), start.until(tell()));
    }

    private void error(Region.Span start, Region.Span end, String message, Object... args) throws ScriptException {
        throw new ScriptException(String.format(message, args), start.until(end));
    }

    private Region.Span tell() {
        return new Region.Span(line, column, offset);
    }

    private boolean consume(char expect) throws IOException {
        if (ch == expect) {
            read();
            return true;
        }

        return false;
    }

    private byte consumeHex() throws IOException, ScriptException {
        if (ch >= '0' && ch <= '9') {
            return (byte) (read() - '0');
        }

        if (ch >= 'a' && ch <= 'f') {
            return (byte) (read() - 'a' + 10);
        }

        if (ch >= 'A' && ch <= 'F') {
            return (byte) (read() - 'A' + 10);
        }

        error("Expected hexadecimal number");
        return 0;
    }

    private char read() throws IOException {
        if (ch > 0) {
            if (ch == '\n') {
                line += 1;
                column = 1;
            } else {
                column += 1;
            }

            offset += 1;
        }

        char last = ch;

        ch = (char) reader.read();

        buffer.append(ch);

        return last;
    }

    private enum Mode {
        Normal,
        Inside
    }
}
