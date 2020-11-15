package com.shade.lang.parser.node.visitor;

import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.expr.*;
import com.shade.lang.parser.node.stmt.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class SimpleVisitor implements Visitor {
    protected SimpleVisitor() {
    }

    @Override
    public void visit(Expression expression) {
        try {
            MethodHandles.lookup()
                .findVirtual(Visitor.class, "visit", MethodType.methodType(void.class, expression.getClass()))
                .invoke(this, expression);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot find visitor for expression " + expression, e);
        }
    }

    @Override
    public void visit(Statement statement) {
        try {
            MethodHandles.lookup()
                .findVirtual(Visitor.class, "visit", MethodType.methodType(void.class, statement.getClass()))
                .invoke(this, statement);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot find visitor for statement " + statement, e);
        }
    }

    @Override
    public void visit(BinaryExpression expression) {
    }

    @Override
    public void visit(CallExpression expression) {
    }

    @Override
    public void visit(CompoundExpression expression) {
    }

    @Override
    public void visit(LambdaExpression expression) {
    }

    @Override
    public void visit(LoadAttributeExpression expression) {
    }

    @Override
    public void visit(LoadConstantExpression<?> expression) {
    }

    @Override
    public void visit(LoadIndexExpression expression) {
    }

    @Override
    public void visit(LoadSymbolExpression expression) {
    }

    @Override
    public void visit(LogicalExpression expression) {
    }

    @Override
    public void visit(NewExpression expression) {
    }

    @Override
    public void visit(UnaryExpression expression) {
    }

    @Override
    public void visit(AssertStatement statement) {
    }

    @Override
    public void visit(AssignAttributeStatement statement) {
    }

    @Override
    public void visit(AssignIndexStatement statement) {
    }

    @Override
    public void visit(AssignSymbolStatement statement) {
    }

    @Override
    public void visit(BlockStatement statement) {
    }

    @Override
    public void visit(BranchStatement statement) {
    }

    @Override
    public void visit(BreakStatement statement) {
    }

    @Override
    public void visit(ContinueStatement statement) {
    }

    @Override
    public void visit(DeclareClassStatement statement) {
    }

    @Override
    public void visit(DeclareFunctionStatement statement) {
    }

    @Override
    public void visit(DeclareVariableStatement statement) {
    }

    @Override
    public void visit(ExpressionStatement statement) {
    }

    @Override
    public void visit(ImportStatement statement) {
    }

    @Override
    public void visit(LoopStatement statement) {
    }

    @Override
    public void visit(ReturnStatement statement) {
    }

    @Override
    public void visit(TryStatement statement) {
    }

    @Override
    public void visit(UnitStatement statement) {
    }
}
