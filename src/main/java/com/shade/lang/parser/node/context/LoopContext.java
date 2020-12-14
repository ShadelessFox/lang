package com.shade.lang.parser.node.context;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Statement;

import java.util.ArrayList;
import java.util.List;

public class LoopContext extends Context {
    private final List<Canceller> cancellers;
    private final String name;

    public LoopContext(Context parent, String name) {
        super(parent);
        this.cancellers = new ArrayList<>();
        this.name = name;
    }

    public void addCanceller(Statement statement, Assembler.Label label, CancelType type, String name) throws ScriptException {
        if (name != null) {
            for (LoopContext context = this; context != null; context = context.parent.unwrap(LoopContext.class)) {
                if (name.equals(context.name)) {
                    context.cancellers.add(new Canceller(label, type));
                    return;
                }
            }

            throw new ScriptException("Cannot find loop named '" + name + "'", statement.getRegion());
        }

        cancellers.add(new Canceller(label, type));
    }

    public List<Canceller> getCancellers() {
        return cancellers;
    }

    public String getName() {
        return name;
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
