package com.shade.lang.vm.runtime.module;

import com.shade.lang.vm.Machine;

public interface NativeModuleProvider {
    NativeModule create(Machine machine);
}
