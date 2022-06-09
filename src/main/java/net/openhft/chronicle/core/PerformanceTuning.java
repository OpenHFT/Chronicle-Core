package net.openhft.chronicle.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static net.openhft.chronicle.core.Jvm.rethrow;

/**
 * This class is responsible for OS performance tuning diagnostic.
 */
public final class PerformanceTuning {
    private PerformanceTuning() {
    }

    /**
     * Report OS performance tuning issues.
     */
    public static void reportIssues() {
        List<String> issues = issues();
        if (!issues.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("OS configuration is non-optimal for high performance:").append("\n");
            issues.forEach(issue -> msg.append(issue).append("\n"));
            msg.append("If you would like assistance, please contact sales@chroncle.software.");
            Jvm.warn().on(PerformanceTuning.class, msg.toString());
        }
    }

    /**
     * @return list of issues regarding OS performance tuning.
     */
    @NotNull
    public static List<String> issues() {
        if (!OS.isLinux()) {
            return Collections.emptyList();
        }

        List<String> issues = new LinkedList<>();

        issues.add(checkScalingGovernors());

        Set<String> kernelCmdLineParams = kernelCommandLineParameters();
        if (!kernelCmdLineParams.isEmpty()) {
            issues.addAll(asList(
                    checkCStates(kernelCmdLineParams),
                    checkSpectreMitigation(kernelCmdLineParams),
                    checkSoftlockup(kernelCmdLineParams),
                    checkAudit(kernelCmdLineParams),
                    checkPageTableIsolation(kernelCmdLineParams),
                    checkMCE(kernelCmdLineParams)
            ));
        }

        return issues.stream().filter(Objects::nonNull).collect(toList());
    }

    @Nullable
    public static String checkScalingGovernors() {
        return checkScalingGovernors(Paths.get("/sys/devices/system/cpu/"));
    }

    @Nullable
    static String checkScalingGovernors(Path sysDevicesSystemCpu) {
        try (Stream<Path> pathsToCpus = Files.list(sysDevicesSystemCpu)) {
            List<String> cpusWithSlowGovernors = pathsToCpus
                    .filter(pathToCpu -> pathToCpu.getFileName().toString().startsWith("cpu"))
                    .filter(pathToCpu -> !pathToCpu.toString().endsWith("idle"))
                    .filter(pathToCpu -> {
                        Path pathToGovernor = pathToCpu.resolve("cpufreq/scaling_governor");
                        try {
                            String scalingGovernor = readFirstLine(pathToGovernor);
                            if (scalingGovernor == null) {
                                throw new IllegalStateException(
                                        "Failed to read 'scaling_governor' value for " + pathToCpu.getFileName());
                            }
                            return !"performance".equals(scalingGovernor);
                        } catch (IOException e) {
                            rethrow(e);
                            return false;
                        }
                    })
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .sorted()
                    .collect(toList());

            if (cpusWithSlowGovernors.isEmpty()) {
                return null;
            } else {
                return cpusWithSlowGovernors.stream().collect(joining(", ", "Following CPUs have non-performant scaling governor setting: ", "."));
            }
        } catch (IOException e) {
            Jvm.debug().on(PerformanceTuning.class, "Unable to read " + sysDevicesSystemCpu + "/cpu*/cpufreq/scaling_governor", e);
        }

        return null;
    }

    @Nullable
    static String checkSpectreMitigation(Set<String> kernelCmdLineParams) {
        if (!kernelCmdLineParams.contains("nospectre_v2")
                && !kernelCmdLineParams.contains("spectre_v2=off")
                // Let's check if all mitigations are disabled, to avoid false positives
                && !kernelCmdLineParams.contains("mitigations=off")) {
            return "Spectre variant 2 mitigation is enabled";
        }

        return null;
    }

    @Nullable
    static String checkCStates(Set<String> kernelCmdLineParams) {
        if (!kernelCmdLineParams.containsAll(asList(
                "processor.max_cstate=1", "intel_idle.max_cstate=0", "idle=poll"))) {
            return "C-States are enabled";
        }

        return null;
    }

    @Nullable
    static String checkMCE(Set<String> kernelCmdLineParams) {
        if (!kernelCmdLineParams.contains("mce=ignore_ce")) {
            return "Machine Check Exception configuration is non-optimal";
        }

        return null;
    }

    @Nullable
    static String checkSoftlockup(Set<String> kernelCmdLineParams) {
        if (!kernelCmdLineParams.contains("nosoftlockup")) {
            return "Soft-lockup detector is enabled";
        }

        return null;
    }

    @Nullable
    static String checkAudit(Set<String> kernelCmdLineParams) {
        if (!kernelCmdLineParams.contains("audit=0") && !kernelCmdLineParams.contains("audit=off")) {
            return "Audit sub-system is enabled";
        }

        return null;
    }

    @Nullable
    static String checkPageTableIsolation(Set<String> kernelCmdLineParams) {
        if (!kernelCmdLineParams.contains("nopti") && !kernelCmdLineParams.contains("pti=off")) {
            return "Page Table Isolation is enabled";
        }

        return null;
    }

    @NotNull
    static Set<String> kernelCommandLineParameters() {
        return kernelCommandLineParameters(Paths.get("/proc/cmdline"));
    }

    @NotNull
    static Set<String> kernelCommandLineParameters(Path procCmdlinePath) {
        try {
            String procCmdline = readFirstLine(procCmdlinePath);
            if (procCmdline != null) {
                return parseKernelCommandLineParameters(procCmdline);
            }
        } catch (IOException e) {
            Jvm.debug().on(PerformanceTuning.class, "Failed to read " + procCmdlinePath, e);
        }

        return Collections.emptySet();
    }

    @NotNull
    static Set<String> parseKernelCommandLineParameters(String procCmdline) {
        return new HashSet<>(asList(procCmdline.split(" ")));
    }

    static String readFirstLine(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return reader.readLine();
        }
    }
}
