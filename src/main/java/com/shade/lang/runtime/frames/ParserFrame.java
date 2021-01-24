package com.shade.lang.runtime.frames;

import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.util.annotations.NotNull;

public class ParserFrame extends Frame {
    private final String source;
    private final ScriptException exception;

    public ParserFrame(@NotNull Module module, @NotNull String source, @NotNull ScriptException exception, int stack) {
        super(module, null, null, stack);
        this.source = source;
        this.exception = exception;
    }

    @NotNull
    public String getSource() {
        return source;
    }

    @NotNull
    public ScriptException getException() {
        return exception;
    }

    @Override
    public String toString() {
        return source + '(' + exception.getRegion().getBegin() + ')';
    }
}
