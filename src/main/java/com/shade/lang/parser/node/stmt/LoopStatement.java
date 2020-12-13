package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Operation;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.context.LoopContext;
import com.shade.lang.parser.token.Region;

public class LoopStatement extends Statement {
    private final Expression condition;
    private final BlockStatement body;

    public LoopStatement(Expression condition, BlockStatement body, Region region) {
        super(region);
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        Assembler.Label end = null;
        int start = assembler.getPosition();

        if (condition != null) {
            condition.compile(context, assembler);
            end = assembler.jump(Operation.JUMP_IF_FALSE);
            assembler.addLocation(getRegion().getBegin());
        }

        LoopContext loopContext = new LoopContext(context);
        body.compile(loopContext, assembler);

        assembler.bind(assembler.jump(Operation.JUMP), start);

        for (LoopContext.Canceller canceller : loopContext.getCancellers()) {
            switch (canceller.getType()) {
                case Continue:
                    assembler.bind(canceller.getLabel(), start);
                    break;
                case Break:
                    assembler.bind(canceller.getLabel());
                    break;
            }
        }

        assembler.bind(end);
    }

    public Expression getCondition() {
        return condition;
    }

    public BlockStatement getBody() {
        return body;
    }
}
