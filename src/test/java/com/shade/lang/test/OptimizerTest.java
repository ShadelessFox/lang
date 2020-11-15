package com.shade.lang.test;

import com.shade.lang.optimizer.Optimizer;
import com.shade.lang.parser.node.Node;
import com.shade.lang.parser.node.expr.BinaryExpression;
import com.shade.lang.parser.node.expr.LoadConstantExpression;
import com.shade.lang.parser.node.expr.LogicalExpression;
import com.shade.lang.parser.node.expr.UnaryExpression;
import com.shade.lang.parser.node.stmt.BlockStatement;
import com.shade.lang.parser.node.stmt.BranchStatement;
import com.shade.lang.parser.node.stmt.ReturnStatement;
import com.shade.lang.parser.token.TokenKind;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class OptimizerTest {
    private static final int OPTIMIZER_PASSES_COUNT = 10;

    @Test
    public void testConstantFolding() {
        assertOptimized(
            new LoadConstantExpression<>(12, null),
            new BinaryExpression(
                new LoadConstantExpression<>(5, null),
                new LoadConstantExpression<>(7, null),
                TokenKind.Add,
                null
            )
        );

        assertOptimized(
            new LoadConstantExpression<>(true, null),
            new LogicalExpression(
                new LoadConstantExpression<>(true, null),
                new LoadConstantExpression<>(false, null),
                TokenKind.Or,
                null
            )
        );

        assertOptimized(
            new LoadConstantExpression<>(false, null),
            new LogicalExpression(
                new LoadConstantExpression<>(false, null),
                new LoadConstantExpression<>(false, null),
                TokenKind.Or,
                null
            )
        );

        assertOptimized(
            new LoadConstantExpression<>(true, null),
            new LogicalExpression(
                new LoadConstantExpression<>(true, null),
                new LoadConstantExpression<>(true, null),
                TokenKind.And,
                null
            )
        );

        assertOptimized(
            new LoadConstantExpression<>(false, null),
            new LogicalExpression(
                new LoadConstantExpression<>(false, null),
                new LoadConstantExpression<>(true, null),
                TokenKind.And,
                null
            )
        );

        assertOptimized(
            new LoadConstantExpression<>(-5, null),
            new UnaryExpression(
                new LoadConstantExpression<>(5, null),
                TokenKind.Sub,
                null
            )
        );
    }

    @Test
    public void testDeadCodeElimination() {
        assertOptimized(
            new BlockStatement(
                Collections.singletonList(
                    new ReturnStatement(new LoadConstantExpression<>(1, null), null)
                ),
                null
            ),
            new BlockStatement(
                Arrays.asList(
                    new ReturnStatement(new LoadConstantExpression<>(1, null), null),
                    new ReturnStatement(new LoadConstantExpression<>(2, null), null),
                    new ReturnStatement(new LoadConstantExpression<>(3, null), null)
                ),
                null
            )
        );

        assertOptimized(
            new BlockStatement(
                Collections.singletonList(
                    new ReturnStatement(new LoadConstantExpression<>(1, null), null)
                ),
                null
            ),
            new BranchStatement(
                new LoadConstantExpression<>(true, null),
                new BlockStatement(
                    Collections.singletonList(
                        new ReturnStatement(new LoadConstantExpression<>(1, null), null)
                    ),
                    null
                ),
                new BlockStatement(
                    Collections.singletonList(
                        new ReturnStatement(new LoadConstantExpression<>(2, null), null)
                    ),
                    null
                ),
                null
            )
        );
    }

    private void assertOptimized(Node expected, Node actual) {
        Node optimized = Optimizer.optimize(actual, Integer.MAX_VALUE, OPTIMIZER_PASSES_COUNT);
        Assert.assertEquals(expected, optimized);
    }
}
