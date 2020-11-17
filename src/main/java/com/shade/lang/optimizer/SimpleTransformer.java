package com.shade.lang.optimizer;

import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.expr.*;
import com.shade.lang.parser.node.stmt.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class SimpleTransformer implements Transformer {
    protected SimpleTransformer() {
    }

    @Override
    public final Statement transform(Statement statement) {
        try {
            return (Statement) MethodHandles.lookup()
                .findVirtual(Transformer.class, "transform", MethodType.methodType(Statement.class, statement.getClass()))
                .invoke(this, statement);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot find transformer for statement " + statement, e);
        }
    }

    @Override
    public final Expression transform(Expression expression) {
        try {
            return (Expression) MethodHandles.lookup()
                .findVirtual(Transformer.class, "transform", MethodType.methodType(Expression.class, expression.getClass()))
                .invoke(this, expression);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot find transformer for expression " + expression, e);
        }
    }

    @Override
    public Expression transform(BinaryExpression expression) {
        Expression lhs = expression.getLhs().transform(this);
        Expression rhs = expression.getRhs().transform(this);

        if (lhs != expression.getLhs() || rhs != expression.getRhs()) {
            return new BinaryExpression(lhs, rhs, expression.getOperator(), expression.getRegion());
        }

        return expression;
    }

    @Override
    public Expression transform(CallExpression expression) {
        List<Expression> arguments = expression.getArguments()
            .stream()
            .map(x -> x.transform(this))
            .collect(Collectors.toList());

        if (!arguments.equals(expression.getArguments())) {
            return new CallExpression(expression.getCallee(), arguments, expression.getRegion());
        }

        return expression;
    }

    @Override
    public Expression transform(CompoundExpression expression) {
        Expression inner = expression.getExpression().transform(this);

        if (inner != expression.getExpression()) {
            return new CompoundExpression(inner, expression.getRegion());
        }

        return expression;
    }

    @Override
    public Expression transform(LambdaExpression expression) {
        DeclareFunctionStatement function = (DeclareFunctionStatement) expression.getFunction().transform(this);

        if (function != expression.getFunction()) {
            return new LambdaExpression(function, expression.getRegion());
        }

        return expression;
    }

    @Override
    public Expression transform(LoadAttributeExpression expression) {
        return expression;
    }

    @Override
    public Expression transform(LoadConstantExpression<?> expression) {
        return expression;
    }

    @Override
    public Expression transform(LoadIndexExpression expression) {
        return expression;
    }

    @Override
    public Expression transform(LoadSymbolExpression expression) {
        return expression;
    }

    @Override
    public Expression transform(LogicalExpression expression) {
        Expression lhs = expression.getLhs().transform(this);
        Expression rhs = expression.getRhs().transform(this);

        if (lhs != expression.getLhs() || rhs != expression.getRhs()) {
            return new LogicalExpression(lhs, rhs, expression.getOperator(), expression.getRegion());
        }

        return expression;
    }

    @Override
    public Expression transform(NewExpression expression) {
        return expression;
    }

    @Override
    public Expression transform(UnaryExpression expression) {
        Expression rhs = expression.getRhs().transform(this);

        if (rhs != expression.getRhs()) {
            return new UnaryExpression(rhs, expression.getOperator(), expression.getRegion());
        }

        return expression;
    }

    @Override
    public Statement transform(AssertStatement statement) {
        Expression condition = statement.getCondition().transform(this);

        if (condition != statement.getCondition()) {
            return new AssertStatement(condition, statement.getSource(), statement.getMessage(), statement.getRegion());
        }

        return statement;
    }

    @Override
    public Statement transform(AssignAttributeStatement statement) {
        return statement;
    }

    @Override
    public Statement transform(AssignIndexStatement statement) {
        return statement;
    }

    @Override
    public Statement transform(AssignSymbolStatement statement) {
        return statement;
    }

    @Override
    public Statement transform(BlockStatement statement) {
        List<Statement> statements = statement.getStatements()
            .stream()
            .map(x -> x.transform(this))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (!statements.equals(statement.getStatements())) {
            return new BlockStatement(statements, statement.getRegion());
        }

        return statement;

    }

    @Override
    public Statement transform(BranchStatement statement) {
        BlockStatement pass = (BlockStatement) statement.getPass().transform(this);
        BlockStatement fail = statement.getFail() != null ? (BlockStatement) statement.getFail().transform(this) : null;

        if (pass != statement.getPass() || fail != statement.getFail()) {
            return new BranchStatement(statement.getCondition(), pass, fail, statement.getRegion());
        }

        return statement;
    }

    @Override
    public Statement transform(BreakStatement statement) {
        return statement;
    }

    @Override
    public Statement transform(ContinueStatement statement) {
        return statement;
    }

    @Override
    public Statement transform(DeclareClassStatement statement) {
        return statement;
    }

    @Override
    public Statement transform(DeclareFunctionStatement statement) {
        BlockStatement body = (BlockStatement) statement.getBody().transform(this);

        if (body != statement.getBody()) {
            return new DeclareFunctionStatement(statement.getName(), statement.getArguments(), statement.getBoundArguments(), body, statement.isVariadic(), statement.getRegion());
        }

        return statement;
    }

    @Override
    public Statement transform(DeclareVariableStatement statement) {
        Expression value = statement.getValue().transform(this);

        if (value != statement.getValue()) {
            return new DeclareVariableStatement(statement.getName(), value, statement.getRegion());
        }

        return statement;
    }

    @Override
    public Statement transform(ExpressionStatement statement) {
        Expression inner = statement.getExpression().transform(this);

        if (inner != statement.getExpression()) {
            return new ExpressionStatement(inner, statement.getRegion());
        }

        return statement;
    }

    @Override
    public Statement transform(ImportStatement statement) {
        return statement;
    }

    @Override
    public Statement transform(LoopStatement statement) {
        Expression condition = statement.getCondition() != null ? statement.getCondition().transform(this) : null;
        BlockStatement body = (BlockStatement) statement.getBody().transform(this);

        if (condition != statement.getCondition() || body != statement.getBody()) {
            return new LoopStatement(condition, body, statement.getRegion());
        }

        return statement;
    }

    @Override
    public Statement transform(ReturnStatement statement) {
        Expression value = statement.getValue().transform(this);

        if (value != statement.getValue()) {
            return new ReturnStatement(value, statement.getRegion());
        }

        return statement;
    }

    @Override
    public Statement transform(TryStatement statement) {
        BlockStatement body = (BlockStatement) statement.getBody().transform(this);
        BlockStatement recover = (BlockStatement) statement.getRecover().transform(this);

        if (body != statement.getBody() || recover != statement.getRecover()) {
            return new TryStatement(body, recover, statement.getName(), statement.getRegion());
        }

        return statement;
    }

    @Override
    public Statement transform(UnitStatement statement) {
        List<Statement> statements = statement.getStatements()
            .stream()
            .map(x -> x.transform(this))
            .collect(Collectors.toList());

        if (!statements.equals(statement.getStatements())) {
            return new UnitStatement(statement.getName(), statements, statement.getRegion());
        }

        return statement;
    }

    protected static  <T> boolean isConst(Expression expression, Class<T> clazz) {
        return expression instanceof LoadConstantExpression<?> && clazz.isInstance(((LoadConstantExpression<?>) expression).getValue());
    }

    @SuppressWarnings("unchecked")
    protected static  <T> T asConst(Expression expression) {
        return (T) ((LoadConstantExpression<?>) expression).getValue();
    }
}
