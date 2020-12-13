package com.shade.lang.vm.runtime.module;

import com.shade.lang.parser.node.stmt.ImportStatement;
import com.shade.lang.vm.runtime.ScriptObject;

import java.util.ArrayList;
import java.util.List;

public class Module extends ScriptObject {
    private final String name;
    private final String source;
    private final List<ImportStatement> imports;

    public Module(String name, String source) {
        super(true);
        this.name = name;
        this.source = source;
        this.imports = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    public List<ImportStatement> getImports() {
        return imports;
    }
}