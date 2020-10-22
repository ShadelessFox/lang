package com.shade.lang.parser;

import com.shade.lang.parser.token.Region;

public class ScriptException extends Exception {
    private final Region region;

    public ScriptException(String message, Region region) {
        super(message);
        this.region = region;
    }

    public Region getRegion() {
        return region;
    }
}
