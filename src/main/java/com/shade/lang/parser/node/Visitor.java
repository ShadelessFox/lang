package com.shade.lang.parser.node;

import com.shade.lang.parser.node.expr.*;
import com.shade.lang.parser.node.stmt.*;

public interface Visitor {
    default void visit(Expression expression) {
        if (expression instanceof BinaryExpression) {
            visit((BinaryExpression) expression);
        } else if (expression instanceof UnaryExpression) {
            visit((UnaryExpression) expression);
        } else if (expression instanceof LoadConstantExpression) {
            visit((LoadConstantExpression<?>) expression);
        } else if (expression instanceof CallExpression) {
            visit((CallExpression) expression);
        } else if (expression instanceof LoadAttributeExpression) {
            visit((LoadAttributeExpression) expression);
        } else if (expression instanceof LoadSymbolExpression) {
            visit((LoadSymbolExpression) expression);
        } else {
            throw new RuntimeException("Unknown expression: " + expression);
        }
    }

    default void visit(BinaryExpression expression) {
    }

    default void visit(UnaryExpression expression) {
    }

    default void visit(LoadConstantExpression<?> expression) {
    }

    default void visit(CallExpression expression) {
    }

    default void visit(LoadAttributeExpression expression) {
    }

    default void visit(LoadSymbolExpression expression) {
    }

    default void visit(Statement statement) {
        if (statement instanceof BlockStatement) {
            visit((BlockStatement) statement);
        } else if (statement instanceof BranchStatement) {
            visit((BranchStatement) statement);
        } else if (statement instanceof ReturnStatement) {
            visit((ReturnStatement) statement);
        } else if (statement instanceof DeclareFunctionStatement) {
            visit((DeclareFunctionStatement) statement);
        } else if (statement instanceof DeclareVariableStatement) {
            visit((DeclareVariableStatement) statement);
        } else if (statement instanceof ExpressionStatement) {
            visit((ExpressionStatement) statement);
        } else if (statement instanceof AssignSymbolStatement) {
            visit((AssignSymbolStatement) statement);
        } else if (statement instanceof AssignAttributeStatement) {
            visit((AssignAttributeStatement) statement);
        } else if (statement instanceof ImportStatement) {
            visit((ImportStatement) statement);
        } else if (statement instanceof UnitStatement) {
            visit((UnitStatement) statement);
        } else {
            throw new RuntimeException("Unknown statement: " + statement);
        }
    }

    default void visit(BlockStatement statement) {
    }

    default void visit(BranchStatement statement) {
    }

    default void visit(ReturnStatement statement) {
    }

    default void visit(DeclareFunctionStatement statement) {
    }

    default void visit(DeclareVariableStatement statement) {
    }

    default void visit(ExpressionStatement statement) {
    }

    default void visit(AssignSymbolStatement statement) {
    }

    default void visit(AssignAttributeStatement statement) {
    }

    default void visit(ImportStatement statement) {
    }

    default void visit(UnitStatement statement) {
    }
}
