package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.stmt.DeclareFunctionStatement;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

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

    @NotNull
    @Override
    public Expression accept(@NotNull Visitor visitor) {
        if (visitor.enterLambdaExpression(this)) {
            final DeclareFunctionStatement function = (DeclareFunctionStatement) this.function.accept(visitor);

            if (function != this.function) {
                return visitor.leaveLambdaExpression(new LambdaExpression(function, getRegion()));
            } else {
                return visitor.leaveLambdaExpression(this);
            }
        }

        return this;
    }

    public DeclareFunctionStatement getFunction() {
        return function;
    }
}
