package com.shade.lang.compiler.optimizer;

import com.shade.lang.compiler.optimizer.transformers.ConstantFoldingTransformer;
import com.shade.lang.compiler.optimizer.transformers.DeadCodeEliminationTransformer;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Node;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.runtime.Machine;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Optimizer {
    private static final Logger LOG = Logger.getLogger(Optimizer.class.getName());

    static {
        LOG.setLevel(Machine.ENABLE_LOGGING ? null : Level.OFF);
    }

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

        LOG.info("Running optimization transformers of level " + level + " or lower with " + passes + " pass(-es)");

        List<Transformer> transformers = Stream.of(Optimizer.transformers)
            .filter(x -> x.getLevel() <= level)
            .collect(Collectors.toList());

        Node result = node;

        for (int pass = 0; pass < passes; pass++) {
            Node optimized = result;

            LOG.info("Optimization pass #" + pass);

            for (Transformer transformer : transformers) {
                LOG.info("Optimizing using " + transformer + " transformer");

                if (node instanceof Statement) {
                    optimized = transformer.transform((Statement) optimized);
                } else {
                    optimized = transformer.transform((Expression) optimized);
                }
            }

            if (optimized == result) {
                LOG.info("Nothing more to optimize");
                break;
            }

            result = optimized;
        }

        return result;
    }
}
