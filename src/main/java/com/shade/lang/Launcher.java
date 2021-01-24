package com.shade.lang;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.function.Function;
import com.shade.lang.runtime.objects.module.NativeModuleProvider;

import java.nio.file.Paths;
import java.util.List;
import java.util.LongSummaryStatistics;
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
            List<Long> samples = entry.getValue();

            if (samples.size() > 0) {
                LongSummaryStatistics summary = samples.stream().mapToLong(x -> x).summaryStatistics();

                machine.getOut().printf("- %s/%s%n  [ samples: %d, min: %.2fms, max: %.2fms, avg: %.2fms ]%n",
                    function.getModule().getName(),
                    function.getName(),
                    samples.size(),
                    summary.getMin() / 1e6D,
                    summary.getMax() / 1e6D,
                    summary.getAverage() / 1e6D);
            }
        }
    }
}
