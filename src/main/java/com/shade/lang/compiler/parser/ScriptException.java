package com.shade.lang.compiler.parser;

import com.shade.lang.compiler.parser.token.Region;

public class ScriptException extends Exception {
    private final Region region;

    public ScriptException(String message, Throwable cause, Region region) {
        super(message, cause);
        this.region = region;
    }

    public ScriptException(String message, Region region) {
        super(message);
        this.region = region;
    }

    public Region getRegion() {
        return region;
    }
}
