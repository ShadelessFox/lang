package com.shade.lang.parser.node.expr;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.stmt.DeclareFunctionStatement;
import com.shade.lang.parser.token.Region;

public class LambdaExpression extends Expression {
    private final DeclareFunctionStatement function;

    public LambdaExpression(DeclareFunctionStatement function, Region region) {
        super(region);
        this.function = function;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        function.compile(context.getParent(), assembler);

        assembler.imm8(Opcode.GET_GLOBAL);
        assembler.imm32(assembler.addConstant(function.getName()));
    }

    public DeclareFunctionStatement getFunction() {
        return function;
    }
}
