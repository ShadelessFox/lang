package com.shade.lang.parser.node.expr;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Operand;
import com.shade.lang.compiler.Operation;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.stmt.DeclareFunctionStatement;
import com.shade.lang.parser.token.Region;

import java.util.Map;

public class LambdaExpression extends Expression {
    private final DeclareFunctionStatement function;

    public LambdaExpression(DeclareFunctionStatement function, Region region) {
        super(region);
        this.function = function;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        function.compile(context, assembler);

        assembler.emit(Operation.GET_GLOBAL, Operand.constant(function.getName()));

        for (Map.Entry<Integer, Integer> argument : function.getBoundArgumentsMapping().entrySet()) {
            assembler.emit(Operation.DUP);
            assembler.emit(Operation.GET_LOCAL, Operand.imm8(argument.getValue()));
            assembler.emit(Operation.BIND, Operand.imm8(argument.getKey()));
        }
    }

    public DeclareFunctionStatement getFunction() {
        return function;
    }
}
