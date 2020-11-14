package com.shade.lang.test;

import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.Tokenizer;
import com.shade.lang.parser.token.Token;
import com.shade.lang.parser.token.TokenKind;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

public class TokenizerTest {
    @Test
    public void testString() throws ScriptException {
        Tokenizer tokenizer = make("'a b c'");
        expect(TokenKind.String, "a b c", tokenizer.next());
        expect(TokenKind.End, null, tokenizer.next());
    }

    @Test
    public void testStringInterpolation() throws ScriptException {
        Tokenizer tokenizer = make("'a\\{'b\\{'c'}'}d'");
        expect(TokenKind.StringPart, "a", tokenizer.next());
        expect(TokenKind.StringPart, "b", tokenizer.next());
        expect(TokenKind.String, "c", tokenizer.next());
        expect(TokenKind.String, "", tokenizer.next());
        expect(TokenKind.String, "d", tokenizer.next());
        expect(TokenKind.End, null, tokenizer.next());
    }

    @Test
    public void testStringEscape() throws ScriptException {
        Tokenizer tokenizer = make("'\\b\\t\\n\\r\\\"\\'\\\\'");
        expect(TokenKind.String, "\b\t\n\r\"'\\", tokenizer.next());
        expect(TokenKind.End, null, tokenizer.next());
    }

    @Test
    public void testStringEscapeHexadecimal() throws ScriptException {
        Tokenizer tokenizer = make("'\\x{00}\\x{7F}'");
        expect(TokenKind.String, "\000\177", tokenizer.next());
        expect(TokenKind.End, null, tokenizer.next());
        Assert.assertThrows(ScriptException.class, () -> make("\\x{ff}").next());
    }

    @Test
    public void testStringEscapeOctal() throws ScriptException {
        Tokenizer tokenizer = make("'\\o{000}\\o{177}'");
        expect(TokenKind.String, "\000\177", tokenizer.next());
        expect(TokenKind.End, null, tokenizer.next());
        Assert.assertThrows(ScriptException.class, () -> make("\\o{377}").next());
    }

    @Test
    public void testStringEscapeUnicode() throws ScriptException {
        Tokenizer tokenizer = make("'\\u{0024}\\u{00A2}\\u{0939}\\u{20ac}\\u{D55c}\\u{10348}'");
        expect(TokenKind.String, "\u0024\u00A2\u0939\u20ac\ud55c\ud800\udf48", tokenizer.next());
        expect(TokenKind.End, null, tokenizer.next());
        Assert.assertThrows(ScriptException.class, () -> make("\\u{110000}").next());
        Assert.assertThrows(ScriptException.class, () -> make("\\u{d800}").next());
        Assert.assertThrows(ScriptException.class, () -> make("\\u{dfff}").next());
    }

    @Test
    public void testIntegerNumber() throws ScriptException {
        Tokenizer tokenizer = make("100 1_0_0");
        expect(TokenKind.Number, 100, tokenizer.next());
        expect(TokenKind.Number, 100, tokenizer.next());
        Assert.assertThrows(ScriptException.class, () -> make("100_").next());
    }

    @Test
    public void testFloatNumber() throws ScriptException {
        Tokenizer tokenizer = make("0.1 100.00 1e3 1e+3 1e-3 1.5e2 1.0. 123.. 0..10");
        expect(TokenKind.Number, 0.1f, tokenizer.next());
        expect(TokenKind.Number, 100.0f, tokenizer.next());
        expect(TokenKind.Number, 1.0e3f, tokenizer.next());
        expect(TokenKind.Number, 1.0e+3f, tokenizer.next());
        expect(TokenKind.Number, 1.0e-3f, tokenizer.next());
        expect(TokenKind.Number, 1.5e2f, tokenizer.next());
        expect(TokenKind.Number, 1.0f, tokenizer.next());
        expect(TokenKind.Dot, null, tokenizer.next());
        expect(TokenKind.Number, 123, tokenizer.next());
        expect(TokenKind.Range, null, tokenizer.next());
        expect(TokenKind.Number, 0, tokenizer.next());
        expect(TokenKind.Range, null, tokenizer.next());
        expect(TokenKind.Number, 10, tokenizer.next());
        Assert.assertThrows(ScriptException.class, () -> make("1.0e").next());
        Assert.assertThrows(ScriptException.class, () -> make("1.0e+").next());
    }

    private static void expect(TokenKind kind, Object value, Token actual) {
        Assert.assertEquals(kind, actual.getKind());
        Assert.assertEquals(value, actual.getValue());
    }

    private static Tokenizer make(String source) throws ScriptException {
        return new Tokenizer(new StringReader(source));
    }
}
