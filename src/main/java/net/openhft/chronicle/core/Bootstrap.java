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

package net.openhft.chronicle.core;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import static java.lang.Runtime.getRuntime;

/**
 * Contains the pieces which must be loaded first
 */
final class Bootstrap {

    // Suppresses default constructor, ensuring non-instantiability.

    public static final String OS_NAME = System.getProperty("os.name", "?");
    public static final String OS_ARCH = System.getProperty("os.arch", "?");
    public static final String VM_VENDOR = System.getProperty("java.vm.vendor", "?");
    public static final String VM_VERSION = System.getProperty("java.vm.version", "?");
    public static final String VM_NAME = System.getProperty("java.vm.name", "?");
    static final int JVM_JAVA_MAJOR_VERSION;
    static final boolean IS_JAVA_9_PLUS;
    static final boolean IS_JAVA_12_PLUS;
    static final boolean IS_JAVA_14_PLUS;
    static final boolean IS_JAVA_15_PLUS;
    static final boolean IS_JAVA_19_PLUS;

    static {
        JVM_JAVA_MAJOR_VERSION = Bootstrap.getMajorVersion0();
        IS_JAVA_9_PLUS = JVM_JAVA_MAJOR_VERSION > 8; // IS_JAVA_9_PLUS value is used in maxDirectMemory0 method.
        IS_JAVA_12_PLUS = JVM_JAVA_MAJOR_VERSION > 11;
        IS_JAVA_14_PLUS = JVM_JAVA_MAJOR_VERSION > 13;
        IS_JAVA_15_PLUS = JVM_JAVA_MAJOR_VERSION > 14;
        IS_JAVA_19_PLUS = JVM_JAVA_MAJOR_VERSION > 18;
    }

    private Bootstrap() {
    }

    // can't be in Jvm or causes a problem on initialisation.
    static boolean isArm0() {
        return Boolean.parseBoolean(System.getProperty("jvm.isarm"))
                || OS_ARCH.startsWith("arm")
                || OS_ARCH.startsWith("aarch")
                || isMacArm0();
    }

    static boolean isMacArm0() {
        return OS_NAME.equals("Mac OS X") && !OS_ARCH.equals("x86_64");
    }

    static boolean isAzulZing0() {
        return VM_VENDOR.startsWith("Azul ") && VM_VERSION.contains("zing");
    }

    static boolean isAzulZulu0() {
        return VM_VENDOR.startsWith("Azul ") && (VM_NAME.startsWith("OpenJDK ") || VM_NAME.startsWith("Zulu"));
    }

    private static int getMajorVersion0() {
        try {
            final Method method = Runtime.class.getDeclaredMethod("version");
            if (method != null) {
                final Object version = method.invoke(getRuntime());
                final Class<?> clz = Class.forName("java.lang.Runtime$Version");
                return (Integer) clz.getDeclaredMethod("major").invoke(version);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | IllegalArgumentException e) {
            // ignore and fall back to pre-jdk9
        }
        try {
            return Integer.parseInt(Runtime.class.getPackage().getSpecificationVersion().split("\\.")[1]);
        } catch (NumberFormatException nfe) {
            Jvm.warn().on(Jvm.class, "Unable to get the major version, defaulting to 8 " + nfe);
            return 8;
        }
    }

    static final class CpuClass {
        static final String CPU_MODEL;

        private static final String PROCESS = "process ";

        static {
            String model = Jvm.getProperty("os.arch", "unknown");
            try {
                final Path path = Paths.get("/proc/cpuinfo");
                if (Files.isReadable(path)) {
                    model = Files.lines(path)
                            .filter(line -> line.startsWith("model name"))
                            .map(removingTag())
                            .findFirst().orElse(model);
                } else if (OS.isWindows()) {
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
                            Jvm.warn().on(CpuClass.class, PROCESS + cmd + " returned " + ret);
                    } catch (InterruptedException e) {
                        Jvm.warn().on(CpuClass.class, PROCESS + cmd + " waitFor threw ", e);
                        // Restore the interrupt state...
                        Thread.currentThread().interrupt();
                    }
                    process.destroy();

                } else if (OS.isMacOSX()) {

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
                            Jvm.warn().on(CpuClass.class, PROCESS + cmd + " returned " + ret);
                    } catch (InterruptedException e) {
                        Jvm.warn().on(CpuClass.class, PROCESS + cmd + " waitFor threw ", e);
                        // Restore the interrupt state...
                        Thread.currentThread().interrupt();
                    }
                    process.destroy();

                }

            } catch (IOException e) {
                Jvm.debug().on(CpuClass.class, "Unable to read cpuinfo", e);
            }
            CPU_MODEL = model;
        }

        // Suppresses default constructor, ensuring non-instantiability.
        private CpuClass() {
        }

        @SuppressWarnings("java:S5852") // Possessive quantifiers (*+) are used preventing catastrophic backtracking
        @NotNull
        static Function<String, String> removingTag() {
            return line -> line.replaceFirst("[^:]*+: ", "");
        }
    }
}
