package com.shade.lang.parser.node.context;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.vm.runtime.Module;

import java.util.ArrayList;
import java.util.List;

public class LoopContext extends Context {
    private final List<Canceller> cancellers;

    public LoopContext(Context parent) {
        super(parent);
        this.cancellers = new ArrayList<>();
    }

    public LoopContext(Module module) {
        super(module);
        this.cancellers = new ArrayList<>();
    }

    public void addCanceller(Assembler.Label label, CancelType type) {
        cancellers.add(new Canceller(label, type));
    }

    public List<Canceller> getCancellers() {
        return cancellers;
    }

    public enum CancelType {
        Continue,
        Break
    }

    public static class Canceller {
        private final Assembler.Label label;
        private final CancelType type;

        public Canceller(Assembler.Label label, CancelType type) {
            this.label = label;
            this.type = type;
        }

        public Assembler.Label getLabel() {
            return label;
        }

        public CancelType getType() {
            return type;
        }

    }
}
