package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;

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
        throw new ScriptException("Not implemented", getRegion());
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
