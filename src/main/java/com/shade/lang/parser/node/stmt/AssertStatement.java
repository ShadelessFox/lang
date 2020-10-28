package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class AssertStatement extends Statement {
    private final Expression condition;
    private final String source;
    private final String message;

    public AssertStatement(Expression condition, String source, String message, Region region) {
        super(region);
        this.condition = condition;
        this.message = message;
        this.source = source;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        assembler.addDebugLine(getRegion().getBegin(), "Assert");
        condition.compile(context, assembler);
        assembler.imm8(Opcode.ASSERT);
        assembler.imm32(assembler.addConstant(source));
        assembler.imm32(assembler.addConstant(message));
        assembler.addTraceLine(getRegion().getBegin());
    }

    public Expression getCondition() {
        return condition;
    }

    public String getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }
}
