package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DeclareClassStatement extends Statement {
    private final String name;
    private final List<String> bases;
    private final List<Statement> members;

    public DeclareClassStatement(String name, List<String> bases, List<Statement> members, Region region) {
        super(region);
        this.name = name;
        this.bases = Collections.unmodifiableList(bases);
        this.members = Collections.unmodifiableList(members);
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        throw new ScriptException("Not implemented", getRegion());
    }

    public String getName() {
        return name;
    }

    public List<String> getBases() {
        return bases;
    }

    public List<Statement> getMembers() {
        return members;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeclareClassStatement that = (DeclareClassStatement) o;
        return name.equals(that.name) &&
            bases.equals(that.bases) &&
            members.equals(that.members);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, bases, members);
    }
}
