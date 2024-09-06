/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Utility class to detect and provide information about the CPU model.
 * <p>
 * This class detects the CPU model based on the underlying operating system.
 * It reads the CPU model from various system files or commands depending on whether the OS is Linux, Windows, or macOS.
 * The result is stored in a static final variable {@code CPU_MODEL} and can be retrieved using the {@link #getCpuModel()} method.
 * </p>
 * <p>
 * This class cannot be instantiated and provides static utility methods for CPU model detection.
 * </p>
 */
public final class CpuClass {

    /**
     * The detected CPU model string.
     * <p>
     * This value is determined during static initialization and can vary depending on the underlying operating system.
     * If the CPU model cannot be determined, the default value is the system architecture.
     * </p>
     */
    static final String CPU_MODEL;

    // Log prefix for process execution
    private static final String PROCESS = "process ";

    static {
        // Default model based on system architecture
        String model = System.getProperty("os.arch", "unknown");
        Logger logger = LoggerFactory.getLogger(CpuClass.class);

        try {
            final Path path = Paths.get("/proc/cpuinfo");
            // Check if running on Linux and if /proc/cpuinfo is readable
            if (Files.isReadable(path)) {
                // Read CPU model name from /proc/cpuinfo
                model = Files.lines(path)
                        .filter(line -> line.startsWith("model name"))
                        .map(removingTag()) // Remove the "model name:" tag
                        .findFirst().orElse(model);

            } else if (Bootstrap.IS_WIN) {
                // On Windows, run WMIC command to get CPU name
                String cmd = "wmic cpu get name";
                Process process = new ProcessBuilder(cmd.split(" "))
                        .redirectErrorStream(true)
                        .start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    // Read and process CPU model from the command output
                    model = reader.lines()
                            .map(String::trim)
                            .filter(s -> !"Name".equals(s) && !s.isEmpty())
                            .findFirst().orElse(model);
                }

                // Wait for the process to complete
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
                // On macOS, run the sysctl command to get CPU information
                String cmd = "sysctl -a";
                Process process = new ProcessBuilder(cmd.split(" "))
                        .redirectErrorStream(true)
                        .start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    // Read and process CPU model from the sysctl command output
                    model = reader.lines()
                            .map(String::trim)
                            .filter(s -> s.startsWith("machdep.cpu.brand_string")) // Filter for the CPU brand string
                            .map(removingTag()) // Remove the "machdep.cpu.brand_string:" tag
                            .findFirst().orElse(model);
                }

                // Wait for the process to complete
                try {
                    int ret = process.waitFor();
                    if (ret != 0)
                        logger.warn(PROCESS + cmd + " returned " + ret);
                } catch (InterruptedException e) {
                    logger.warn(PROCESS + cmd + " waitFor threw ", e);
                    // Restore the interrupt state
                    Thread.currentThread().interrupt();
                }
                process.destroy();

            }

        } catch (IOException e) {
            // Log an error if unable to read CPU info
            logger.debug("Unable to read cpuinfo", e);
        }

        // Assign the detected or default CPU model to the static field
        CPU_MODEL = model;
    }

    // Suppresses default constructor, ensuring non-instantiability.
    private CpuClass() {
    }

    /**
     * Retrieves the detected CPU model string.
     *
     * @return The detected CPU model or the default system architecture if the model could not be determined.
     */
    public static String getCpuModel() {
        return CPU_MODEL;
    }

    /**
     * Utility method to remove the tag portion from lines in CPU info, such as "model name: ".
     *
     * @return A function that removes everything up to and including the first colon and space.
     */
    @SuppressWarnings("java:S5852") // Possessive quantifiers (*+) are used preventing catastrophic backtracking
    @NotNull
    static Function<String, String> removingTag() {
        return line -> line.replaceFirst("[^:]*+: ", "");
    }
}
