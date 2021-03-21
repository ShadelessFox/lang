package com.shade.lang.tool.serialization.attributes;

import com.shade.lang.util.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface AttributeDescriptor<T extends Attribute<T>> {
    T load(@NotNull DataInputStream is) throws IOException;

    void save(@NotNull DataOutputStream os, @NotNull T attribute) throws IOException;

    @NotNull
    String getName();
}
