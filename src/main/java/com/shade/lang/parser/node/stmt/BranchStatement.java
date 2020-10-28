package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.ScriptException;
import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.expr.LogicalExpression;
import com.shade.lang.parser.token.Region;

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
        return pass.isControlFlowReturned() && (fail == null || fail.isControlFlowReturned());
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        assembler.addDebugLine(getRegion().getBegin(), "Branch");

        if (condition instanceof LogicalExpression) {
            LogicalExpression expression = (LogicalExpression) condition;
            expression.setPass(pass);
            expression.setFail(fail);
            expression.compile(context, assembler);
        } else {
            condition.compile(context, assembler);
            Assembler.Label alt = assembler.jump(Opcode.JUMP_IF_FALSE);
            pass.compile(context, assembler);
            Assembler.Label end = pass.isControlFlowReturned() ? null : assembler.jump(Opcode.JUMP);
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
