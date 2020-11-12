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

import java.lang.String;
import java.util.*;

import static com.shade.lang.parser.token.TokenKind.*;

public class Parser {
    private final Tokenizer tokenizer;
    private Token token;

    public Parser(Tokenizer tokenizer) throws ScriptException {
        this.tokenizer = tokenizer;
        this.advance();
    }

    public Node parse(String source, Parser.Mode mode) throws ScriptException {
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

    private UnitStatement parseUnit(String name) throws ScriptException {
        Region start = token.getRegion();

        List<Statement> statements = new ArrayList<>();

        while (true) {
            Token token = expect(Import, Class, Def, End);

            switch (token.getKind()) {
                case Import:
                    statements.add(parseImportStatement(token.getRegion(), true));
                    break;
                case Class:
                    statements.add(parseClassDeclareStatement(token.getRegion()));
                    break;
                case Def:
                    statements.add(parseFunctionDeclareStatement(token.getRegion()));
                    break;
                case End:
                    return new UnitStatement(name, statements, start.until(token.getRegion()));
            }
        }
    }

    private ImportStatement parseImportStatement(Region start, boolean global) throws ScriptException {
        String name = expect(Symbol).getStringValue();
        String alias = null;
        if (consume(Assign) != null) {
            alias = expect(Symbol).getStringValue();
        }
        Token end = expect(Semicolon);
        return new ImportStatement(name, alias, global, start.until(end.getRegion()));
    }

    private Statement parseStatement() throws ScriptException {
        switch (token.getKind()) {
            case Import:
                return parseImportStatement(advance().getRegion(), false);
            case Assert:
                return parseAssertStatement();
            case Let:
                return parseVariableDeclareStatement();
            case If:
                return parseBranchStatement();
            case Return:
                return parseReturnStatement();
            case Try:
                return parseTryStatement();
            case Loop:
                return parseLoopStatement();
            case For:
                return parseRangeStatement();
            case Continue:
                return parseContinueStatement();
            case Super:
                return parseSuperStatement();
            case Break:
                return parseBreakStatement();
            case BraceL:
                return parseBlockStatement();
        }

        Expression expression = parseExpression();

        if (token.getKind().hasFlag(TokenFlag.ASSIGNMENT)) {
            TokenKind operator = advance().getKind();
            Expression value = parseExpression();
            Token end = expect(Semicolon);

            if (operator != Assign) {
                value = new BinaryExpression(expression, value, operator, expression.getRegion().until(value.getRegion()));
            }

            if (expression instanceof LoadAttributeExpression) {
                LoadAttributeExpression attribute = (LoadAttributeExpression) expression;
                return new AssignAttributeStatement(attribute.getOwner(), attribute.getName(), value, expression.getRegion().until(end.getRegion()));
            } else if (expression instanceof LoadSymbolExpression) {
                LoadSymbolExpression attribute = (LoadSymbolExpression) expression;
                return new AssignSymbolStatement(attribute.getName(), value, expression.getRegion().until(end.getRegion()));
            } else if (expression instanceof LoadConstantExpression<?>) {
                throw new ScriptException("Left hand side expression must not be a constant value", expression.getRegion());
            } else {
                throw new RuntimeException("Not implemented");
            }
        }

        Token end = expect(Semicolon);

        return new ExpressionStatement(expression, expression.getRegion().until(end.getRegion()));
    }

    private LoopStatement parseLoopStatement() throws ScriptException {
        Region start = expect(Loop).getRegion();
        Token mode = consume(While);
        Expression condition = null;
        if (mode != null && !matches(BraceL)) {
            condition = parseExpression();
        }
        BlockStatement body = parseBlockStatement();
        return new LoopStatement(condition, body, start.until(body.getRegion()));
    }

    private BlockStatement parseRangeStatement() throws ScriptException {
        Region start = expect(For).getRegion();
        Token variable = expect(Symbol);
        expect(In);
        int rangeBegin = expect(Number).getIntegerValue();
        boolean rangeInclusive = expect(Range, RangeInc).getKind() == RangeInc;
        int rangeEnd = expect(Number).getIntegerValue();
        boolean rangeDescending = rangeEnd < rangeBegin;
        BlockStatement body = parseBlockStatement();
        Region region = start.until(body.getRegion());

        return new BlockStatement(Arrays.asList(
            new DeclareVariableStatement(
                variable.getStringValue(),
                new LoadConstantExpression<>(rangeBegin, variable.getRegion()),
                variable.getRegion()
            ),
            new LoopStatement(
                new BinaryExpression(
                    new LoadSymbolExpression(variable.getStringValue(), region),
                    new LoadConstantExpression<>(rangeEnd, region),
                    rangeInclusive ? rangeDescending ? GreaterEq : LessEq : rangeDescending ? Greater : Less,
                    region
                ),
                new BlockStatement(
                    Arrays.asList(
                        body,
                        new AssignSymbolStatement(variable.getStringValue(),
                            new BinaryExpression(
                                new LoadSymbolExpression(variable.getStringValue(), region),
                                new LoadConstantExpression<>(1, region),
                                rangeDescending ? Sub : Add,
                                region
                            ),
                            region
                        )
                    ),
                    region
                ),
                region
            )
        ), region);
    }

    private ContinueStatement parseContinueStatement() throws ScriptException {
        Region start = expect(Continue).getRegion();
        Region end = expect(Semicolon).getRegion();
        return new ContinueStatement(start.until(end));
    }

    private BreakStatement parseBreakStatement() throws ScriptException {
        Region start = expect(Break).getRegion();
        Region end = expect(Semicolon).getRegion();
        return new BreakStatement(start.until(end));
    }

    private SuperStatement parseSuperStatement() throws ScriptException {
        Region start = expect(Super).getRegion();
        String name = null;
        if (consume(Colon) != null) {
            name = expect(Symbol).getStringValue();
        }
        List<Expression> arguments = new ArrayList<>();
        list(ParenL, ParenR, Comma, arguments, this::parseExpression);
        Region end = expect(Semicolon).getRegion();
        return new SuperStatement(name, arguments, start.until(end));
    }

    private DeclareFunctionStatement parseConstructorDeclareStatement(Region start) throws ScriptException {
        List<String> arguments = new ArrayList<>();
        list(ParenL, ParenR, Comma, arguments, () -> expect(Symbol).getStringValue());
        List<Statement> statements = new ArrayList<>();
        Region region = list(BraceL, BraceR, null, statements, () -> {
            if (matches(Super) && !statements.isEmpty() && !(statements.get(statements.size() - 1) instanceof SuperStatement)) {
                throw new ScriptException("Mixing 'super' and regular statements is not allowed", token.getRegion());
            }
            if (matches(Return)) {
                throw new ScriptException("Cannot return from constructor", token.getRegion());
            }
            return parseStatement();
        });
        BlockStatement body = new BlockStatement(statements, region);
        return new DeclareFunctionStatement("<init>", arguments, body, start.until(body.getRegion()));
    }

    private AssertStatement parseAssertStatement() throws ScriptException {
        Region start = expect(Assert).getRegion();
        Expression condition = parseExpression();
        String message = null;
        if (consume(Comma) != null) {
            message = expect(String).getStringValue();
        }
        Token end = expect(Semicolon);
        String source = condition.getRegion().of(tokenizer.getBuffer());
        return new AssertStatement(condition, source, message, start.until(end.getRegion()));
    }

    private TryStatement parseTryStatement() throws ScriptException {
        Region start = expect(Try).getRegion();
        BlockStatement body = parseBlockStatement();
        expect(Recover);
        Token name = consume(Symbol);
        BlockStatement recover = parseBlockStatement();
        return new TryStatement(body, recover, name == null ? null : name.getStringValue(), start.until(recover.getRegion()));
    }

    private ReturnStatement parseReturnStatement() throws ScriptException {
        Region start = expect(Return).getRegion();
        Expression value = parseExpression();
        Token end = expect(Semicolon);
        return new ReturnStatement(value, start.until(end.getRegion()));
    }

    private BlockStatement parseBlockStatement() throws ScriptException {
        List<Statement> statements = new ArrayList<>();
        Region region = list(BraceL, BraceR, null, statements, this::parseStatement);
        return new BlockStatement(statements, region);
    }

    private BranchStatement parseBranchStatement() throws ScriptException {
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

    private DeclareVariableStatement parseVariableDeclareStatement() throws ScriptException {
        Region start = expect(Let).getRegion();
        Token name = expect(Symbol);
        expect(Assign);
        Expression value = parseExpression();
        expect(Semicolon);
        return new DeclareVariableStatement(name.getStringValue(), value, start.until(token.getRegion()));
    }

    private DeclareFunctionStatement parseFunctionDeclareStatement(Region start) throws ScriptException {
        String name = expect(Symbol).getStringValue();
        List<String> arguments = new ArrayList<>();
        list(ParenL, ParenR, Comma, arguments, () -> expect(Symbol).getStringValue());
        BlockStatement body = parseBlockStatement();
        return new DeclareFunctionStatement(name, arguments, body, start.until(body.getRegion()));
    }

    private DeclareClassStatement parseClassDeclareStatement(Region start) throws ScriptException {
        String name = expect(Symbol).getStringValue();

        List<String> bases = new ArrayList<>();
        if (consume(Colon) != null) {
            list(new TokenKind[]{Symbol}, Comma, bases, () -> expect(Symbol).getStringValue());
        }

        List<Statement> members = new ArrayList<>();
        Region region = list(BraceL, BraceR, null, members, () -> {
            Token token = expect(Def, Constructor);
            switch (token.getKind()) {
                case Def:
                    return parseFunctionDeclareStatement(token.getRegion());
                case Constructor:
                    return parseConstructorDeclareStatement(token.getRegion());
                default:
                    return null;
            }
        });

        return new DeclareClassStatement(name, bases, members, start.until(region));
    }

    public Expression parseExpression() throws ScriptException {
        return parseExpression(parseLambdaExpression(), 0);
    }

    private Expression parseExpression(Expression lhs, int minimumPrecedence) throws ScriptException {
        TokenKind lookahead = token.getKind();

        while (lookahead.hasFlag(TokenFlag.BINARY) && lookahead.getPrecedence() >= minimumPrecedence) {
            TokenKind operator = advance().getKind();
            Expression rhs = parseLambdaExpression();
            lookahead = token.getKind();

            while (lookahead.hasFlag(TokenFlag.BINARY) && (lookahead.getPrecedence() > operator.getPrecedence()
                || lookahead.hasFlag(TokenFlag.RIGHT_ASSOCIATIVE) && lookahead.getPrecedence() >= operator.getPrecedence())) {
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

    private Expression parseLambdaExpression() throws ScriptException {
        Token start = consume(Def);

        if (start == null) {
            return parseUnaryExpression();
        }

        List<String> arguments = new ArrayList<>();
        list(ParenL, ParenR, Comma, arguments, () -> expect(Symbol).getStringValue());

        List<String> boundArguments = new ArrayList<>();
        if (consume(Use) != null) {
            list(ParenL, ParenR, Comma, boundArguments, () -> expect(Symbol).getStringValue());
        }

        BlockStatement body = parseBlockStatement();
        DeclareFunctionStatement function = new DeclareFunctionStatement(
            "<lambda:" + UUID.randomUUID() + ">",
            arguments,
            boundArguments,
            body,
            start.getRegion().until(body.getRegion())
        );
        return new LambdaExpression(function, function.getRegion());
    }

    private Expression parseUnaryExpression() throws ScriptException {
        Region start = token.getRegion();
        Token operator = consume(Not, Add, Sub);
        if (operator != null) {
            Expression rhs = parsePrimaryExpression();
            return new UnaryExpression(rhs, operator.getKind(), start.until(rhs.getRegion()));
        }
        return parsePrimaryExpression();
    }

    private Expression parsePrimaryExpression() throws ScriptException {
        Token token = expect(Symbol, Number, True, False, String, StringPart, New, ParenL);
        Region start = token.getRegion();

        if (token.getKind() == Symbol) {
            return parsePrimaryExpression(new LoadSymbolExpression(token.getStringValue(), start));
        }

        if (token.getKind() == Number) {
            return new LoadConstantExpression<>(token.getIntegerValue(), start);
        }

        if (token.getKind() == True) {
            return new LoadConstantExpression<>(true, start);
        }

        if (token.getKind() == False) {
            return new LoadConstantExpression<>(false, start);
        }

        if (token.getKind() == String) {
            return new LoadConstantExpression<>(token.getStringValue(), start);
        }

        if (token.getKind() == StringPart) {
            Expression lhs = new LoadConstantExpression<>(token.getStringValue(), start);
            Expression rhs = parseExpression();
            Expression string = new BinaryExpression(lhs, rhs, Add, lhs.getRegion().until(rhs.getRegion()));

            while (true) {
                token = expect(String, StringPart);

                if (!token.getStringValue().isEmpty()) {
                    rhs = new LoadConstantExpression<>(token.getStringValue(), start.until(token.getRegion()));
                    string = new BinaryExpression(string, rhs, Add, string.getRegion().until(rhs.getRegion()));
                }

                if (token.getKind() == String) {
                    break;
                }

                rhs = parseExpression();
                string = new BinaryExpression(string, rhs, Add, string.getRegion().until(rhs.getRegion()));
            }

            return string;
        }

        if (token.getKind() == New) {
            Token name = expect(Symbol);
            List<Expression> arguments = new ArrayList<>();
            Region region = list(ParenL, ParenR, Comma, arguments, this::parseExpression);
            return new NewExpression(name.getStringValue(), arguments, token.getRegion().until(region));
        }

        if (token.getKind() == ParenL) {
            Expression expression = parseExpression();
            Region end = expect(ParenR).getRegion();
            return parsePrimaryExpression(new CompoundExpression(expression, start.until(end)));
        }

        throw new AssertionError("Unreachable");
    }

    private Expression parsePrimaryExpression(Expression lhs) throws ScriptException {
        if (!matches(ParenL, Dot)) {
            return lhs;
        }

        if (consume(Dot) != null) {
            Token name = expect(Symbol);
            return parsePrimaryExpression(new LoadAttributeExpression(lhs, name.getStringValue(), lhs.getRegion().until(name.getRegion())));
        }

        List<Expression> arguments = new ArrayList<>();
        Region region = list(ParenL, ParenR, Comma, arguments, this::parseExpression);
        return parsePrimaryExpression(new CallExpression(lhs, arguments, lhs.getRegion().until(region)));
    }

    private <T> Region list(TokenKind opening, TokenKind closing, TokenKind separator, Collection<T> output, Supplier<T> supplier) throws ScriptException {
        Region begin = expect(opening).getRegion();

        if (!matches(closing)) {
            do {
                output.add(supplier.get());
            } while (separator != null && consume(separator) != null || !matches(closing, End));
        }

        Region end = expect(closing).getRegion();

        return begin.until(end);
    }

    private <T> Region list(TokenKind[] starting, TokenKind separator, Collection<T> output, Supplier<T> supplier) throws ScriptException {
        Region begin = token.getRegion();
        if (matches(starting)) {
            do {
                output.add(supplier.get());
            } while (consume(separator) != null);
        }
        Region end = token.getRegion();
        return begin.until(end);
    }

    private boolean matches(TokenKind... kinds) {
        for (TokenKind kind : kinds) {
            if (token.getKind() == kind) {
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenKind... kinds) throws ScriptException {
        for (TokenKind kind : kinds) {
            if (token.getKind() == kind) {
                return advance();
            }
        }

        return null;
    }

    private Token expect(TokenKind... kinds) throws ScriptException {
        Token consumed = consume(kinds);

        if (consumed != null) {
            return consumed;
        }

        StringBuilder message = new StringBuilder();
        message.append("Expected ");

        for (int index = 0; index < kinds.length; index++) {
            message.append(kinds[index].getQuotedName());
            if (index < kinds.length - 2) {
                message.append(", ");
            } else if (index < kinds.length - 1) {
                message.append(" or ");
            }
        }

        message.append(" but found ");

        message.append(token.getKind().getQuotedName());
        if (token.getKind().hasFlag(TokenFlag.DISPLAY)) {
            message.append(" '").append(token.getStringValue()).append("'");
        }

        throw new ScriptException(message.toString(), token.getRegion());
    }

    private Token advance() throws ScriptException {
        Token token = this.token;
        this.token = tokenizer.next();
        return token;
    }

    public enum Mode {
        Unit,
        Statement,
        Expression
    }

    private interface Supplier<T> {
        T get() throws ScriptException;
    }
}
