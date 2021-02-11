package com.shade.lang.compiler.optimizer;

import com.shade.lang.compiler.parser.node.Node;
import com.shade.lang.runtime.Machine;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Optimizer {
    private static final Logger LOG = Logger.getLogger(Optimizer.class.getName());

    static {
        LOG.setLevel(Machine.ENABLE_LOGGING ? null : Level.OFF);
    }

    private final Transformer[] transformers;
    private final int level;

    public Optimizer(int level) {
        final List<Transformer> transformers = new ArrayList<>();

        for (TransformerProvider provider : ServiceLoader.load(TransformerProvider.class)) {
            if (provider.getLevel() <= level) {
                transformers.add(provider.create());
            }
        }

        this.transformers = transformers.toArray(new Transformer[0]);
        this.level = level;
    }

    public Node optimize(Node node, int passes) {
        if (passes <= 0) {
            return node;
        }

        LOG.info("Running optimization transformers of level <= " + level + " with " + passes + " pass(-es)");

        Node result = node;

        for (int pass = 0; pass < passes; pass++) {
            Node optimized = result;

            LOG.info("Optimization pass #" + pass);

            for (Transformer transformer : transformers) {
                LOG.info("Optimizing using '" + transformer.getClass().getName() + "' transformer");
                optimized = optimized.accept(transformer);
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
