package net.openhft.chronicle.core.internal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public final class CpuClass {
    static final String CPU_MODEL;

    private static final String PROCESS = "process ";

    static {
        String model = System.getProperty("os.arch", "unknown");
        Logger logger = LoggerFactory.getLogger(CpuClass.class);

        try {
            final Path path = Paths.get("/proc/cpuinfo");
            if (Files.isReadable(path)) {
                model = Files.lines(path)
                        .filter(line -> line.startsWith("model name"))
                        .map(removingTag())
                        .findFirst().orElse(model);
            } else if (Bootstrap.IS_WIN) {
                String cmd = "wmic cpu get name";
                Process process = new ProcessBuilder(cmd.split(" "))
                        .redirectErrorStream(true)
                        .start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    model = reader.lines()
                            .map(String::trim)
                            .filter(s -> !"Name".equals(s) && !s.isEmpty())
                            .findFirst().orElse(model);
                }
                try {
                    int ret = process.waitFor();
                    if (ret != 0)
                        logger.warn(PROCESS + cmd + " returned " + ret);
                } catch (InterruptedException e) {
                    logger.warn(PROCESS + cmd + " waitFor threw ", e);
                    // Restore the interrupt state...
                    Thread.currentThread().interrupt();
                }
                process.destroy();

            } else if (Bootstrap.IS_MAC) {

                String cmd = "sysctl -a";
                Process process = new ProcessBuilder(cmd.split(" "))
                        .redirectErrorStream(true)
                        .start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    model = reader.lines()
                            .map(String::trim)
                            .filter(s -> s.startsWith("machdep.cpu.brand_string"))
                            .map(removingTag())
                            .findFirst().orElse(model);
                }
                try {
                    int ret = process.waitFor();
                    if (ret != 0)
                        logger.warn(PROCESS + cmd + " returned " + ret);
                } catch (InterruptedException e) {
                    logger.warn(PROCESS + cmd + " waitFor threw ", e);
                    // Restore the interrupt state...
                    Thread.currentThread().interrupt();
                }
                process.destroy();

            }

        } catch (IOException e) {
            logger.debug("Unable to read cpuinfo", e);
        }
        CPU_MODEL = model;
    }

    // Suppresses default constructor, ensuring non-instantiability.
    private CpuClass() {
    }

    public static String getCpuModel() {
        return CPU_MODEL;
    }

    @SuppressWarnings("java:S5852") // Possessive quantifiers (*+) are used preventing catastrophic backtracking
    @NotNull
    static Function<String, String> removingTag() {
        return line -> line.replaceFirst("[^:]*+: ", "");
    }
}
