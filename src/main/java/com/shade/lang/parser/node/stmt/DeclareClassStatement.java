package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.ClassContext;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.expr.CallExpression;
import com.shade.lang.parser.node.expr.LoadAttributeExpression;
import com.shade.lang.parser.node.expr.LoadSymbolExpression;
import com.shade.lang.parser.token.Region;
import com.shade.lang.vm.runtime.Class;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DeclareClassStatement extends Statement {
    private final String name;
    private final List<String> bases;
    private final List<Statement> members;
    private final Statement constructor;

    public DeclareClassStatement(String name, List<String> bases, List<Statement> members, Region region, Statement constructor) {
        super(region);
        this.name = name;
        this.bases = Collections.unmodifiableList(bases);
        this.members = Collections.unmodifiableList(members);
        this.constructor = constructor;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        Module module = context.getModule();

        Class[] resolvedBases = new Class[bases.size()];

        for (int index = 0; index < bases.size(); index++) {
            String base = bases.get(index);
            ScriptObject object = module.getAttribute(base);

            if (object == null) {
                throw new ScriptException("Cannot find base class '" + base + "'", getRegion());
            }

            if (!(object instanceof Class)) {
                throw new ScriptException("Cannot inherit from non-class '" + base + "'", getRegion());
            }

            resolvedBases[index] = (Class) object;
        }

        Class clazz = new Class(module, name, resolvedBases);
        ClassContext clazzContext = new ClassContext(context, clazz);

        if (constructor == null) {
            List<Statement> baseConstructors = new ArrayList<>();

            for (Class base : resolvedBases) {
                Expression getSelf = new LoadSymbolExpression("self", getRegion());
                Expression getBase = new LoadSymbolExpression(base.getName(), getRegion());
                Expression getBaseConstructor = new LoadAttributeExpression(getBase, "<init>", getRegion());

                // TODO: Check that base constructor does not accept parameters in this case
                Expression callConstructor = new CallExpression(
                        getBaseConstructor,
                        Collections.singletonList(getSelf),
                        getRegion()
                );

                baseConstructors.add(new ExpressionStatement(callConstructor, getRegion()));
            }

            DeclareFunctionStatement constructor = new DeclareFunctionStatement(
                    "<init>",
                    Collections.singletonList("self"),
                    new BlockStatement(
                            baseConstructors,
                            getRegion()
                    ),
                    getRegion()
            );

            constructor.compile(clazzContext, null);
        } else {
            // TODO: Verify that all base classes' constructors are called first
            throw new ScriptException("User-defined constructors are not supported yet", getRegion());
        }

        for (Statement member : members) {
            member.compile(clazzContext, null);
        }

        context.setAttribute(name, clazz);
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

    public Statement getConstructor() {
        return constructor;
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
