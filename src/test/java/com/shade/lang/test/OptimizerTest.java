package com.shade.lang.test;

import com.shade.lang.compiler.optimizer.Optimizer;
import com.shade.lang.compiler.parser.node.Node;
import com.shade.lang.compiler.parser.node.expr.BinaryExpression;
import com.shade.lang.compiler.parser.node.expr.LoadConstantExpression;
import com.shade.lang.compiler.parser.node.expr.LogicalExpression;
import com.shade.lang.compiler.parser.node.expr.UnaryExpression;
import com.shade.lang.compiler.parser.node.stmt.BlockStatement;
import com.shade.lang.compiler.parser.node.stmt.BranchStatement;
import com.shade.lang.compiler.parser.node.stmt.ReturnStatement;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.compiler.parser.token.TokenKind;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class OptimizerTest {
    private final Region region = new Region(new Region.Span(0, 0, 0), new Region.Span(0, 0, 0));
    private static final int OPTIMIZER_PASSES_COUNT = 10;

    @Test
    public void testConstantFolding() {
        assertOptimized(
            new LoadConstantExpression<>(12, region),
            new BinaryExpression(
                new LoadConstantExpression<>(5, region),
                new LoadConstantExpression<>(7, region),
                TokenKind.Add,
                region
            )
        );

        assertOptimized(
            new LoadConstantExpression<>(true, region),
            new LogicalExpression(
                new LoadConstantExpression<>(true, region),
                new LoadConstantExpression<>(false, region),
                TokenKind.Or,
                region
            )
        );

        assertOptimized(
            new LoadConstantExpression<>(false, region),
            new LogicalExpression(
                new LoadConstantExpression<>(false, region),
                new LoadConstantExpression<>(false, region),
                TokenKind.Or,
                region
            )
        );

        assertOptimized(
            new LoadConstantExpression<>(true, region),
            new LogicalExpression(
                new LoadConstantExpression<>(true, region),
                new LoadConstantExpression<>(true, region),
                TokenKind.And,
                region
            )
        );

        assertOptimized(
            new LoadConstantExpression<>(false, region),
            new LogicalExpression(
                new LoadConstantExpression<>(false, region),
                new LoadConstantExpression<>(true, region),
                TokenKind.And,
                region
            )
        );

        assertOptimized(
            new LoadConstantExpression<>(-5, region),
            new UnaryExpression(
                new LoadConstantExpression<>(5, region),
                TokenKind.Sub,
                region
            )
        );
    }

    @Test
    public void testDeadCodeElimination() {
        assertOptimized(
            new BlockStatement(
                Collections.singletonList(
                    new ReturnStatement(new LoadConstantExpression<>(1, region), region)
                ),
                region
            ),
            new BlockStatement(
                Arrays.asList(
                    new ReturnStatement(new LoadConstantExpression<>(1, region), region),
                    new ReturnStatement(new LoadConstantExpression<>(2, region), region),
                    new ReturnStatement(new LoadConstantExpression<>(3, region), region)
                ),
                region
            )
        );

        assertOptimized(
            new BlockStatement(
                Collections.singletonList(
                    new ReturnStatement(new LoadConstantExpression<>(1, region), region)
                ),
                region
            ),
            new BranchStatement(
                new LoadConstantExpression<>(true, region),
                new BlockStatement(
                    Collections.singletonList(
                        new ReturnStatement(new LoadConstantExpression<>(1, region), region)
                    ),
                    region
                ),
                new BlockStatement(
                    Collections.singletonList(
                        new ReturnStatement(new LoadConstantExpression<>(2, region), region)
                    ),
                    region
                ),
                region
            )
        );
    }

    private void assertOptimized(Node expected, Node actual) {
        Node optimized = Optimizer.optimize(actual, Integer.MAX_VALUE, OPTIMIZER_PASSES_COUNT);
        Assert.assertEquals(expected, optimized);
    }
}
