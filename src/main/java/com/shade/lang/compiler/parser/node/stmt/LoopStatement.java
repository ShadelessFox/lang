package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.context.LoopContext;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

public class LoopStatement extends Statement {
    private final Expression condition;
    private final BlockStatement body;
    private final String name;

    public LoopStatement(Expression condition, BlockStatement body, String name, Region region) {
        super(region);
        this.condition = condition;
        this.body = body;
        this.name = name;
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

        LoopContext loopContext = new LoopContext(context, name);
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

    @NotNull
    @Override
    public Statement accept(@NotNull Visitor visitor) {
        if (visitor.enterLoopStatement(this)) {
            final Expression condition = this.condition == null ? null : this.condition.accept(visitor);
            final BlockStatement body = (BlockStatement) this.body.accept(visitor);

            if (condition != this.condition || body != this.body) {
                return visitor.leaveLoopStatement(new LoopStatement(condition, body, name, getRegion()));
            } else {
                return visitor.leaveLoopStatement(this);
            }
        }

        return this;
    }

    public Expression getCondition() {
        return condition;
    }

    public BlockStatement getBody() {
        return body;
    }

    public String getName() {
        return name;
    }
}
