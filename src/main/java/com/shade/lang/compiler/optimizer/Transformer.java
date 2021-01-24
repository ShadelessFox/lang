package com.shade.lang.compiler.optimizer;

import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.expr.*;
import com.shade.lang.compiler.parser.node.stmt.*;

public interface Transformer {
    int getLevel();

    Expression transform(Expression expression);

    Expression transform(ArrayExpression expression);

    Expression transform(BinaryExpression expression);

    Expression transform(CallExpression expression);

    Expression transform(CompoundExpression expression);

    Expression transform(LambdaExpression expression);

    Expression transform(LoadAttributeExpression expression);

    Expression transform(LoadConstantExpression<?> expression);

    Expression transform(LoadIndexExpression expression);

    Expression transform(LoadSymbolExpression expression);

    Expression transform(LogicalExpression expression);

    Expression transform(NewExpression expression);

    Expression transform(UnaryExpression expression);

    Statement transform(Statement statement);

    Statement transform(AssertStatement statement);

    Statement transform(AssignAttributeStatement statement);

    Statement transform(AssignIndexStatement statement);

    Statement transform(AssignSymbolStatement statement);

    Statement transform(BlockStatement statement);

    Statement transform(BranchStatement statement);

    Statement transform(BreakStatement statement);

    Statement transform(ContinueStatement statement);

    Statement transform(DeclareClassStatement statement);

    Statement transform(DeclareFunctionStatement statement);

    Statement transform(DeclareVariableStatement statement);

    Statement transform(ExpressionStatement statement);

    Statement transform(ImportStatement statement);

    Statement transform(LoopStatement statement);

    Statement transform(ReturnStatement statement);

    Statement transform(TryStatement statement);

    Statement transform(UnitStatement statement);
}
