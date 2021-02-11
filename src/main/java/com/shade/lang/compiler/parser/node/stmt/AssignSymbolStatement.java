package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

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

    @NotNull
    @Override
    public Statement accept(@NotNull Visitor visitor) {
        if (visitor.enterAssignSymbolStatement(this)) {
            final Expression value = this.value.accept(visitor);

            if (value != this.value) {
                return visitor.leaveAssignSymbolStatement(new AssignSymbolStatement(name, value, getRegion()));
            } else {
                return visitor.leaveAssignSymbolStatement(this);
            }
        }

        return this;
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }
}
