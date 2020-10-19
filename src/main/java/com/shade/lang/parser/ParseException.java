package com.shade.lang.parser;

import com.shade.lang.parser.token.Region;

public class ParseException extends Exception {
    private final Region region;

    public ParseException(String message, Region region) {
        super(message);
        this.region = region;
    }

    public Region getRegion() {
        return region;
    }
}
