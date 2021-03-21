package com.shade.lang.tool.serialization.attributes;

import com.shade.lang.util.annotations.NotNull;

public interface Attribute<T extends Attribute<T>> {
    @NotNull
    AttributeDescriptor<T> getDescriptor();
}
