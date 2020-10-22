package com.shade.lang.parser;

import com.shade.lang.parser.node.Node;
import com.shade.lang.parser.node.expr.*;
import com.shade.lang.parser.node.stmt.*;
import com.shade.lang.parser.token.Region;
import com.shade.lang.parser.token.Token;
import com.shade.lang.parser.token.TokenFlag;
import com.shade.lang.parser.token.TokenKind;
import static com.shade.lang.parser.token.TokenKind.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Tokenizer tokenizer;
    private Token token;

    public Parser(Tokenizer tokenizer) throws ScriptException, IOException {
        this.tokenizer = tokenizer;
        this.advance();
    }

    public Node parse(String source, Parser.Mode mode) throws ScriptException, IOException {
        switch (mode) {
            case Unit:
                return parseUnit(source);
            case Statement:
                return parseStatement();
            case Expression:
                return parseExpression();
        }

        return null;
    }

    private UnitStatement parseUnit(String name) throws ScriptException, IOException {
        List<Statement> statements = new ArrayList<>();

        while (true) {
            Region start = token.getRegion();
            Token token = expect(Import, Def, End);

            switch (token.getKind()) {
                case Import:
                    statements.add(parseImportStatement(start));
                    break;
                case Def:
                    statements.add(parseFunctionDeclareStatement());
                    break;
                case End:
                    return new UnitStatement(name, statements, start.until(token.getRegion()));
            }
        }
    }

    private ImportStatement parseImportStatement(Region start) throws ScriptException, IOException {
        Token name = expect(Symbol, String);
        expect(Semicolon);
        return new ImportStatement(name.getValue(), name.getKind() == String, start.until(token.getRegion()));
    }

    private Statement parseStatement() throws ScriptException, IOException {
        switch (token.getKind()) {
            case Let:
                return parseVariableDeclareStatement();
            case If:
                return parseBranchStatement();
            case Return:
                return parseReturnStatement();
            case BraceL:
                return parseBlockStatement();
        }

        Expression expression = parseExpression();

        Region start = token.getRegion();

        if (consume(Assign) != null) {
            Expression value = parseExpression();
            expect(Semicolon);

            if (expression instanceof LoadAttributeExpression) {
                LoadAttributeExpression attribute = (LoadAttributeExpression) expression;
                return new AssignAttributeStatement(attribute.getOwner(), attribute.getName(), value, start.until(token.getRegion()));
            } else if (expression instanceof LoadGlobalExpression) {
                LoadGlobalExpression attribute = (LoadGlobalExpression) expression;
                return new AssignGlobalStatement(attribute.getName(), value, start.until(token.getRegion()));
            } else {
                throw new RuntimeException("Not implemented");
            }
        }

        expect(Semicolon);

        return new ExpressionStatement(expression, start.until(token.getRegion()));
    }

    private ReturnStatement parseReturnStatement() throws ScriptException, IOException {
        Region start = token.getRegion();
        expect(Return);
        Expression value = parseExpression();
        expect(Semicolon);
        return new ReturnStatement(value, start.until(token.getRegion()));
    }

    private BlockStatement parseBlockStatement() throws ScriptException, IOException {
        Region start = token.getRegion();
        expect(BraceL);
        List<Statement> statements = new ArrayList<>();
        while (!matches(BraceR, End)) {
            statements.add(parseStatement());
        }
        expect(BraceR);
        return new BlockStatement(statements, start.until(token.getRegion()));
    }

    private BranchStatement parseBranchStatement() throws ScriptException, IOException {
        Region start = token.getRegion();
        expect(If);
        Expression condition = parseExpression();
        Statement pass = parseBlockStatement();
        Statement fail = null;
        if (consume(Else) != null) {
            if (matches(If)) {
                fail = parseBranchStatement();
            } else {
                fail = parseBlockStatement();
            }
        }
        return new BranchStatement(condition, pass, fail, start.until(token.getRegion()));
    }

    private DeclareVariableStatement parseVariableDeclareStatement() throws ScriptException, IOException {
        expect(Let);
        Region start = token.getRegion();
        Token name = expect(Symbol);
        expect(Assign);
        Expression value = parseExpression();
        expect(Semicolon);
        return new DeclareVariableStatement(name.getValue(), value, start.until(token.getRegion()));
    }

    private DeclareFunctionStatement parseFunctionDeclareStatement() throws ScriptException, IOException {
        Region start = token.getRegion();
        Token name = expect(Symbol);
        List<String> args = new ArrayList<>();
        expect(ParenL);
        if (!matches(ParenR, End)) {
            args.add(expect(Symbol).getValue());
            while (consume(Comma) != null) {
                args.add(expect(Symbol).getValue());
            }
        }
        expect(ParenR);
        return new DeclareFunctionStatement(name.getValue(), args, parseBlockStatement(), start.until(token.getRegion()));
    }

    public Expression parseExpression() throws ScriptException, IOException {
        return parseLogicalExpression();
    }

    private Expression parseLogicalExpression() throws ScriptException, IOException {
        Region start = token.getRegion();
        Expression lhs = parseRelationalExpression();
        Token operator = consume(And, Or);
        if (operator != null) {
            return new BinaryExpression(lhs, parseLogicalExpression(), operator.getKind(), start.until(token.getRegion()));
        }
        return lhs;
    }

    private Expression parseRelationalExpression() throws ScriptException, IOException {
        Region start = token.getRegion();
        Expression lhs = parseAdditiveExpression();
        Token operator = consume(Less, LessEq, Greater, GreaterEq, Eq, NotEq);
        if (operator != null) {
            return new BinaryExpression(lhs, parseRelationalExpression(), operator.getKind(), start.until(token.getRegion()));
        }
        return lhs;
    }

    private Expression parseAdditiveExpression() throws ScriptException, IOException {
        Region start = token.getRegion();
        Expression lhs = parseMultiplicativeExpression();
        Token operator = consume(Add, Sub);
        if (operator != null) {
            return new BinaryExpression(lhs, parseAdditiveExpression(), operator.getKind(), start.until(token.getRegion()));
        }
        return lhs;
    }

    private Expression parseMultiplicativeExpression() throws ScriptException, IOException {
        Region start = token.getRegion();
        Expression lhs = parseUnaryExpression();
        Token operator = consume(Mul, Div);
        if (operator != null) {
            return new BinaryExpression(lhs, parseMultiplicativeExpression(), operator.getKind(), start.until(token.getRegion()));
        }
        return lhs;
    }

    private Expression parseUnaryExpression() throws ScriptException, IOException {
        Region start = token.getRegion();
        Token operator = consume(Not, Add, Sub);
        if (operator != null) {
            return new UnaryExpression(parsePrimaryExpression(), operator.getKind(), start.until(token.getRegion()));
        }
        return parsePrimaryExpression();
    }

    private Expression parsePrimaryExpression() throws ScriptException, IOException {
        Region start = token.getRegion();
        Token token = expect(ParenL, Symbol, String, Number);

        if (token.getKind() == ParenL) {
            Expression expr = parseExpression();
            expect(ParenR);
            return expr;
        }

        if (token.getKind() == Symbol) {
            Expression expression = new LoadGlobalExpression(token.getValue(), start.until(token.getRegion()));

            while (matches(Dot, ParenL)) {
                while (consume(Dot) != null) {
                    Token name = expect(Symbol);
                    expression = new LoadAttributeExpression(expression, name.getValue(), start.until(token.getRegion()));
                }

                if (consume(ParenL) != null) {
                    List<Expression> arguments = new ArrayList<>();
                    if (!matches(ParenR, End)) {
                        arguments.add(parseExpression());
                        while (consume(Comma) != null) {
                            arguments.add(parseExpression());
                        }
                    }
                    expect(ParenR);
                    expression = new CallExpression(expression, arguments, start.until(token.getRegion()));
                }
            }

            return expression;
        }

        if (token.getKind() == String) {
            return new LoadConstantExpression<>(token.getValue(), start.until(token.getRegion()));
        }

        if (token.getKind() == Number) {
            try {
                return new LoadConstantExpression<>(Integer.parseInt(token.getValue()), start.until(token.getRegion()));
            } catch (NumberFormatException e) {
                throw new ScriptException("Invalid number literal", token.getRegion());
            }
        }

        return null;
    }

    private boolean matches(TokenKind... kinds) {
        for (TokenKind kind : kinds) {
            if (token.getKind() == kind) {
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenKind... kinds) throws ScriptException, IOException {
        for (TokenKind kind : kinds) {
            if (token.getKind() == kind) {
                return advance();
            }
        }

        return null;
    }

    private Token expect(TokenKind... kinds) throws ScriptException, IOException {
        Token consumed = consume(kinds);

        if (consumed != null) {
            return consumed;
        }

        StringBuilder expected = new StringBuilder();

        for (int index = 0; index < kinds.length; index++) {
            expected.append(kinds[index].getQuotedName());
            if (index < kinds.length - 2) {
                expected.append(", ");
            } else if (index < kinds.length - 1) {
                expected.append(" or ");
            }
        }

        StringBuilder found = new StringBuilder();
        found.append(token.getKind().getQuotedName());

        if (token.getKind().hasFlag(TokenFlag.DISPLAY)) {
            found.append(" '").append(token.getValue()).append("'");
        }

        throw new ScriptException("Expected " + expected + " but found " + found, token.getRegion());
    }

    private Token advance() throws ScriptException, IOException {
        Token token = this.token;
        this.token = tokenizer.next();
        return token;
    }

    public enum Mode {
        Unit,
        Statement,
        Expression
    }
}
