package com.shade.lang.parser;

import com.shade.lang.parser.node.Node;
import com.shade.lang.parser.node.expr.*;
import com.shade.lang.parser.node.stmt.*;
import com.shade.lang.parser.token.Region;
import com.shade.lang.parser.token.Token;
import com.shade.lang.parser.token.TokenKind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Tokenizer tokenizer;
    private Token token;

    public Parser(Tokenizer tokenizer) throws ParseException, IOException {
        this.tokenizer = tokenizer;
        this.advance();
    }

    public Node parse(String source, Parser.Mode mode) throws ParseException, IOException {
        switch (mode) {
            case Unit:
                return parseUnit(source);
            case Statement:
                return declarativeStatement();
            case Expression:
                return expression();
        }

        return null;
    }

    public UnitStatement parseUnit(String name) throws ParseException, IOException {
        List<Statement> statements = new ArrayList<>();

        while (true) {
            Region start = token.getRegion();
            Token token = expect(TokenKind.Import, TokenKind.Def, TokenKind.End);

            switch (token.getKind()) {
                case Def:
                    statements.add(declareFunctionStatement());
                    break;
                case End:
                    return new UnitStatement(name, statements, start.until(token.getRegion()));
            }
        }
    }

    private ImportStatement declareImportStatement(boolean matchOpening, Region start) throws ParseException, IOException {
        if (start == null) {
            start = token.getRegion();
        }
        if (matchOpening) {
            expect(TokenKind.Import);
        }
        Token name = expect(TokenKind.Symbol, TokenKind.String);
        expect(TokenKind.Semicolon);
        return new ImportStatement(name.getValue(), name.getKind() == TokenKind.String, start.until(token.getRegion()));
    }

    public Statement declarativeStatement() throws ParseException, IOException {
        Region start = token.getRegion();
        switch (token.getKind()) {
            case Import:
                advance();
                return declareImportStatement(false, start);
            case Let:
                advance();
                return declareVariableStatement();
            case If:
                advance();
                return branchStatement(false);
            case Return:
                advance();
                return returnStatement(false);
            case BraceL:
                advance();
                return blockStatement(false);
        }

        Expression expression = expression();

        start = token.getRegion();

        if (consume(TokenKind.Assign) != null) {
            Expression value = expression();
            expect(TokenKind.Semicolon);

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

        expect(TokenKind.Semicolon);

        return new ExpressionStatement(expression, start.until(token.getRegion()));
    }

    private AssignGlobalStatement assignGlobalStatement() throws ParseException, IOException {
        Region start = token.getRegion();
        String name = expect(TokenKind.Symbol).getValue();
        expect(TokenKind.Assign);
        Expression expression = expression();
        expect(TokenKind.Semicolon);
        return new AssignGlobalStatement(name, expression, start.until(token.getRegion()));
    }

    private ReturnStatement returnStatement(boolean matchOpening) throws ParseException, IOException {
        Region start = token.getRegion();
        if (matchOpening) {
            expect(TokenKind.Return);
        }
        Expression value = expression();
        expect(TokenKind.Semicolon);
        return new ReturnStatement(value, start.until(token.getRegion()));
    }

    private BlockStatement blockStatement(boolean matchOpening) throws ParseException, IOException {
        Region start = token.getRegion();
        if (matchOpening) {
            expect(TokenKind.BraceL);
        }
        List<Statement> statements = new ArrayList<>();
        while (!matches(TokenKind.BraceR, TokenKind.End)) {
            statements.add(declarativeStatement());
        }
        expect(TokenKind.BraceR);
        return new BlockStatement(statements, start.until(token.getRegion()));
    }

    private BranchStatement branchStatement(boolean matchOpening) throws ParseException, IOException {
        Region start = token.getRegion();
        if (matchOpening) {
            expect(TokenKind.If);
        }
        Expression condition = expression();
        Statement pass = blockStatement(true);
        Statement fail = null;
        if (consume(TokenKind.Else) != null) {
            if (matches(TokenKind.If)) {
                fail = branchStatement(true);
            } else {
                fail = blockStatement(true);
            }
        }
        return new BranchStatement(condition, pass, fail, start.until(token.getRegion()));
    }

    private DeclareVariableStatement declareVariableStatement() throws ParseException, IOException {
        Region start = token.getRegion();
        Token name = expect(TokenKind.Symbol);
        expect(TokenKind.Assign);
        Expression value = expression();
        expect(TokenKind.Semicolon);
        return new DeclareVariableStatement(name.getValue(), value, start.until(token.getRegion()));
    }

    private DeclareFunctionStatement declareFunctionStatement() throws ParseException, IOException {
        Region start = token.getRegion();
        Token name = expect(TokenKind.Symbol);
        List<String> args = new ArrayList<>();
        expect(TokenKind.ParenL);
        if (!matches(TokenKind.ParenR, TokenKind.End)) {
            args.add(expect(TokenKind.Symbol).getValue());
            while (consume(TokenKind.Comma) != null) {
                args.add(expect(TokenKind.Symbol).getValue());
            }
        }
        expect(TokenKind.ParenR);
        return new DeclareFunctionStatement(name.getValue(), args, blockStatement(true), start.until(token.getRegion()));
    }

    public Expression expression() throws ParseException, IOException {
        return additiveExpression();
    }

    private Expression additiveExpression() throws ParseException, IOException {
        Region start = token.getRegion();
        Expression left = multiplicativeExpression();
        Token operator = consume(TokenKind.Add, TokenKind.Sub);
        if (operator != null) {
            return new BinaryExpression(left, additiveExpression(), operator.getKind(), start.until(token.getRegion()));
        }
        return left;
    }

    private Expression multiplicativeExpression() throws ParseException, IOException {
        Region start = token.getRegion();
        Expression left = comparisonExpression();
        Token operator = consume(TokenKind.Mul, TokenKind.Div);
        if (operator != null) {
            return new BinaryExpression(left, multiplicativeExpression(), operator.getKind(), start.until(token.getRegion()));
        }
        return left;
    }

    private Expression comparisonExpression() throws ParseException, IOException {
        Region start = token.getRegion();
        Expression left = unaryExpression();
        Token operator = consume(TokenKind.And, TokenKind.Or);
        if (operator != null) {
            return new BinaryExpression(left, comparisonExpression(), operator.getKind(), start.until(token.getRegion()));
        }
        return left;
    }

    private Expression unaryExpression() throws ParseException, IOException {
        Region start = token.getRegion();
        Token operator = consume(TokenKind.Not, TokenKind.Add, TokenKind.Sub);
        if (operator != null) {
            return new UnaryExpression(primaryExpression(), operator.getKind(), start.until(token.getRegion()));
        }
        return primaryExpression();
    }

    private Expression primaryExpression() throws ParseException, IOException {
        Region start = token.getRegion();
        Token token = expect(TokenKind.ParenL, TokenKind.Symbol, TokenKind.String, TokenKind.Number);

        if (token.getKind() == TokenKind.ParenL) {
            Expression expr = expression();
            expect(TokenKind.ParenR);
            return expr;
        }

        if (token.getKind() == TokenKind.Symbol) {
            Expression expression = new LoadGlobalExpression(token.getValue(), start.until(token.getRegion()));

            while (matches(TokenKind.Dot, TokenKind.ParenL)) {
                while (consume(TokenKind.Dot) != null) {
                    Token name = expect(TokenKind.Symbol);
                    expression = new LoadAttributeExpression(expression, name.getValue(), start.until(token.getRegion()));
                }

                if (consume(TokenKind.ParenL) != null) {
                    List<Expression> arguments = new ArrayList<>();
                    if (!matches(TokenKind.ParenR, TokenKind.End)) {
                        arguments.add(expression());
                        while (consume(TokenKind.Comma) != null) {
                            arguments.add(expression());
                        }
                    }
                    expect(TokenKind.ParenR);
                    expression = new CallExpression(expression, arguments, start.until(token.getRegion()));
                }
            }

            return expression;
        }

        if (token.getKind() == TokenKind.String) {
            return new LoadConstantExpression<>(token.getValue(), start.until(token.getRegion()));
        }

        if (token.getKind() == TokenKind.Number) {
            try {
                return new LoadConstantExpression<>(Integer.parseInt(token.getValue()), start.until(token.getRegion()));
            } catch (NumberFormatException e) {
                throw new ParseException("Invalid number literal", token.getRegion());
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

    private Token consume(TokenKind... kinds) throws ParseException, IOException {
        for (TokenKind kind : kinds) {
            if (token.getKind() == kind) {
                return advance();
            }
        }

        return null;
    }

    private Token expect(TokenKind... kinds) throws ParseException, IOException {
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

        if (token.getKind().isDisplay()) {
            found.append(" '").append(token.getValue()).append("'");
        }

        throw new ParseException("Expected " + expected + " but found " + found, token.getRegion());
    }

    private Token advance() throws ParseException, IOException {
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
