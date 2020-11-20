package com.shade.lang;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.module.NativeModuleProvider;

import java.nio.file.Paths;
import java.util.ServiceLoader;

public class Launcher {
    public static void main(String[] args) throws Exception {
        Machine machine = new Machine();
        machine.getSearchRoots().add(Paths.get("src/main/resources"));

        for (NativeModuleProvider provider : ServiceLoader.load(NativeModuleProvider.class)) {
            machine.load(provider.create(machine));
        }

        machine.load(Paths.get("src/main/resources/sandbox.ash"));

        if (!machine.isHalted()) {
            machine.call("sandbox", "main");
        }

        System.exit(machine.getStatus());
    }
}
