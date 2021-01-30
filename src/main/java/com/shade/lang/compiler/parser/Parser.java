package com.shade.lang.compiler.parser;

import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Node;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.expr.*;
import com.shade.lang.compiler.parser.node.stmt.*;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.compiler.parser.token.Token;
import com.shade.lang.compiler.parser.token.TokenFlag;
import com.shade.lang.compiler.parser.token.TokenKind;
import com.shade.lang.runtime.objects.value.NoneValue;
import com.shade.lang.util.annotations.NotNull;

import java.lang.String;
import java.util.*;

import static com.shade.lang.compiler.parser.token.TokenKind.Class;
import static com.shade.lang.compiler.parser.token.TokenKind.Number;
import static com.shade.lang.compiler.parser.token.TokenKind.String;
import static com.shade.lang.compiler.parser.token.TokenKind.*;

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
                LoadSymbolExpression symbol = (LoadSymbolExpression) expression;
                return new AssignSymbolStatement(symbol.getName(), value, expression.getRegion().until(end.getRegion()));
            } else if (expression instanceof LoadIndexExpression) {
                LoadIndexExpression index = (LoadIndexExpression) expression;
                return new AssignIndexStatement(index.getOwner(), index.getIndex(), value, expression.getRegion().until(end.getRegion()));
            } else if (expression instanceof LoadConstantExpression<?>) {
                throw new ScriptException("Left hand side expression must not be a constant value", expression.getRegion());
            } else {
                throw new ScriptException("Expressions is not assignable", expression.getRegion());
            }
        }

        Token end = expect(Semicolon);

        return new ExpressionStatement(expression, expression.getRegion().until(end.getRegion()));
    }

    private LoopStatement parseLoopStatement() throws ScriptException {
        Region start = expect(Loop).getRegion();
        Expression condition = null;
        if (consume(While) != null) {
            condition = parseExpression();
        }
        String name = null;
        if (consume(Colon) != null) {
            name = expect(Symbol).getStringValue();
        }
        BlockStatement body = parseBlockStatement();
        return new LoopStatement(condition, body, name, start.until(body.getRegion()));
    }

    private BlockStatement parseRangeStatement() throws ScriptException {
        Region start = expect(For).getRegion();
        Token variable = expect(Symbol);
        expect(In);
        Expression begin = parseExpression();
        boolean inclusive = expect(Range, RangeInc).getKind() == RangeInc;
        Expression end = parseExpression();
        String name = null;
        if (consume(Colon) != null) {
            name = expect(Symbol).getStringValue();
        }
        BlockStatement body = parseBlockStatement();
        Region region = start.until(body.getRegion());

        {
            // Probably we need a better solution for codegen.
            // Maybe use nested parsers with templated code?

            final String variableId = variable.getStringValue();
            final String rangeId = UUID.randomUUID().toString();
            final String iteratorId = UUID.randomUUID().toString();

            return new BlockStatement(Arrays.asList(
                // import 'range;
                new ImportStatement("range", rangeId, false, region),

                // variableId = begin;
                new DeclareVariableStatement(variableId, begin, variable.getRegion()),

                // let iteratorObjectId = new Range(variableId, end).get_iterator()
                new DeclareVariableStatement(
                    iteratorId,
                    new CallExpression(
                        new LoadAttributeExpression(
                            new NewExpression(
                                new LoadAttributeExpression(
                                    new LoadSymbolExpression(rangeId, region),
                                    "Range",
                                    region
                                ),
                                Arrays.asList(
                                    new LoadSymbolExpression(variableId, start),
                                    end,
                                    new LoadConstantExpression<>(inclusive, start)
                                ),
                                start
                            ),
                            "get_iterator",
                            start
                        ),
                        Collections.emptyList(),
                        region
                    ),
                    start
                ),

                new LoopStatement(
                    // iteratorObjectId.has_next()
                    new CallExpression(
                        new LoadAttributeExpression(
                            new LoadSymbolExpression(iteratorId, region),
                            "has_next",
                            region
                        ),
                        Collections.emptyList(),
                        region
                    ),

                    new BlockStatement(Arrays.asList(
                        // variableId = iteratorObjectId.get_next()
                        new AssignSymbolStatement(
                            variableId,
                            new CallExpression(
                                new LoadAttributeExpression(
                                    new LoadSymbolExpression(iteratorId, region),
                                    "get_next",
                                    region
                                ),
                                Collections.emptyList(),
                                region
                            ),
                            region
                        ),
                        body
                    ), region),
                    name,
                    region
                )
            ), region);
        }
    }

    private ContinueStatement parseContinueStatement() throws ScriptException {
        Region start = expect(Continue).getRegion();
        Token name = consume(Symbol);
        Region end = expect(Semicolon).getRegion();
        return new ContinueStatement(name == null ? null : name.getStringValue(), start.until(end));
    }

    private BreakStatement parseBreakStatement() throws ScriptException {
        Region start = expect(Break).getRegion();
        Token name = consume(Symbol);
        Region end = expect(Semicolon).getRegion();
        return new BreakStatement(name == null ? null : name.getStringValue(), start.until(end));
    }

    private DeclareFunctionStatement parseConstructorDeclareStatement(Region start) throws ScriptException {
        List<String> arguments = new ArrayList<>();
        boolean variadic = parseFunctionArgumentsList(arguments);
        BlockStatement body = parseBlockStatement();
        return new DeclareFunctionStatement("<init>", arguments, body, variadic, start.until(body.getRegion()));
    }

    private AssertStatement parseAssertStatement() throws ScriptException {
        Region start = expect(Assert).getRegion();
        Expression condition = parseExpression();
        String message = "";
        if (consume(Comma) != null) {
            message = expect(String).getStringValue();
        }
        Token end = expect(Semicolon);
        String source = condition.getRegion().of(tokenizer.getBuffer());
        return new AssertStatement(condition, source, message, start.until(end.getRegion()));
    }

    private TryStatement parseTryStatement() throws ScriptException {
        final Region start = expect(Try).getRegion();
        final BlockStatement body = parseBlockStatement();

        Region region = start;

        String recoverBlockName = null;
        BlockStatement recoverBlock = null;
        if (consume(Recover) != null) {
            final Token name = consume(Symbol);
            if (name != null) {
                recoverBlockName = name.getStringValue();
            }
            recoverBlock = parseBlockStatement();
            region = region.until(recoverBlock.getRegion());
        }

        BlockStatement finallyBlock = null;
        if (consume(Finally) != null) {
            finallyBlock = parseBlockStatement();
            region = region.until(finallyBlock.getRegion());
        }

        if (recoverBlock == null && finallyBlock == null) {
            throw new ScriptException("The 'try' statement must contain at least either 'recover' or 'finally' block", region);
        }

        return new TryStatement(body, recoverBlock, recoverBlockName, finallyBlock, region);
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
        boolean variadic = parseFunctionArgumentsList(arguments);
        BlockStatement body = parseBlockStatement();
        return new DeclareFunctionStatement(name, arguments, body, variadic, start.until(body.getRegion()));
    }

    private boolean parseFunctionArgumentsList(Collection<String> output) throws ScriptException {
        expect(ParenL);

        if (!matches(ParenR)) {
            output.add(expect(Symbol).getStringValue());

            while (!matches(ParenR, End)) {
                Token token = expect(Comma, Ellipsis);

                if (token.getKind() == Ellipsis) {
                    expect(ParenR);
                    return true;
                }

                output.add(expect(Symbol).getStringValue());
            }
        }

        expect(ParenR);
        return false;
    }

    private DeclareClassStatement parseClassDeclareStatement(Region start) throws ScriptException {
        String name = expect(Symbol).getStringValue();

        List<Expression> bases = new ArrayList<>();
        if (consume(Colon) != null) {
            list(new TokenKind[]{Symbol}, Comma, bases, this::parseQualifiedIdentifier);
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
        boolean variadic = parseFunctionArgumentsList(arguments);

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
            variadic,
            start.getRegion().until(body.getRegion()));
        return new LambdaExpression(function, function.getRegion());
    }

    private Expression parseUnaryExpression() throws ScriptException {
        Region start = token.getRegion();
        Token operator = consume(Not, Add, Sub, Try);
        if (operator != null) {
            Expression rhs = parsePrimaryExpression();
            return new UnaryExpression(rhs, operator.getKind(), start.until(rhs.getRegion()));
        }
        return parsePrimaryExpression();
    }

    private Expression parsePrimaryExpression() throws ScriptException {
        Token token = expect(Symbol, Number, True, False, None, String, StringPart, New, Super, ParenL, BracketL);
        Region start = token.getRegion();

        if (token.getKind() == Symbol) {
            return parsePrimaryExpression(new LoadSymbolExpression(token.getStringValue(), start));
        }

        if (token.getKind() == Number) {
            return parsePrimaryExpression(new LoadConstantExpression<>(token.getNumberValue(), start));
        }

        if (token.getKind() == True) {
            return parsePrimaryExpression(new LoadConstantExpression<>(true, start));
        }

        if (token.getKind() == False) {
            return parsePrimaryExpression(new LoadConstantExpression<>(false, start));
        }

        if (token.getKind() == None) {
            return parsePrimaryExpression(new LoadConstantExpression<>(NoneValue.INSTANCE, start));
        }

        if (token.getKind() == String) {
            return parsePrimaryExpression(new LoadConstantExpression<>(token.getStringValue(), start));
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

            return parsePrimaryExpression(string);
        }

        if (token.getKind() == New) {
            Expression callee = parseQualifiedIdentifier();
            List<Expression> arguments = new ArrayList<>();
            Region region = list(ParenL, ParenR, Comma, arguments, this::parseExpression);
            return parsePrimaryExpression(new NewExpression(callee, arguments, token.getRegion().until(region)));
        }

        if (token.getKind() == Super) {
            Region region = token.getRegion();

            Expression target = null;
            if (consume(BracketL) != null) {
                target = parseExpression();
                region = region.until(expect(BracketR).getRegion());
            }

            List<Expression> arguments = null;
            if (matches(ParenL)) {
                arguments = new ArrayList<>();
                region = region.until(list(ParenL, ParenR, Comma, arguments, this::parseExpression));
            }

            return parsePrimaryExpression(new SuperExpression(target, arguments, token.getRegion().until(region)));
        }

        if (token.getKind() == ParenL) {
            Expression expression = parseExpression();
            Region end = expect(ParenR).getRegion();
            return parsePrimaryExpression(new CompoundExpression(expression, start.until(end)));
        }

        if (token.getKind() == BracketL) {
            List<Expression> elements = new ArrayList<>();

            if (!matches(BracketR)) {
                elements.add(parseExpression());

                while (!matches(BracketR, End)) {
                    expect(Comma);
                    elements.add(parseExpression());
                }
            }

            Region end = expect(BracketR).getRegion();
            return parsePrimaryExpression(new ArrayExpression(elements, start.until(end)));
        }

        throw new AssertionError("Unreachable");
    }

    private Expression parsePrimaryExpression(Expression lhs) throws ScriptException {
        if (!matches(ParenL, Dot, BracketL)) {
            return lhs;
        }

        if (consume(Dot) != null) {
            Token name = expect(Symbol);
            return parsePrimaryExpression(new LoadAttributeExpression(lhs, name.getStringValue(), lhs.getRegion().until(name.getRegion())));
        }

        if (consume(BracketL) != null) {
            Expression index = parseExpression();
            Region end = expect(BracketR).getRegion();
            return parsePrimaryExpression(new LoadIndexExpression(lhs, index, lhs.getRegion().until(end)));
        }

        List<Expression> arguments = new ArrayList<>();
        Region region = list(ParenL, ParenR, Comma, arguments, this::parseExpression);
        return parsePrimaryExpression(new CallExpression(lhs, arguments, lhs.getRegion().until(region)));
    }

    @NotNull
    private Expression parseQualifiedIdentifier() throws ScriptException {
        Expression expression = null;

        do {
            Token symbol = expect(Symbol);
            if (expression == null) {
                expression = new LoadSymbolExpression(symbol.getStringValue(), symbol.getRegion());
            } else {
                expression = new LoadAttributeExpression(expression, symbol.getStringValue(), expression.getRegion().until(symbol.getRegion()));
            }
        } while (consume(Dot) != null);

        return expression;
    }

    private <T> Region list(TokenKind opening, TokenKind closing, TokenKind separator, Collection<T> output, Supplier<T> supplier) throws ScriptException {
        Region begin = expect(opening).getRegion();

        if (!matches(closing)) {
            output.add(supplier.get());

            while (!matches(closing, End)) {
                if (separator != null) {
                    expect(separator);
                }
                output.add(supplier.get());
            }
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
