package com.shade.lang.parser;

import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Node;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.expr.*;
import com.shade.lang.parser.node.stmt.*;
import com.shade.lang.parser.token.Region;
import com.shade.lang.parser.token.Token;
import com.shade.lang.parser.token.TokenFlag;
import com.shade.lang.parser.token.TokenKind;

import java.io.IOException;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import static com.shade.lang.parser.token.TokenKind.Number;
import static com.shade.lang.parser.token.TokenKind.String;
import static com.shade.lang.parser.token.TokenKind.*;

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
        Region start = token.getRegion();

        List<Statement> statements = new ArrayList<>();

        while (true) {
            Token token = expect(Import, Def, End);

            switch (token.getKind()) {
                case Import:
                    statements.add(parseImportStatement(token.getRegion()));
                    break;
                case Def:
                    statements.add(parseFunctionDeclareStatement(token.getRegion()));
                    break;
                case End:
                    return new UnitStatement(name, statements, start.until(token.getRegion()));
            }
        }
    }

    private ImportStatement parseImportStatement(Region start) throws ScriptException, IOException {
        Token name = expect(Symbol, String);
        String alias = null;
        if (consume(Assign) != null) {
            alias = expect(Symbol).getValue();
        }
        Token end = expect(Semicolon);
        return new ImportStatement(name.getValue(), alias, name.getKind() == String, start.until(end.getRegion()));
    }

    private Statement parseStatement() throws ScriptException, IOException {
        switch (token.getKind()) {
            case Assert:
                return parseAssertStatement();
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

        if (consume(Assign) != null) {
            Expression value = parseExpression();
            Token end = expect(Semicolon);

            if (expression instanceof LoadAttributeExpression) {
                LoadAttributeExpression attribute = (LoadAttributeExpression) expression;
                return new AssignAttributeStatement(attribute.getOwner(), attribute.getName(), value, expression.getRegion().until(end.getRegion()));
            } else if (expression instanceof LoadGlobalExpression) {
                LoadGlobalExpression attribute = (LoadGlobalExpression) expression;
                return new AssignGlobalStatement(attribute.getName(), value, expression.getRegion().until(end.getRegion()));
            } else {
                throw new RuntimeException("Not implemented");
            }
        }

        Token end = expect(Semicolon);

        return new ExpressionStatement(expression, expression.getRegion().until(end.getRegion()));
    }

    private AssertStatement parseAssertStatement() throws ScriptException, IOException {
        Region start = expect(Assert).getRegion();
        Expression condition = parseExpression();
        String message = null;
        if (consume(Comma) != null) {
            message = expect(String).getValue();
        }
        Token end = expect(Semicolon);
        String source = condition.getRegion().of(tokenizer.getBuffer());
        return new AssertStatement(condition, source, message, start.until(end.getRegion()));
    }

    private ReturnStatement parseReturnStatement() throws ScriptException, IOException {
        Region start = expect(Return).getRegion();
        Expression value = parseExpression();
        Token end = expect(Semicolon);
        return new ReturnStatement(value, start.until(end.getRegion()));
    }

    private BlockStatement parseBlockStatement() throws ScriptException, IOException {
        Region start = expect(BraceL).getRegion();
        List<Statement> statements = new ArrayList<>();
        while (!matches(BraceR, End)) {
            statements.add(parseStatement());
        }
        Token end = expect(BraceR);
        return new BlockStatement(statements, start.until(end.getRegion()));
    }

    private BranchStatement parseBranchStatement() throws ScriptException, IOException {
        Region start = expect(If).getRegion();
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
        Region end = fail == null ? pass.getRegion() : fail.getRegion();
        return new BranchStatement(condition, pass, fail, start.until(end));
    }

    private DeclareVariableStatement parseVariableDeclareStatement() throws ScriptException, IOException {
        Region start = expect(Let).getRegion();
        Token name = expect(Symbol);
        expect(Assign);
        Expression value = parseExpression();
        expect(Semicolon);
        return new DeclareVariableStatement(name.getValue(), value, start.until(token.getRegion()));
    }

    private DeclareFunctionStatement parseFunctionDeclareStatement(Region start) throws ScriptException, IOException {
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
        BlockStatement body = parseBlockStatement();
        return new DeclareFunctionStatement(name.getValue(), args, body, start.until(body.getRegion()));
    }

    public Expression parseExpression() throws ScriptException, IOException {
        return parseExpression(parseUnaryExpression(), 0);
    }

    private Expression parseExpression(Expression lhs, int minimumPrecedence) throws ScriptException, IOException {
        TokenKind lookahead = token.getKind();

        while (lookahead.hasFlag(TokenFlag.BINARY) && lookahead.getPrecedence() >= minimumPrecedence) {
            TokenKind operator = advance().getKind();
            Expression rhs = parseUnaryExpression();
            lookahead = token.getKind();

            while (lookahead.hasFlag(TokenFlag.BINARY) && lookahead.getPrecedence() > operator.getPrecedence()) {
                rhs = parseExpression(rhs, lookahead.getPrecedence());
                lookahead = token.getKind();
            }

            if (operator.hasFlag(TokenFlag.LOGICAL)) {
                lhs = new LogicalExpression(lhs, rhs, operator, lhs.getRegion().until(rhs.getRegion()));
            } else {
                lhs = new BinaryExpression(lhs, rhs, operator, lhs.getRegion().until(rhs.getRegion()));
            }
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
                    expression = new LoadAttributeExpression(expression, name.getValue(), start.until(name.getRegion()));
                }

                if (consume(ParenL) != null) {
                    List<Expression> arguments = new ArrayList<>();
                    if (!matches(ParenR, End)) {
                        arguments.add(parseExpression());
                        while (consume(Comma) != null) {
                            arguments.add(parseExpression());
                        }
                    }
                    Token end = expect(ParenR);
                    expression = new CallExpression(expression, arguments, start.until(end.getRegion()));
                }
            }

            return expression;
        }

        if (token.getKind() == String) {
            return new LoadConstantExpression<>(token.getValue(), start.until(token.getRegion()));
        }

        if (token.getKind() == Number) {
            return new LoadConstantExpression<>(Integer.parseInt(token.getValue()), start.until(token.getRegion()));
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
