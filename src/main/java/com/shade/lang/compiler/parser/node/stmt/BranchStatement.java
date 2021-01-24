package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.expr.LogicalExpression;
import com.shade.lang.compiler.parser.token.Region;

public class BranchStatement extends Statement {
    private final Expression condition;
    private final Statement pass;
    private final Statement fail;

    public BranchStatement(Expression condition, Statement pass, Statement fail, Region region) {
        super(region);
        this.condition = condition;
        this.pass = pass;
        this.fail = fail;
    }

    @Override
    public boolean isControlFlowReturned() {
        return pass.isControlFlowReturned() && fail != null && fail.isControlFlowReturned();
    }

    @Override
    public boolean isControlFlowInterrupted() {
        return pass.isControlFlowInterrupted() && fail != null && fail.isControlFlowInterrupted();
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        if (condition instanceof LogicalExpression) {
            LogicalExpression expression = (LogicalExpression) condition;
            expression.setPass(pass);
            expression.setFail(fail);
            expression.compile(context, assembler);
        } else {
            condition.compile(context, assembler);
            Assembler.Label alt = assembler.jump(Operation.JUMP_IF_FALSE);
            assembler.addLocation(getRegion().getBegin());
            pass.compile(context, assembler);
            Assembler.Label end = pass.isControlFlowReturned() ? null : assembler.jump(Operation.JUMP);
            assembler.bind(alt);
            if (fail != null) {
                fail.compile(context, assembler);
            }
            assembler.bind(end);
        }
    }

    public Expression getCondition() {
        return condition;
    }

    public Statement getPass() {
        return pass;
    }

    public Statement getFail() {
        return fail;
    }
}
