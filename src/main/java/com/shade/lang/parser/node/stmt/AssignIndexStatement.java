package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class AssignIndexStatement extends Statement {
    private final Expression target;
    private final Expression index;
    private final Expression value;

    public AssignIndexStatement(Expression target, Expression index, Expression value, Region region) {
        super(region);
        this.target = target;
        this.index = index;
        this.value = value;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        assembler.addDebugLine(getRegion().getBegin(), "Assign index");
        target.compile(context, assembler);
        index.compile(context, assembler);
        value.compile(context, assembler);
        assembler.imm8(Opcode.SET_INDEX);
        assembler.addTraceLine(getRegion().getBegin());
    }

    public Expression getTarget() {
        return target;
    }

    public Expression getIndex() {
        return index;
    }

    public Expression getValue() {
        return value;
    }
}
