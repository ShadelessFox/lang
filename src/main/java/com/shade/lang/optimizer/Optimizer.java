package com.shade.lang.optimizer;

import com.shade.lang.optimizer.transformers.ConstantFoldingTransformer;
import com.shade.lang.optimizer.transformers.DeadCodeEliminationTransformer;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Node;
import com.shade.lang.parser.node.Statement;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Optimizer {
    private static final Transformer[] transformers = new Transformer[]{
        new ConstantFoldingTransformer(),
        new DeadCodeEliminationTransformer()
    };

    private Optimizer() {
    }

    public static Node optimize(Node node, int level, int passes) {
        if (passes <= 0) {
            return node;
        }

        List<Transformer> transformers = Stream.of(Optimizer.transformers)
            .filter(x -> x.getLevel() <= level)
            .collect(Collectors.toList());

        Node result = node;

        for (int pass = 0; pass < passes; pass++) {
            Node optimized = result;

            for (Transformer transformer : transformers) {
                if (node instanceof Statement) {
                    optimized = transformer.transform((Statement) optimized);
                } else {
                    optimized = transformer.transform((Expression) optimized);
                }
            }

            if (optimized == result) {
                break;
            }

            result = optimized;
        }

        return result;
    }
}
