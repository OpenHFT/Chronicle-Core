/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static java.lang.Runtime.getRuntime;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;

/**
 * Holds internal bootstrap wiring that must load before platform checks and flags are used.
 */
public final class Bootstrap {

    public static final String OS_ARCH = System.getProperty("os.arch", "?");
    public static final String VM_VENDOR = System.getProperty("java.vm.vendor", "?");
    public static final String VM_VERSION = System.getProperty("java.vm.version", "?");
    public static final String VM_NAME = System.getProperty("java.vm.name", "?");
    public static final boolean IS_64BIT = is64bit0();
    public static final boolean IS_AZUL_ZING = Bootstrap.isAzulZing0();
    public static final boolean IS_AZUL_ZULU = Bootstrap.isAzulZulu0();
    public static final boolean ASSERT_ENABLED;
    public static final String PROC_SELF = "/proc/self";
    public static final int PROCESS_ID = getProcessId0();
    static final int JVM_JAVA_MAJOR_VERSION;
    static final boolean IS_JAVA_9_PLUS, IS_JAVA_12_PLUS, IS_JAVA_14_PLUS, IS_JAVA_15_PLUS;
    static final boolean IS_JAVA_19_PLUS, IS_JAVA_20_PLUS, IS_JAVA_21_PLUS;
    // Suppresses default constructor, ensuring non-instantiability.
    private static final String OS_NAME = System.getProperty("os.name");
    private static final String LOWER_OS_NAME = OS_NAME.toLowerCase();
    public static final boolean IS_WIN10 = LOWER_OS_NAME.equals("windows 10");
    public static final boolean IS_WIN = LOWER_OS_NAME.startsWith("win");
    public static final boolean IS_MAC = LOWER_OS_NAME.contains("mac");
    public static final boolean IS_ARM = Bootstrap.isArm0();
    public static final boolean IS_MAC_ARM = Bootstrap.isMacArm0();
    public static final boolean IS_LINUX = LOWER_OS_NAME.startsWith("linux");
    public static final boolean IS_WSL = isWsl0();

    static {

        JVM_JAVA_MAJOR_VERSION = Bootstrap.getMajorVersion0();
        IS_JAVA_9_PLUS = JVM_JAVA_MAJOR_VERSION >= 9; // IS_JAVA_9_PLUS value is used in maxDirectMemory0 method.
        IS_JAVA_12_PLUS = JVM_JAVA_MAJOR_VERSION >= 12;
        IS_JAVA_14_PLUS = JVM_JAVA_MAJOR_VERSION >= 14;
        IS_JAVA_15_PLUS = JVM_JAVA_MAJOR_VERSION >= 15;
        IS_JAVA_19_PLUS = JVM_JAVA_MAJOR_VERSION >= 19;
        IS_JAVA_20_PLUS = JVM_JAVA_MAJOR_VERSION >= 20;
        IS_JAVA_21_PLUS = JVM_JAVA_MAJOR_VERSION >= 21;

        ASSERT_ENABLED = Bootstrap.class.desiredAssertionStatus();

        ChronicleGuarding.bootstrap();
    }

    private Bootstrap() {
    }

    // can't be in Jvm or causes a problem on initialisation.
    public static boolean isArm0() {
        return Boolean.parseBoolean(System.getProperty("jvm.isarm"))
                || OS_ARCH.startsWith("arm")
                || OS_ARCH.startsWith("aarch")
                || isMacArm0();
    }

    public static boolean isMacArm0() {
        return IS_MAC && !OS_ARCH.equals("x86_64");
    }

    public static boolean isAzulZing0() {
        return VM_VENDOR.startsWith("Azul ") && VM_VERSION.contains("zing");
    }

    public static boolean isAzulZulu0() {
        return VM_VENDOR.startsWith("Azul ") && (VM_NAME.startsWith("OpenJDK ") || VM_NAME.startsWith("Zulu"));
    }

    private static boolean isWsl0() {
        if (!LOWER_OS_NAME.startsWith("linux")) {
            return false;
        }
        if (isEnvSet("WSL_DISTRO_NAME") || isEnvSet("WSL_INTEROP") || isEnvSet("WSLENV")) {
            return true;
        }
        return fileContainsIgnoreCase("/proc/sys/kernel/osrelease", "microsoft")
                || fileContainsIgnoreCase("/proc/version", "microsoft");
    }

    private static boolean isEnvSet(String name) {
        String value = System.getenv(name);
        return value != null && !value.isEmpty();
    }

    private static boolean fileContainsIgnoreCase(String path, String token) {
        File file = new File(path);
        if (!file.canRead()) {
            return false;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(token)) {
                    return true;
                }
            }
        } catch (IOException ignored) {
            // ignore
        }
        return false;
    }

    public static int getJvmJavaMajorVersion() {
        return JVM_JAVA_MAJOR_VERSION;
    }

    public static boolean isJava9Plus() {
        return IS_JAVA_9_PLUS;
    }

    public static boolean isJava12Plus() {
        return IS_JAVA_12_PLUS;
    }

    public static boolean isJava14Plus() {
        return IS_JAVA_14_PLUS;
    }

    public static boolean isJava15Plus() {
        return IS_JAVA_15_PLUS;
    }

    public static boolean isJava19Plus() {
        return IS_JAVA_19_PLUS;
    }

    public static boolean isJava20Plus() {
        return IS_JAVA_20_PLUS;
    }

    public static boolean isJava21Plus() {
        return IS_JAVA_21_PLUS;
    }

    private static int getMajorVersion0() {
        try {
            final Method method = Runtime.class.getDeclaredMethod("version");
            final Object version = method.invoke(getRuntime());
            final Class<?> clz = Class.forName("java.lang.Runtime$Version");
            return (Integer) clz.getDeclaredMethod("major").invoke(version);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException |
                 IllegalArgumentException e) {
            // ignore and fall back to pre-jdk9
        }
        try {
            return Integer.parseInt(Runtime.class.getPackage().getSpecificationVersion().split("\\.")[1]);
        } catch (Exception e) {
            System.err.println("Unable to get the major version, defaulting to 8 " + e);
            return 8;
        }
    }

    public static boolean is64bit0() {
        String systemProp;
        systemProp = System.getProperty("com.ibm.vm.bitmode");
        if (systemProp != null) {
            return "64".equals(systemProp);
        }
        systemProp = System.getProperty("sun.arch.data.model");
        if (systemProp != null) {
            return "64".equals(systemProp);
        }
        systemProp = System.getProperty("java.vm.version");
        return systemProp != null && systemProp.contains("_64");
    }

    private static int getProcessId0() {
        String pid = null;
        final File self = new File(PROC_SELF);
        try {
            if (self.exists()) {
                pid = self.getCanonicalFile().getName();
            }
        } catch (IOException ignored) {
            // Ignore
        }

        if (pid == null) {
            pid = getRuntimeMXBean().getName().split("@", 0)[0];
        }

        if (pid != null) {
            try {
                return Integer.parseInt(pid);
            } catch (NumberFormatException nfe) {
                // ignore
            }
        }

        int rpid = 1;
        System.err.println(Bootstrap.class.getName() + ": Unable to determine PID, picked 1 as a PID");
        return rpid;
    }

    @SuppressWarnings("unchecked")
    public static <T> T uncheckedCast(Object obj) {
        return (T) obj;
    }
}
