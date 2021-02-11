package com.shade.lang.compiler.parser.node.visitor;

import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Node;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.expr.*;
import com.shade.lang.compiler.parser.node.stmt.*;
import com.shade.lang.util.annotations.NotNull;

public abstract class AbstractVisitor implements Visitor {
    @Override
    public abstract boolean enterDefault(@NotNull Node node);

    @NotNull
    @Override
    public abstract <T extends Node> T leaveDefault(@NotNull T node);

    @Override
    public boolean enterArrayExpression(@NotNull ArrayExpression expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveArrayExpression(@NotNull ArrayExpression expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterBinaryExpression(@NotNull BinaryExpression expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveBinaryExpression(@NotNull BinaryExpression expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterCallExpression(@NotNull CallExpression expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveCallExpression(@NotNull CallExpression expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterCompoundExpression(@NotNull CompoundExpression expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveCompoundExpression(@NotNull CompoundExpression expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterLambdaExpression(@NotNull LambdaExpression expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveLambdaExpression(@NotNull LambdaExpression expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterLoadAttributeExpression(@NotNull LoadAttributeExpression expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveLoadAttributeExpression(@NotNull LoadAttributeExpression expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterLoadConstantExpression(@NotNull LoadConstantExpression<?> expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveLoadConstantExpression(@NotNull LoadConstantExpression<?> expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterLoadIndexExpression(@NotNull LoadIndexExpression expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveLoadIndexExpression(@NotNull LoadIndexExpression expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterLoadSymbolExpression(@NotNull LoadSymbolExpression expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveLoadSymbolExpression(@NotNull LoadSymbolExpression expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterLogicalExpression(@NotNull LogicalExpression expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveLogicalExpression(@NotNull LogicalExpression expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterNewExpression(@NotNull NewExpression expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveNewExpression(@NotNull NewExpression expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterSuperExpression(@NotNull SuperExpression expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveSuperExpression(@NotNull SuperExpression expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterUnaryExpression(@NotNull UnaryExpression expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveUnaryExpression(@NotNull UnaryExpression expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterUnwrapExpression(@NotNull UnwrapExpression expression) {
        return enterDefault(expression);
    }

    @NotNull
    @Override
    public Expression leaveUnwrapExpression(@NotNull UnwrapExpression expression) {
        return leaveDefault(expression);
    }

    @Override
    public boolean enterAssertStatement(@NotNull AssertStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveAssertStatement(@NotNull AssertStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterAssignAttributeStatement(@NotNull AssignAttributeStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveAssignAttributeStatement(@NotNull AssignAttributeStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterAssignIndexStatement(@NotNull AssignIndexStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveAssignIndexStatement(@NotNull AssignIndexStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterAssignSymbolStatement(@NotNull AssignSymbolStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveAssignSymbolStatement(@NotNull AssignSymbolStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterBlockStatement(@NotNull BlockStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveBlockStatement(@NotNull BlockStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterBranchStatement(@NotNull BranchStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveBranchStatement(@NotNull BranchStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterBreakStatement(@NotNull BreakStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveBreakStatement(@NotNull BreakStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterContinueStatement(@NotNull ContinueStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveContinueStatement(@NotNull ContinueStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterDeclareClassStatement(@NotNull DeclareClassStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveDeclareClassStatement(@NotNull DeclareClassStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterDeclareFunctionStatement(@NotNull DeclareFunctionStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveDeclareFunctionStatement(@NotNull DeclareFunctionStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterDeclareVariableStatement(@NotNull DeclareVariableStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveDeclareVariableStatement(@NotNull DeclareVariableStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterExpressionStatement(@NotNull ExpressionStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveExpressionStatement(@NotNull ExpressionStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterImportStatement(@NotNull ImportStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveImportStatement(@NotNull ImportStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterLoopStatement(@NotNull LoopStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveLoopStatement(@NotNull LoopStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterReturnStatement(@NotNull ReturnStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveReturnStatement(@NotNull ReturnStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterThrowStatement(@NotNull ThrowStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveThrowStatement(@NotNull ThrowStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterTryStatement(@NotNull TryStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveTryStatement(@NotNull TryStatement statement) {
        return leaveDefault(statement);
    }

    @Override
    public boolean enterUnitStatement(@NotNull UnitStatement statement) {
        return enterDefault(statement);
    }

    @NotNull
    @Override
    public Statement leaveUnitStatement(@NotNull UnitStatement statement) {
        return leaveDefault(statement);
    }
}
