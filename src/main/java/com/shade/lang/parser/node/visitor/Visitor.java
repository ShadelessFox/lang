package com.shade.lang.parser.node.visitor;

import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.expr.*;
import com.shade.lang.parser.node.stmt.*;

public interface Visitor {
    void visit(Expression expression);

    void visit(Statement expression);

    void visit(BinaryExpression expression);

    void visit(CallExpression expression);

    void visit(CompoundExpression expression);

    void visit(LambdaExpression expression);

    void visit(LoadAttributeExpression expression);

    void visit(LoadConstantExpression<?> expression);

    void visit(LoadSymbolExpression expression);

    void visit(LogicalExpression expression);

    void visit(UnaryExpression expression);

    void visit(AssertStatement statement);

    void visit(AssignAttributeStatement statement);

    void visit(AssignSymbolStatement statement);

    void visit(BlockStatement statement);

    void visit(BranchStatement statement);

    void visit(BreakStatement statement);

    void visit(ContinueStatement statement);

    void visit(DeclareFunctionStatement statement);

    void visit(DeclareVariableStatement statement);

    void visit(ExpressionStatement statement);

    void visit(ImportStatement statement);

    void visit(LoopStatement statement);

    void visit(ReturnStatement statement);

    void visit(TryStatement statement);

    void visit(UnitStatement statement);
}
