package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.ScriptException;
import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class LoadAttributeExpression extends Expression {
    private final Expression owner;
    private final String name;

    public LoadAttributeExpression(Expression owner, String name, Region region) {
        super(region);
        this.owner = owner;
        this.name = name;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        owner.compile(context, assembler);
        assembler.imm8(Opcode.GET_ATTRIBUTE);
        assembler.imm32(assembler.constant(name));
        assembler.span(getRegion().getBegin());
    }

    public Expression getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }
}
