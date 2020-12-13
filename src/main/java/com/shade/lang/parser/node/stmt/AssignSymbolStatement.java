package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Operand;
import com.shade.lang.compiler.Operation;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class AssignSymbolStatement extends Statement {
    private final String name;
    private final Expression value;

    public AssignSymbolStatement(String name, Expression value, Region region) {
        super(region);
        this.name = name;
        this.value = value;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        value.compile(context, assembler);

        if (context.hasSlot(name)) {
            assembler.emit(Operation.SET_LOCAL, Operand.imm8(context.addSlot(name)));
        } else {
            assembler.emit(Operation.SET_GLOBAL, Operand.constant(name));
        }
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }
}
