package com.shade.lang;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.function.Function;
import com.shade.lang.vm.runtime.module.NativeModuleProvider;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class Launcher {
    public static void main(String[] args) throws Exception {
        Machine machine = new Machine();
        machine.getSearchRoots().add(Paths.get("src/main/resources"));

        for (NativeModuleProvider provider : ServiceLoader.load(NativeModuleProvider.class)) {
            machine.load(provider.create(machine));
        }

        machine.load("sandbox");

        if (!machine.isHalted()) {
            machine.call("sandbox", "main");
        }

        if (Machine.ENABLE_PROFILING) {
            printProfileResults(machine);
        }

        System.exit(machine.getStatus());
    }

    private static void printProfileResults(Machine machine) {
        machine.getOut().println("--- Profile Results ---");

        for (Map.Entry<Machine.Frame, List<Long>> entry : machine.getProfilerSamples().entrySet()) {
            Function function = entry.getKey().getFunction();
            if (entry.getValue().size() > 0) {
                long min = Long.MAX_VALUE;
                long max = 0;
                long avg = 0;

                for (long sample : entry.getValue()) {
                    if (sample < min) {
                        min = sample;
                    }
                    if (sample > max) {
                        max = sample;
                    }
                    avg += sample;
                }

                machine.getOut().printf("- %s/%s%n  [ samples: %d, min: %.2fms, max: %.2fms, avg: %.2fms ]%n",
                    function.getModule().getName(),
                    function.getName(),
                    entry.getValue().size(),
                    min / 1e6D,
                    max / 1e6D,
                    avg / entry.getValue().size() / 1e6D);
            }
        }
    }
}
