package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.ClassContext;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.expr.LoadAttributeExpression;
import com.shade.lang.parser.node.expr.LoadSymbolExpression;
import com.shade.lang.parser.token.Region;
import com.shade.lang.vm.runtime.Class;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SuperStatement extends Statement {
    private final String name;
    private final List<Expression> arguments;

    public SuperStatement(String name, List<Expression> arguments, Region region) {
        super(region);
        this.name = name;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        ClassContext classContext = context.unwrap(ClassContext.class);

        if (classContext == null) {
            throw new ScriptException("Cannot use 'super' outside class function definition", getRegion());
        }

        Class clazz = classContext.getAssociatedClass();

        if (name != null) {
            for (Class base : clazz.getBases()) {
                if (base.getName().equals(name)) {
                    compile(context, assembler, name);
                    return;
                }
            }

            throw new ScriptException("Cannot find base class named '" + name + "'", getRegion());
        }

        if (clazz.getBases().length > 1) {
            throw new ScriptException("Name of one of the base classes must be specified", getRegion());
        }

        compile(context, assembler, clazz.getBases()[0].getName());
    }

    private void compile(Context context, Assembler assembler, String name) throws ScriptException {
        Expression constructor = new LoadAttributeExpression(
            new LoadSymbolExpression(name, getRegion()),
            "<init>",
            getRegion()
        );

        assembler.imm8(Opcode.GET_LOCAL);
        assembler.imm8(0);

        for (Expression argument : arguments) {
            argument.compile(context, assembler);
        }

        constructor.compile(context, assembler);
        assembler.addTraceLine(getRegion().getBegin());

        assembler.imm8(Opcode.CALL);
        assembler.imm8(arguments.size() + 1);
        assembler.addTraceLine(getRegion().getBegin());
        assembler.imm8(Opcode.POP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuperStatement that = (SuperStatement) o;
        return Objects.equals(name, that.name) &&
            arguments.equals(that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, arguments);
    }

    public String getName() {
        return name;
    }

    public List<Expression> getArguments() {
        return arguments;
    }
}
