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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.Runtime.getRuntime;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;

/**
 * Contains essential static properties and methods for bootstrapping the application.
 * <p>
 * This class provides various system properties related to the Java Virtual Machine (JVM) and the operating system.
 * It handles checks for architecture, vendor, and version of the JVM and determines if the JVM is running on
 * specific platforms such as 64-bit, ARM, or Azul Zing/Zulu.
 * </p>
 * <p>
 * Additionally, the class determines the major version of the JVM, checks if assertions are enabled,
 * and identifies the process ID (PID) of the running Java process.
 * </p>
 * <p>
 * This class cannot be instantiated and is intended for early initialization of system-level properties.
 * </p>
 */
public final class Bootstrap {

    // JVM and system properties
    public static final String OS_ARCH = System.getProperty("os.arch", "?");
    public static final String VM_VENDOR = System.getProperty("java.vm.vendor", "?");
    public static final String VM_VERSION = System.getProperty("java.vm.version", "?");
    public static final String VM_NAME = System.getProperty("java.vm.name", "?");

    // System architecture check
    public static final boolean IS_64BIT = is64bit0();
    public static final boolean IS_AZUL_ZING = Bootstrap.isAzulZing0();
    public static final boolean IS_AZUL_ZULU = Bootstrap.isAzulZulu0();
    public static final boolean ASSERT_ENABLED;

    // System-specific paths and process information
    public static final String PROC_SELF = "/proc/self";
    public static final int PROCESS_ID = getProcessId0();

    // JVM version checks
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

    // Static block to initialize some values during class loading
    static {

        JVM_JAVA_MAJOR_VERSION = Bootstrap.getMajorVersion0();
        IS_JAVA_9_PLUS = JVM_JAVA_MAJOR_VERSION >= 9; // IS_JAVA_9_PLUS value is used in maxDirectMemory0 method.
        IS_JAVA_12_PLUS = JVM_JAVA_MAJOR_VERSION >= 12;
        IS_JAVA_14_PLUS = JVM_JAVA_MAJOR_VERSION >= 14;
        IS_JAVA_15_PLUS = JVM_JAVA_MAJOR_VERSION >= 15;
        IS_JAVA_19_PLUS = JVM_JAVA_MAJOR_VERSION >= 19;
        IS_JAVA_20_PLUS = JVM_JAVA_MAJOR_VERSION >= 20;
        IS_JAVA_21_PLUS = JVM_JAVA_MAJOR_VERSION >= 21;

        // Check if assertions are enabled
        boolean assertEnabled = false;
        assert assertEnabled = true;
        ASSERT_ENABLED = assertEnabled;
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private Bootstrap() {
    }

    /**
     * Checks if the system is running on an ARM architecture.
     *
     * @return {@code true} if the system architecture is ARM, {@code false} otherwise.
     */
    public static boolean isArm0() {
        return Boolean.parseBoolean(System.getProperty("jvm.isarm"))
                || OS_ARCH.startsWith("arm")
                || OS_ARCH.startsWith("aarch")
                || isMacArm0();
    }

    /**
     * Checks if the system is running on an ARM-based Mac.
     *
     * @return {@code true} if the system is a Mac with ARM architecture, {@code false} otherwise.
     */
    public static boolean isMacArm0() {
        return IS_MAC && !OS_ARCH.equals("x86_64");
    }

    /**
     * Checks if the JVM is Azul Zing.
     *
     * @return {@code true} if the JVM is Azul Zing, {@code false} otherwise.
     */
    public static boolean isAzulZing0() {
        return VM_VENDOR.startsWith("Azul ") && VM_VERSION.contains("zing");
    }

    /**
     * Checks if the JVM is Azul Zulu.
     *
     * @return {@code true} if the JVM is Azul Zulu, {@code false} otherwise.
     */
    public static boolean isAzulZulu0() {
        return VM_VENDOR.startsWith("Azul ") && (VM_NAME.startsWith("OpenJDK ") || VM_NAME.startsWith("Zulu"));
    }

    /**
     * Returns the major version of the JVM.
     *
     * @return The major version of the JVM.
     */
    public static int getJvmJavaMajorVersion() {
        return JVM_JAVA_MAJOR_VERSION;
    }

    /**
     * Checks if the JVM is version 9 or later.
     *
     * @return {@code true} if the JVM is version 9 or later, {@code false} otherwise.
     */
    public static boolean isJava9Plus() {
        return IS_JAVA_9_PLUS;
    }

    /**
     * Checks if the JVM is version 12 or later.
     *
     * @return {@code true} if the JVM is version 12 or later, {@code false} otherwise.
     */
    public static boolean isJava12Plus() {
        return IS_JAVA_12_PLUS;
    }

    /**
     * Checks if the JVM is version 14 or later.
     *
     * @return {@code true} if the JVM is version 14 or later, {@code false} otherwise.
     */
    public static boolean isJava14Plus() {
        return IS_JAVA_14_PLUS;
    }

    /**
     * Checks if the JVM is version 15 or later.
     *
     * @return {@code true} if the JVM is version 15 or later, {@code false} otherwise.
     */
    public static boolean isJava15Plus() {
        return IS_JAVA_15_PLUS;
    }

    /**
     * Checks if the JVM is version 19 or later.
     *
     * @return {@code true} if the JVM is version 19 or later, {@code false} otherwise.
     */
    public static boolean isJava19Plus() {
        return IS_JAVA_19_PLUS;
    }

    /**
     * Checks if the JVM is version 20 or later.
     *
     * @return {@code true} if the JVM is version 20 or later, {@code false} otherwise.
     */
    public static boolean isJava20Plus() {
        return IS_JAVA_20_PLUS;
    }

    /**
     * Checks if the JVM is version 21 or later.
     *
     * @return {@code true} if the JVM is version 21 or later, {@code false} otherwise.
     */
    public static boolean isJava21Plus() {
        return IS_JAVA_21_PLUS;
    }

    /**
     * Determines the major version of the JVM.
     *
     * @return The major version number of the JVM.
     */
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

    /**
     * Checks if the system is 64-bit based on system properties.
     *
     * @return {@code true} if the system is 64-bit, {@code false} otherwise.
     */
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

    /**
     * Gets the process ID (PID) of the current Java process.
     *
     * @return The process ID of the running JVM.
     */
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

        // Fallback to default PID if it cannot be determined
        int rpid = 1;
        System.err.println(Bootstrap.class.getName() + ": Unable to determine PID, picked 1 as a PID");
        return rpid;
    }
}
