package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Operand;
import com.shade.lang.compiler.Operation;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class AssignAttributeStatement extends Statement {
    private final Expression target;
    private final String name;
    private final Expression value;

    public AssignAttributeStatement(Expression target, String name, Expression value, Region region) {
        super(region);
        this.target = target;
        this.name = name;
        this.value = value;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        target.compile(context, assembler);
        value.compile(context, assembler);
        assembler.emit(Operation.SET_ATTRIBUTE, Operand.constant(name));
        assembler.addLocation(getRegion().getBegin());
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    public Expression getTarget() {
        return target;
    }
}
