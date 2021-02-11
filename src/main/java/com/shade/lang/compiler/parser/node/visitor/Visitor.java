package com.shade.lang.compiler.parser.node.visitor;

import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Node;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.expr.*;
import com.shade.lang.compiler.parser.node.stmt.*;
import com.shade.lang.util.annotations.NotNull;

public interface Visitor {
    boolean enterDefault(@NotNull Node node);

    @NotNull
    <T extends Node> T leaveDefault(@NotNull T node);

    boolean enterArrayExpression(@NotNull ArrayExpression expression);

    @NotNull
    Expression leaveArrayExpression(@NotNull ArrayExpression expression);

    boolean enterBinaryExpression(@NotNull BinaryExpression expression);

    @NotNull
    Expression leaveBinaryExpression(@NotNull BinaryExpression expression);

    boolean enterCallExpression(@NotNull CallExpression expression);

    @NotNull
    Expression leaveCallExpression(@NotNull CallExpression expression);

    boolean enterCompoundExpression(@NotNull CompoundExpression expression);

    @NotNull
    Expression leaveCompoundExpression(@NotNull CompoundExpression expression);

    boolean enterLambdaExpression(@NotNull LambdaExpression expression);

    @NotNull
    Expression leaveLambdaExpression(@NotNull LambdaExpression expression);

    boolean enterLoadAttributeExpression(@NotNull LoadAttributeExpression expression);

    @NotNull
    Expression leaveLoadAttributeExpression(@NotNull LoadAttributeExpression expression);

    boolean enterLoadConstantExpression(@NotNull LoadConstantExpression<?> expression);

    @NotNull
    Expression leaveLoadConstantExpression(@NotNull LoadConstantExpression<?> expression);

    boolean enterLoadIndexExpression(@NotNull LoadIndexExpression expression);

    @NotNull
    Expression leaveLoadIndexExpression(@NotNull LoadIndexExpression expression);

    boolean enterLoadSymbolExpression(@NotNull LoadSymbolExpression expression);

    @NotNull
    Expression leaveLoadSymbolExpression(@NotNull LoadSymbolExpression expression);

    boolean enterLogicalExpression(@NotNull LogicalExpression expression);

    @NotNull
    Expression leaveLogicalExpression(@NotNull LogicalExpression expression);

    boolean enterNewExpression(@NotNull NewExpression expression);

    @NotNull
    Expression leaveNewExpression(@NotNull NewExpression expression);

    boolean enterSuperExpression(@NotNull SuperExpression expression);

    @NotNull
    Expression leaveSuperExpression(@NotNull SuperExpression expression);

    boolean enterUnaryExpression(@NotNull UnaryExpression expression);

    @NotNull
    Expression leaveUnaryExpression(@NotNull UnaryExpression expression);

    boolean enterUnwrapExpression(@NotNull UnwrapExpression expression);

    @NotNull
    Expression leaveUnwrapExpression(@NotNull UnwrapExpression expression);

    boolean enterAssertStatement(@NotNull AssertStatement statement);

    @NotNull
    Statement leaveAssertStatement(@NotNull AssertStatement statement);

    boolean enterAssignAttributeStatement(@NotNull AssignAttributeStatement statement);

    @NotNull
    Statement leaveAssignAttributeStatement(@NotNull AssignAttributeStatement statement);

    boolean enterAssignIndexStatement(@NotNull AssignIndexStatement statement);

    @NotNull
    Statement leaveAssignIndexStatement(@NotNull AssignIndexStatement statement);

    boolean enterAssignSymbolStatement(@NotNull AssignSymbolStatement statement);

    @NotNull
    Statement leaveAssignSymbolStatement(@NotNull AssignSymbolStatement statement);

    boolean enterBlockStatement(@NotNull BlockStatement statement);

    @NotNull
    Statement leaveBlockStatement(@NotNull BlockStatement statement);

    boolean enterBranchStatement(@NotNull BranchStatement statement);

    @NotNull
    Statement leaveBranchStatement(@NotNull BranchStatement statement);

    boolean enterBreakStatement(@NotNull BreakStatement statement);

    @NotNull
    Statement leaveBreakStatement(@NotNull BreakStatement statement);

    boolean enterContinueStatement(@NotNull ContinueStatement statement);

    @NotNull
    Statement leaveContinueStatement(@NotNull ContinueStatement statement);

    boolean enterDeclareClassStatement(@NotNull DeclareClassStatement statement);

    @NotNull
    Statement leaveDeclareClassStatement(@NotNull DeclareClassStatement statement);

    boolean enterDeclareFunctionStatement(@NotNull DeclareFunctionStatement statement);

    @NotNull
    Statement leaveDeclareFunctionStatement(@NotNull DeclareFunctionStatement statement);

    boolean enterDeclareVariableStatement(@NotNull DeclareVariableStatement statement);

    @NotNull
    Statement leaveDeclareVariableStatement(@NotNull DeclareVariableStatement statement);

    boolean enterExpressionStatement(@NotNull ExpressionStatement statement);

    @NotNull
    Statement leaveExpressionStatement(@NotNull ExpressionStatement statement);

    boolean enterImportStatement(@NotNull ImportStatement statement);

    @NotNull
    Statement leaveImportStatement(@NotNull ImportStatement statement);

    boolean enterLoopStatement(@NotNull LoopStatement statement);

    @NotNull
    Statement leaveLoopStatement(@NotNull LoopStatement statement);

    boolean enterReturnStatement(@NotNull ReturnStatement statement);

    @NotNull
    Statement leaveReturnStatement(@NotNull ReturnStatement statement);

    boolean enterThrowStatement(@NotNull ThrowStatement statement);

    @NotNull
    Statement leaveThrowStatement(@NotNull ThrowStatement statement);

    boolean enterTryStatement(@NotNull TryStatement statement);

    @NotNull
    Statement leaveTryStatement(@NotNull TryStatement statement);

    boolean enterUnitStatement(@NotNull UnitStatement statement);

    @NotNull
    Statement leaveUnitStatement(@NotNull UnitStatement statement);
}
