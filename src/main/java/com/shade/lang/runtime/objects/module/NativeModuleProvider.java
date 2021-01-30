package com.shade.lang.runtime.objects.module;

import com.shade.lang.runtime.Machine;

public interface NativeModuleProvider {
    NativeModule create(Machine machine);
}
