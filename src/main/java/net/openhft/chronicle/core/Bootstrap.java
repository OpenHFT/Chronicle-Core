package net.openhft.chronicle.core;

import net.openhft.posix.PosixAPI;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.Runtime.getRuntime;

/**
 * Contains the pieces which must be loaded first
 */
final class Bootstrap {

    // Suppresses default constructor, ensuring non-instantiability.
    private Bootstrap() {
    }

    public static final String OS_ARCH = System.getProperty("os.arch", "?");
    public static final String VM_VENDOR = System.getProperty("java.vm.vendor", "?");
    public static final String VM_VERSION = System.getProperty("java.vm.version", "?");
    public static final String VM_NAME = System.getProperty("java.vm.name", "?");

    static final int JVM_JAVA_MAJOR_VERSION;
    static final boolean IS_JAVA_9_PLUS;
    static final boolean IS_JAVA_12_PLUS;
    static final boolean IS_JAVA_14_PLUS;
    static final boolean IS_JAVA_15_PLUS;

    static {
        // Eagerly initialise Posix & Affinity
        PosixAPI.posix();
        try {
            Class.forName("net.openhft.affinity.Affinity");
        } catch (ClassNotFoundException e) {
            // Ignore, Affinity is an optional dependency
        }

        JVM_JAVA_MAJOR_VERSION = Bootstrap.getMajorVersion0();
        IS_JAVA_9_PLUS = JVM_JAVA_MAJOR_VERSION > 8; // IS_JAVA_9_PLUS value is used in maxDirectMemory0 method.
        IS_JAVA_12_PLUS = JVM_JAVA_MAJOR_VERSION > 11;
        IS_JAVA_14_PLUS = JVM_JAVA_MAJOR_VERSION > 13;
        IS_JAVA_15_PLUS = JVM_JAVA_MAJOR_VERSION > 14;
    }

    // can't be in Jvm or causes a problem on initialisation.
    static boolean isArm0() {
        return Boolean.parseBoolean(System.getProperty("jvm.isarm")) ||
                OS_ARCH.startsWith("arm") || OS_ARCH.startsWith("aarch");
    }

    static boolean isMacArm0() {
        return System.getProperty("os.name", "?").equals("Mac OS X")
                && OS_ARCH.equals("aarch64");
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
}
