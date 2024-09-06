/*
 * Copyright 2016-2020 chronicle.software
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

import net.openhft.chronicle.core.annotation.DontChain;
import net.openhft.chronicle.core.internal.*;
import net.openhft.chronicle.core.internal.util.DirectBufferUtil;
import net.openhft.chronicle.core.onoes.*;
import net.openhft.chronicle.core.util.ClassMetrics;
import net.openhft.chronicle.core.util.ObjectUtils;
import net.openhft.chronicle.core.util.ThrowingSupplier;
import net.openhft.posix.PosixAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.Unsafe;
import sun.nio.ch.Interruptible;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static java.lang.Runtime.getRuntime;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.util.stream.Collectors.toList;
import static net.openhft.chronicle.core.OS.*;
import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static net.openhft.chronicle.core.internal.Bootstrap.*;
import static net.openhft.chronicle.core.internal.util.MapUtil.entry;
import static net.openhft.chronicle.core.internal.util.MapUtil.ofUnmodifiable;

/**
 * Utility class to access information and perform operations related to the JVM.
 * This class provides methods to query JVM properties, manage memory, handle exceptions,
 * and work with various JVM-specific features.
 */
public final class Jvm {

    // Constant for the system property "java.class.path"
    public static final String JAVA_CLASS_PATH = "java.class.path";

    // Constant for the system properties file "system.properties"
    public static final String SYSTEM_PROPERTIES = "system.properties";

    // Default exception handlers used for error, warning, performance, and debug messages
    private static final ExceptionHandler DEFAULT_ERROR_EXCEPTION_HANDLER = Slf4jExceptionHandler.ERROR;
    private static final ExceptionHandler DEFAULT_WARN_EXCEPTION_HANDLER = Slf4jExceptionHandler.WARN;
    private static final ExceptionHandler DEFAULT_PERF_EXCEPTION_HANDLER = Slf4jExceptionHandler.PERF;
    private static final ExceptionHandler DEFAULT_DEBUG_EXCEPTION_HANDLER = Slf4jExceptionHandler.DEBUG;

    // Path to the proc file system, used for querying system information in Unix-like operating systems
    private static final String PROC = "/proc";

    // List of JVM input arguments, typically including options passed to the JVM at startup
    private static final List<String> INPUT_ARGUMENTS = getRuntimeMXBean().getInputArguments();

    // String representation of JVM input arguments for easy access
    private static final String INPUT_ARGUMENTS2 = " " + String.join(" ", INPUT_ARGUMENTS);

    // Debugging flag, determines if debugging information should be printed based on JVM arguments
    private static final boolean IS_DEBUG = Jvm.getBoolean("debug", INPUT_ARGUMENTS2.contains("jdwp"));

    // Flag to check if Flight Recorder (JFR) is enabled, typically for profiling and diagnostics
    // e.g-verbose:gc  -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=dumponexit=true,filename=myrecording.jfr,settings=profile -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints
    private static final boolean IS_FLIGHT_RECORDER = Jvm.getBoolean("jfr", INPUT_ARGUMENTS2.contains(" -XX:+FlightRecorder"));

    // Flag indicating if code coverage tools are enabled, based on JVM arguments
    private static final boolean IS_COVERAGE = INPUT_ARGUMENTS2.contains("coverage");

    // Compile threshold for Just-In-Time (JIT) compilation, determining when methods are compiled
    private static final int COMPILE_THRESHOLD = getCompileThreshold0();

    // Flag to report unoptimized code, typically used in debugging or profiling scenarios
    private static final boolean REPORT_UNOPTIMISED;

    // Supplier for reserved memory, used for managing off-heap memory allocations
    private static final Supplier<Long> reservedMemory;

    // Flag to disable debug information, often used to minimize logging in production environments
    private static final boolean DISABLE_DEBUG = Jvm.getBoolean("disable.debug.info");

    // Thread-localized exception handlers for managing error, warning, performance, and debug exceptions
    @NotNull
    private static final ThreadLocalisedExceptionHandler ERROR = new ThreadLocalisedExceptionHandler(DEFAULT_ERROR_EXCEPTION_HANDLER);
    @NotNull
    private static final ThreadLocalisedExceptionHandler WARN = new ThreadLocalisedExceptionHandler(DEFAULT_WARN_EXCEPTION_HANDLER);
    @NotNull
    private static final ThreadLocalisedExceptionHandler PERF = new ThreadLocalisedExceptionHandler(DEFAULT_PERF_EXCEPTION_HANDLER);
    @NotNull
    private static final ExceptionHandler DEBUG;

    // Maximum direct memory available to the JVM, typically set via JVM options
    private static final long MAX_DIRECT_MEMORY;

    // Flag to check if safepoints are enabled, which are specific points in program execution where thread states are consistent
    private static final boolean SAFEPOINT_ENABLED;

    // Map storing metrics for different classes, useful for tracking memory usage and performance
    private static final Map<Class<?>, ClassMetrics> CLASS_METRICS_MAP = new ConcurrentHashMap<>();

    // Map storing the size of primitive data types, useful for memory calculations and optimizations
    /**
     * Map storing the size of primitive data types, useful for memory calculations and optimizations.
     */
    private static final Map<Class<?>, Integer> PRIMITIVE_SIZE = ofUnmodifiable(
            entry(boolean.class, 1),                    // Size of boolean type
            entry(byte.class, Byte.BYTES),              // Size of byte type
            entry(char.class, Character.BYTES),         // Size of char type
            entry(short.class, Short.BYTES),            // Size of short type
            entry(int.class, Integer.BYTES),            // Size of int type
            entry(float.class, Float.BYTES),            // Size of float type
            entry(long.class, Long.BYTES),              // Size of long type
            entry(double.class, Double.BYTES)           // Size of double type
    );

    /**
     * Method handle for the `onSpinWait` method, which can be used to optimize spin-wait loops.
     */
    private static final MethodHandle onSpinWaitMH;

    /**
     * Global signal handler for managing operating system signals in a chained fashion.
     */
    private static final ChainedSignalHandler signalHandlerGlobal;

    /**
     * Flag indicating whether resource tracing is enabled, used for debugging resource management.
     */
    private static boolean RESOURCE_TRACING;

    /**
     * Boolean indicating whether the /proc directory exists, often used in Unix-like systems to check if certain features are available.
     */
    private static final boolean PROC_EXISTS = new File(PROC).exists();

    /**
     * A volatile reference to a thread, used to prevent the JVM from optimizing away certain code paths.
     */
    @SuppressWarnings("unused")
    private static volatile Thread s_blackHole;

    // Static initialization block to initialize various static fields and perform setup tasks.
    static {
        // Logger for logging messages related to Jvm operations
        Logger logger = LoggerFactory.getLogger(Jvm.class);

        // Check if the current environment is not a JUnit test
        if (!isJUnitTest0()) {
            // Eagerly initialize Posix & Affinity
            try {
                // Attempt to initialize Posix API
                PosixAPI.posix();
            } catch (Error e) {
                // Log debug message if unable to load PosixAPI
                logger.debug("Unable to load PosixAPI ", e);
            }

            try {
                // Attempt to load Affinity class
                Class.forName("net.openhft.affinity.Affinity");
            } catch (ClassNotFoundException e) {
                // Log trace message if unable to load Affinity
                logger.trace("Unable to load Affinity", e);
            }
        }

        // Set DEBUG handler based on the DISABLE_DEBUG flag
        if (DISABLE_DEBUG) {
            DEBUG = NullExceptionHandler.NOTHING; // Disable debug logging
        } else {
            DEBUG = new ThreadLocalisedExceptionHandler(DEFAULT_DEBUG_EXCEPTION_HANDLER); // Enable thread-local debug logging
        }

        // Initialize the maximum direct memory available to the JVM
        MAX_DIRECT_MEMORY = maxDirectMemory0();

        // Setup reserved memory management
        Supplier<Long> reservedMemoryGetter;
        try {
            // Attempt to retrieve the reserved memory field from the Bits class
            final Class<?> bitsClass = Class.forName("java.nio.Bits");
            final Field firstTry = getFieldOrNull(bitsClass, "reservedMemory");
            final Field f = firstTry != null ? firstTry : getField(bitsClass, "RESERVED_MEMORY");
            if (f.getType() == AtomicLong.class) {
                // If field type is AtomicLong, use it to get the reserved memory value
                AtomicLong reservedMemory = (AtomicLong) f.get(null);
                reservedMemoryGetter = reservedMemory::get;
            } else {
                // Otherwise, use a supplier to retrieve the reserved memory value
                reservedMemoryGetter = ThrowingSupplier.asSupplier(() -> f.getLong(null));
            }
        } catch (Exception e) {
            // Log error message if unable to determine the reservedMemory value
            System.err.println(Jvm.class.getName() + ": Unable to determine the reservedMemory value, will always report 0");
            reservedMemoryGetter = () -> 0L; // Default to 0 if reserved memory cannot be determined
        }
        reservedMemory = reservedMemoryGetter;

        // Initialize global signal handler
        signalHandlerGlobal = new ChainedSignalHandler();

        // Retrieve the method handle for the `onSpinWait` method
        onSpinWaitMH = getOnSpinWait();

        // Load system properties from the properties file
        findAndLoadSystemProperties();

        // Check if performance info logging should be disabled
        boolean disablePerfInfo = Jvm.getBoolean("disable.perf.info");
        if (disablePerfInfo)
            PERF.defaultHandler(NullExceptionHandler.NOTHING); // Disable performance info logging

        // Check if safepoint is enabled in JVM
        SAFEPOINT_ENABLED = Jvm.getBoolean("jvm.safepoint.enabled");

        // Check if resource tracing is enabled
        RESOURCE_TRACING = Jvm.getBoolean("jvm.resource.tracing");

        // Log messages based on various flags and conditions
        if (DISABLE_DEBUG)
            logger.info("-Ddisable.debug.info turned off debug logging");
        if (logger.isInfoEnabled())
            logger.info("Chronicle core loaded from " + Jvm.class.getProtectionDomain().getCodeSource().getLocation());
        if (RESOURCE_TRACING && !Jvm.getBoolean("disable.resource.warning"))
            logger.warn("Resource tracing is turned on. If you are performance testing or running in PROD you probably don't want this");

        // Set the flag for reporting unoptimized methods
        REPORT_UNOPTIMISED = Jvm.getBoolean("report.unoptimised");

        // Perform any post-initialization tasks
        ChronicleInit.postInit();
    }

    /**
     * Retrieves the method handle for the `onSpinWait` method, which can be used to optimize spin-wait loops.
     * This method first attempts to find the `onSpinWait` method available from Java 9 onwards. If not found,
     * it falls back to a custom `force` method in the `Safepoint` class.
     *
     * @return A MethodHandle pointing to the `onSpinWait` method or `force` method if available; null otherwise.
     */
    private static MethodHandle getOnSpinWait() {
        // Define the method type for a method returning void
        MethodType voidType = MethodType.methodType(void.class);
        // Lookup object for finding method handles
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            // Check if running on Java 9 or later
            if (isJava9Plus())
                // Try to find the onSpinWait method in the Thread class
                return lookup.findStatic(Thread.class, "onSpinWait", voidType);
        } catch (Exception ignored) {
            // Ignore any exceptions, proceed to the next try block
        }
        try {
            // Fallback to find the force method in the Safepoint class
            return lookup.findStatic(Safepoint.class, "force", voidType);
        } catch (Exception ignored) {
            // Ignore any exceptions and return null
        }
        return null;
    }

    /**
     * Suppresses default constructor, ensuring non-instantiability of this utility class.
     */
    private Jvm() {
        // Prevent instantiation
    }

    /**
     * Reports usage of unoptimized methods if the `REPORT_UNOPTIMISED` flag is set.
     * This method traverses the stack trace to find the caller's method and reports it as a warning.
     */
    public static void reportUnoptimised() {
        // Check if reporting unoptimized usage is enabled
        if (!REPORT_UNOPTIMISED)
            return;
        // Retrieve the current stack trace
        final StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        int i = 0;
        // Find the calling method within the stack trace
        while (i < stes.length)
            if (stes[i++].getMethodName().equals("reportUnoptimised"))
                break;
        // Continue searching for the class initializer method
        while (i < stes.length)
            if (stes[i++].getMethodName().equals("<clinit>"))
                break;

        // Log a warning about the usage of an unoptimized method
        Jvm.warn().on(Jvm.class, "Reporting usage of unoptimised method " + stes[i]);
    }

    /**
     * Searches and loads system properties from a file named `system.properties` or an alternative path.
     * The method attempts to find the properties file in the current directory or parent directory.
     */
    private static void findAndLoadSystemProperties() {
        // Retrieve the system properties file path from the JVM property
        String systemProperties = Jvm.getProperty(SYSTEM_PROPERTIES);
        boolean wasSet = true;
        if (systemProperties == null) {
            // Check if the system.properties file exists in the current or parent directory
            if (new File(SYSTEM_PROPERTIES).exists())
                systemProperties = SYSTEM_PROPERTIES;
            else if (new File("../" + SYSTEM_PROPERTIES).exists())
                systemProperties = "../" + SYSTEM_PROPERTIES;
            else {
                // Use default system properties if none are found
                systemProperties = SYSTEM_PROPERTIES;
                wasSet = false;
            }
        }
        // Load the system properties from the determined path
        loadSystemProperties(systemProperties, wasSet);
    }

    /**
     * Initializes the Jvm class by forcing static initialization of the ChronicleInit class.
     * This method is typically called at the start of an application to ensure proper setup.
     */
    public static void init() {
        // Force static initialization
        ChronicleInit.init();
    }

    /**
     * Loads system properties from the specified file, if available.
     * If the properties file does not exist, a warning or debug message is logged depending on the `wasSet` flag.
     *
     * @param name   The name of the properties file to load.
     * @param wasSet Indicates if the system properties were explicitly set by the user.
     */
    private static void loadSystemProperties(final String name, final boolean wasSet) {
        try {
            // Get the class loader for loading resources
            final ClassLoader classLoader = Jvm.class.getClassLoader();
            // Attempt to load the properties file from the classpath
            InputStream is0 = classLoader == null ? null : classLoader.getResourceAsStream(name);
            if (is0 == null) {
                // Fallback to loading the properties file from the file system
                File file = new File(name);
                if (file.exists())
                    is0 = new FileInputStream(file);
            }
            // Load the properties from the input stream if available
            try (InputStream is = is0) {
                if (is == null) {
                    // Log a message if the properties file is not found
                    (wasSet ? Slf4jExceptionHandler.WARN : Slf4jExceptionHandler.DEBUG)
                            .on(Jvm.class, "No " + name + " file found");

                } else {
                    // Load the properties and merge them with existing system properties
                    final Properties prop = new Properties();
                    prop.load(is);
                    // Prevent overwriting properties already set via -D
                    prop.forEach((o, o2) -> System.getProperties().putIfAbsent(o, o2));
                    // Log a debug message indicating properties loaded
                    Slf4jExceptionHandler.DEBUG.on(Jvm.class, "Loaded " + name + " with " + prop);
                }
            }
        } catch (Exception e) {
            // Log a warning if there is an error loading the properties file
            Slf4jExceptionHandler.WARN.on(Jvm.class, "Error loading " + name, e);
        }
    }

    /**
     * Retrieves the compile threshold for Just-In-Time (JIT) compilation from the JVM input arguments.
     * This threshold determines when methods are compiled by the JIT compiler.
     *
     * @return The compile threshold value; defaults to 10,000 if not specified in the JVM arguments.
     */
    private static int getCompileThreshold0() {
        // Iterate over JVM input arguments to find the compile threshold setting
        for (String inputArgument : INPUT_ARGUMENTS) {
            final String prefix = "-XX:CompileThreshold=";
            if (inputArgument.startsWith(prefix)) {
                try {
                    // Parse and return the compile threshold value
                    return Integer.parseInt(inputArgument.substring(prefix.length()));
                } catch (NumberFormatException nfe) {
                    // Ignore the exception and continue
                }
            }
        }
        // Return the default compile threshold value
        return 10_000;
    }

    /**
     * Returns the compile threshold for the JVM or else an
     * estimate thereof (e.g. 10_000).
     * <p>
     * The compile threshold can be explicitly set using the command
     * line parameter "-XX:CompileThreshold="
     *
     * @return the compile threshold for the JVM or else an
     * estimate thereof (e.g. 10_000)
     */
    public static int compileThreshold() {
        return COMPILE_THRESHOLD;
    }

    /**
     * @return the major Java version (e.g. 8, 11 or 17)
     */
    public static int majorVersion() {
        return Bootstrap.getJvmJavaMajorVersion();
    }

    /**
     * @return if the major Java version is 9 or higher
     */
    public static boolean isJava9Plus() {
        return Bootstrap.isJava9Plus();
    }

    /**
     * @return if the major Java version is 12 or higher
     */
    public static boolean isJava12Plus() {
        return Bootstrap.isJava12Plus();
    }

    /**
     * @return if the major Java version is 14 or higher
     */
    public static boolean isJava14Plus() {
        return Bootstrap.isJava14Plus();
    }

    /**
     * @return if the major Java version is 15 or higher
     */
    public static boolean isJava15Plus() {
        return Bootstrap.isJava15Plus();
    }

    /**
     * @return if the major Java version is 19 or higher
     */
    public static boolean isJava19Plus() {
        return Bootstrap.isJava19Plus();
    }

    /**
     * @return if the major Java version is 20 or higher
     */
    public static boolean isJava20Plus() {
        return Bootstrap.isJava20Plus();
    }

    /**
     * @return if the major Java version is 21 or higher
     */
    public static boolean isJava21Plus() {
        return Bootstrap.isJava21Plus();
    }

    /**
     * Returns the current process id.
     *
     * @return the current process id or, if the process id cannot be determined, 1 is used.
     */
    public static int getProcessId() {
        return PROCESS_ID;
    }

    /**
     * Retrieves the process ID (PID) of the current Java Virtual Machine (JVM) process.
     * This method first attempts to read the PID from the `/proc/self` file on Unix-like systems.
     * If unsuccessful, it falls back to using the runtime MXBean's name, which typically includes the PID.
     * If all attempts fail, it defaults to returning a PID of 1.
     *
     * @return The process ID of the current JVM process, or 1 if unable to determine the PID.
     */
    private static int getProcessId0() {
        String pid = null;
        final File self = new File(PROC_SELF);
        try {
            if (self.exists()) {
                // Attempt to read the PID from the /proc/self file
                pid = self.getCanonicalFile().getName();
            }
        } catch (IOException ignored) {
            // Ignore IOExceptions and fallback to other methods
        }

        if (pid == null) {
            // Fallback: Extract PID from the runtime MXBean's name
            pid = getRuntimeMXBean().getName().split("@", 0)[0];
        }

        if (pid != null) {
            try {
                // Attempt to parse the PID as an integer
                return Integer.parseInt(pid);
            } catch (NumberFormatException nfe) {
                // Ignore parsing errors and continue
            }
        }

        // If unable to determine the PID, default to 1
        int rpid = 1;
        System.err.println(Jvm.class.getName() + ": Unable to determine PID, picked 1 as a PID");
        return rpid;
    }

    /**
     * Casts any {@link Throwable}, such as a checked exception, to a {@link RuntimeException}.
     * This method is useful for rethrowing exceptions without needing to declare them in the method signature.
     *
     * @param throwable The throwable to cast and rethrow
     * @param <T>       The type of the Throwable
     * @return This method never returns a Throwable instance; it throws it instead.
     * @throws T The throwable, cast as an unchecked throwable
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException rethrow(Throwable throwable) throws T {
        // Rely on vacuous cast to throw the throwable as an unchecked exception
        throw (T) throwable;
    }

    /**
     * Appends the provided {@code StackTraceElements} to the provided {@code StringBuilder}, trimming some internal methods.
     * This method is useful for cleaning up stack traces by removing unnecessary internal elements.
     *
     * @param stringBuilder      The StringBuilder to append the trimmed stack trace elements to
     * @param stackTraceElements The stack trace elements to trim and append
     */
    public static void trimStackTrace(@NotNull final StringBuilder stringBuilder, @NotNull final StackTraceElement... stackTraceElements) {
        // Determine the first and last stack trace elements to include
        final int first = trimFirst(stackTraceElements);
        final int last = trimLast(first, stackTraceElements);
        // Append the trimmed stack trace elements to the StringBuilder
        for (int i = first; i <= last; i++)
            stringBuilder.append("\n\tat ").append(stackTraceElements[i]);
    }

    /**
     * Determines the index of the first stack trace element that should be included when trimming.
     * Internal methods are skipped, and trimming starts from the first non-internal method.
     *
     * @param stes The stack trace elements to analyze
     * @return The index of the first stack trace element to include
     */
    static int trimFirst(@NotNull final StackTraceElement[] stes) {
        // Special handling for methods ending with 'afepoint' (likely 'safepoint')
        if (stes.length > 2 && stes[1].getMethodName().endsWith("afepoint"))
            return 2;
        int first = 0;
        // Skip over internal methods
        for (; first < stes.length; first++)
            if (!isInternal(stes[first].getClassName()))
                break;
        // Include a couple of elements before the first non-internal method
        return Math.max(0, first - 2);
    }

    /**
     * Determines the index of the last stack trace element that should be included when trimming.
     * Internal methods are skipped, and trimming stops at the last non-internal method.
     *
     * @param first The index of the first element to include
     * @param stes  The stack trace elements to analyze
     * @return The index of the last stack trace element to include
     */
    public static int trimLast(final int first, @NotNull final StackTraceElement[] stes) {
        int last = stes.length - 1;
        // Skip over internal methods starting from the end
        for (; first < last; last--)
            if (!isInternal(stes[last].getClassName()))
                break;
        // Adjust if we went too far
        if (last < stes.length - 1) last++;
        return last;
    }

    /**
     * Determines if a class name corresponds to an internal class that should be excluded from stack traces.
     * Internal classes typically start with 'jdk.', 'sun.', or 'java.'.
     *
     * @param className The fully qualified class name to check
     * @return {@code true} if the class is internal and should be excluded; {@code false} otherwise
     */
    static boolean isInternal(@NotNull final String className) {
        // Check for common prefixes of internal classes
        return className.startsWith("jdk.") || className.startsWith("sun.") || className.startsWith("java.");
    }

    /**
     * Returns if the JVM is running in debug mode.
     *
     * @return if the JVM is running in debug mode
     */
    @SuppressWarnings("SameReturnValue")
    public static boolean isDebug() {
        return IS_DEBUG;
    }

    /**
     * Returns if the JVM is running in flight recorder mode.
     *
     * @return if the JVM is running in flight recorder mode
     */
    @SuppressWarnings("SameReturnValue")
    public static boolean isFlightRecorder() {
        return IS_FLIGHT_RECORDER;
    }

    /**
     * Returns if the JVM is running in code coverage mode.
     *
     * @return if the JVM is running in code coverage mode
     */
    public static boolean isCodeCoverage() {
        return IS_COVERAGE;
    }

    /**
     * Silently pause for the provided {@code durationMs} milliseconds.
     * <p>
     * If the provided {@code durationMs} is positive, then the
     * current thread sleeps.
     * <p>
     * If the provided {@code durationMs} is zero, then the
     * current thread yields.
     *
     * @param durationMs to sleep for.
     */
    public static void pause(final long durationMs) {
        if (durationMs <= 0) {
            Thread.yield();
            return;
        }
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Pause in a busy loop for a very short time.
     */
    public static void nanoPause() {
        try {
            if (onSpinWaitMH != null)
                onSpinWaitMH.invokeExact();
        } catch (Throwable throwable) {
            Jvm.rethrow(throwable);
        }
    }

    /**
     * Pause in a busy loop for the provided {@code durationUs} microseconds.
     * <p>
     * This method is designed to be used when the time to be waited is very small,
     * typically under a millisecond (@{code durationUs &lt; 1_000}).
     *
     * @param durationUs Time in durationUs
     */
    public static void busyWaitMicros(final long durationUs) {
        busyWaitUntil(System.nanoTime() + (durationUs * 1_000));
    }

    /**
     * Pauses the current thread in a busy loop until the provided {@code waitUntilNs} time is reached.
     * <p>
     * This method is designed to be used when the time to be waited is very small,
     * typically under a millisecond (@{code durationNs &lt; 1_000_000}).
     *
     * @param waitUntilNs nanosecond precision counter value to await.
     */
    public static void busyWaitUntil(final long waitUntilNs) {
        while (waitUntilNs > System.nanoTime()) {
            Jvm.nanoPause();
        }
    }

    /**
     * Returns the Field for the provided {@code clazz} and the provided {@code fieldName} or
     * throws an Exception if no such Field exists.
     *
     * @param clazz     to get the field for
     * @param fieldName of the field
     * @return the Field.
     * @throws AssertionError if no such Field exists
     */
    // Todo: Should not throw an AssertionError but rather a RuntimeException
    @NotNull
    public static Field getField(@NotNull final Class<?> clazz, @NotNull final String fieldName) {
        return ClassUtil.getField0(clazz, fieldName, true);
    }

    /**
     * Returns the Field for the provided {@code clazz} and the provided {@code fieldName} or {@code null}
     * if no such Field exists.
     *
     * @param clazz     to get the field for
     * @param fieldName of the field
     * @return the Field.
     * @throws AssertionError if no such Field exists
     */
    @Nullable
    public static Field getFieldOrNull(@NotNull final Class<?> clazz, @NotNull final String fieldName) {
        return ClassUtil.getField0(clazz, fieldName, false);
    }

    /**
     * Returns the Method for the provided {@code clazz}, {@code methodName} and
     * {@code argTypes} or throws an Exception.
     * <p>
     * if it exists or throws {@link AssertionError}.
     * <p>
     * Default methods are not detected unless the class explicitly overrides it
     *
     * @param clazz      class
     * @param methodName methodName
     * @param argTypes   argument types
     * @return method
     * @throws AssertionError if no such Method exists
     */

    // Todo: Should not throw an AssertionError but rather a RuntimeException
    @NotNull
    public static Method getMethod(@NotNull final Class<?> clazz,
                                   @NotNull final String methodName,
                                   final Class... argTypes) {
        return ClassUtil.getMethod0(clazz, methodName, argTypes, true);
    }

    /**
     * Set the accessible flag for the provided {@code accessibleObject} indicating that
     * the reflected object should suppress Java language access checking when it is used.
     * <p>
     * The setting of the accessible flag might be subject to security manager approval.
     *
     * @param accessibleObject to modify
     * @throws SecurityException – if the request is denied.
     * @see SecurityManager#checkPermission
     * @see RuntimePermission
     */
    public static void setAccessible(@NotNull final AccessibleObject accessibleObject) {
        ClassUtil.setAccessible(accessibleObject);
    }
    /**
     * Returns the value of the provided {@code fieldName} extracted from the provided {@code target}.
     * <p>
     * The provided {@code fieldName} can denote fields of arbitrary depth (e.g. foo.bar.baz, whereby
     * the foo value will be extracted from the provided {@code target} and then the bar value
     * will be extracted from the foo value and so on).
     *
     * @param target    used for extraction
     * @param fieldName denoting the field(s) to extract
     * @param <V>       return type
     * @return the value of the provided {@code fieldName} extracted from the provided {@code target}
     */
    @Nullable
    public static <V> V getValue(@NotNull Object target, @NotNull final String fieldName) {
        Class<?> aClass = target.getClass();
        for (String n : fieldName.split("/")) {
            Field f = getField(aClass, n);
            try {
                target = f.get(target);
                if (target == null)
                    return null;
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new AssertionError(e);
            }
            aClass = target.getClass();
        }
        return (V) target;
    }

    /**
     * Log the stack trace of the thread holding a lock.
     *
     * @param lock to log
     * @return the lock.toString plus a stack trace.
     */
    public static String lockWithStack(@NotNull final ReentrantLock lock) {
        final Thread t = getValue(lock, "sync/exclusiveOwnerThread");
        if (t == null) {
            return lock.toString();
        }
        final StringBuilder ret = new StringBuilder();
        ret.append(lock).append(" running at");
        trimStackTrace(ret, t.getStackTrace());
        return ret.toString();
    }

    /**
     * @param clazz     the class for which you want to get field from [ it won't see inherited fields ]
     * @param fieldName the name of the field
     * @return the offset
     */
    public static long fieldOffset(final Class<?> clazz, final String fieldName) {
        try {
            return UNSAFE.objectFieldOffset(clazz.getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the accumulated amount of memory in bytes used by direct ByteBuffers
     * or 0 if the value cannot be determined.
     * <p>
     * (i.e. ever allocated via ByteBuffer.allocateDirect())
     *
     * @return the accumulated amount of memory in bytes used by direct ByteBuffers
     * or 0 if the value cannot be determined
     */
    public static long usedDirectMemory() {
        return reservedMemory.get();
    }

    /**
     * Returns the accumulated amount of memory used in bytes by UnsafeMemory.allocate().
     *
     * @return the accumulated amount of memory used in bytes by UnsafeMemory.allocate()
     */
    public static long usedNativeMemory() {
        return UnsafeMemory.INSTANCE.nativeMemoryUsed();
    }

    /**
     * Returns the maximum direct memory in bytes that can ever be allocated or 0 if the
     * value cannot be determined.
     * (i.e. ever allocated via ByteBuffer.allocateDirect())
     *
     * @return the maximum direct memory in bytes that can ever be allocated or 0 if the
     * value cannot be determined
     */
    public static long maxDirectMemory() {
        return MAX_DIRECT_MEMORY;
    }

    /**
     * Returns if the JVM runs in 64 bit mode.
     *
     * @return if the JVM runs in 64 bit mode
     */
    public static boolean is64bit() {
        return IS_64BIT;
    }

    /**
     * Resets all exception handlers to their default settings. This includes handlers for errors, warnings, debug, and performance logs.
     */
    public static void resetExceptionHandlers() {
        // Reset error, warning, debug, and performance handlers to their default values
        setErrorExceptionHandler(DEFAULT_ERROR_EXCEPTION_HANDLER);
        setWarnExceptionHandler(DEFAULT_WARN_EXCEPTION_HANDLER);
        setDebugExceptionHandler(DEFAULT_DEBUG_EXCEPTION_HANDLER);
        setPerfExceptionHandler(DEFAULT_PERF_EXCEPTION_HANDLER);
    }

    /**
     * Sets a new default exception handler for error level logs.
     *
     * @param exceptionHandler The exception handler to be used for error level logs.
     */
    public static void setErrorExceptionHandler(ExceptionHandler exceptionHandler) {
        // Set the default error handler and reset any thread-local handlers
        ERROR.defaultHandler(exceptionHandler).resetThreadLocalHandler();
    }

    /**
     * Sets a new default exception handler for warning level logs.
     *
     * @param exceptionHandler The exception handler to be used for warning level logs.
     */
    public static void setWarnExceptionHandler(ExceptionHandler exceptionHandler) {
        // Set the default warning handler and reset any thread-local handlers
        WARN.defaultHandler(exceptionHandler).resetThreadLocalHandler();
    }

    /**
     * Sets a new default exception handler for debug level logs.
     *
     * @param exceptionHandler The exception handler to be used for debug level logs.
     */
    public static void setDebugExceptionHandler(ExceptionHandler exceptionHandler) {
        // Check if the current debug handler is a thread-localized handler
        if (DEBUG instanceof ThreadLocalisedExceptionHandler)
            // Set the default debug handler and reset any thread-local handlers
            ((ThreadLocalisedExceptionHandler) DEBUG).defaultHandler(exceptionHandler).resetThreadLocalHandler();
    }

    /**
     * Sets a new default exception handler for performance level logs.
     *
     * @param exceptionHandler The exception handler to be used for performance level logs.
     */
    public static void setPerfExceptionHandler(ExceptionHandler exceptionHandler) {
        // Set the default performance handler and reset any thread-local handlers
        PERF.defaultHandler(exceptionHandler).resetThreadLocalHandler();
    }

    /**
     * Disables the debug level exception handler, effectively ignoring all debug level logs.
     */
    public static void disableDebugHandler() {
        // Set the debug exception handler to null, disabling it
        setDebugExceptionHandler(null);
    }

    /**
     * Disables the performance level exception handler, effectively ignoring all performance level logs.
     */
    public static void disablePerfHandler() {
        // Set the performance exception handler to null, disabling it
        setPerfExceptionHandler(null);
    }

    /**
     * Disables the warning level exception handler, effectively ignoring all warning level logs.
     */
    public static void disableWarnHandler() {
        // Set the warning exception handler to null, disabling it
        setWarnExceptionHandler(null);
    }

    /**
     * Records exceptions by setting up custom exception handlers for error, warning, debug, and performance levels.
     * This method returns a synchronized map that logs each exception occurrence.
     *
     * @return A map containing all recorded exceptions with their corresponding occurrence counts.
     */
    @NotNull
    public static Map<ExceptionKey, Integer> recordExceptions() {
        // Record exceptions with debug enabled by default
        return recordExceptions(true);
    }

    /**
     * Records exceptions by setting up custom exception handlers with optional debug logging.
     *
     * @param debug If true, enables debug level logging.
     * @return A map containing all recorded exceptions with their corresponding occurrence counts.
     */
    @NotNull
    public static Map<ExceptionKey, Integer> recordExceptions(boolean debug) {
        // Record exceptions with the specified debug setting, and exceptions only set to false
        return recordExceptions(debug, false);
    }

    /**
     * Records exceptions by setting up custom exception handlers with options for debug logging and filtering.
     *
     * @param debug           If true, enables debug level logging.
     * @param exceptionsOnly  If true, only exceptions will be recorded, excluding other log levels.
     * @return A map containing all recorded exceptions with their corresponding occurrence counts.
     */
    @NotNull
    public static Map<ExceptionKey, Integer> recordExceptions(boolean debug, boolean exceptionsOnly) {
        // Record exceptions with the specified debug and exceptions only settings, and logToSlf4j set to true
        return recordExceptions(debug, exceptionsOnly, true);
    }

    /**
     * Records exceptions by setting up custom exception handlers with full control over logging and exception handling.
     *
     * @param debug           If true, enables debug level logging.
     * @param exceptionsOnly  If true, only exceptions will be recorded, excluding other log levels.
     * @param logToSlf4j      If true, logs to SLF4J in addition to recording.
     * @return A map containing all recorded exceptions with their corresponding occurrence counts.
     */
    @NotNull
    public static Map<ExceptionKey, Integer> recordExceptions(final boolean debug,
                                                              final boolean exceptionsOnly,
                                                              final boolean logToSlf4j) {
        // Create a synchronized map to store exception occurrences
        final Map<ExceptionKey, Integer> map = Collections.synchronizedMap(new LinkedHashMap<>());

        // Set up custom exception handlers for error, warning, performance, and debug levels
        setErrorExceptionHandler(recordingExceptionHandler(LogLevel.ERROR, map, exceptionsOnly, logToSlf4j));
        setWarnExceptionHandler(recordingExceptionHandler(LogLevel.WARN, map, exceptionsOnly, logToSlf4j));
        setPerfExceptionHandler(debug
                ? recordingExceptionHandler(LogLevel.PERF, map, exceptionsOnly, logToSlf4j)
                : logToSlf4j ? Slf4jExceptionHandler.PERF : NullExceptionHandler.NOTHING);
        setDebugExceptionHandler(debug
                ? recordingExceptionHandler(LogLevel.DEBUG, map, exceptionsOnly, logToSlf4j)
                : logToSlf4j ? Slf4jExceptionHandler.DEBUG : NullExceptionHandler.NOTHING);

        // Return the map containing recorded exceptions
        return map;
    }

    /**
     * Creates an exception handler that records exceptions into a provided map and optionally logs them to SLF4J.
     *
     * @param logLevel        The log level for the exception handler.
     * @param map             The map where exceptions are recorded with their occurrence counts.
     * @param exceptionsOnly  If true, only exceptions will be recorded.
     * @param logToSlf4j      If true, logs to SLF4J in addition to recording.
     * @return The created exception handler that records and optionally logs exceptions.
     */
    private static ExceptionHandler recordingExceptionHandler(final LogLevel logLevel,
                                                              final Map<ExceptionKey, Integer> map,
                                                              final boolean exceptionsOnly,
                                                              final boolean logToSlf4j) {
        // Create a new RecordingExceptionHandler to store exceptions in the map
        final ExceptionHandler eh = new RecordingExceptionHandler(logLevel, map, exceptionsOnly);
        // If logging to SLF4J is enabled, chain the new handler with the SLF4J handler
        if (logToSlf4j)
            return new ChainedExceptionHandler(eh, Slf4jExceptionHandler.valueOf(logLevel));
        // Return the recording exception handler
        return eh;
    }

    /**
     * Checks if a map of recorded exceptions contains any non-debug and non-performance level exceptions.
     *
     * @param exceptions The map of recorded exceptions to check.
     * @return {@code true} if the map contains exceptions other than debug or performance levels; {@code false} otherwise.
     */
    public static boolean hasException(@NotNull final Map<ExceptionKey, Integer> exceptions) {
        // Iterate over the keys of the exceptions map
        final Iterator<ExceptionKey> iterator = exceptions.keySet().iterator();
        while (iterator.hasNext()) {
            final ExceptionKey k = iterator.next();
            // Check if the exception level is not DEBUG or PERF
            if (k.level() != LogLevel.DEBUG && k.level() != LogLevel.PERF)
                return true;
        }

        // Return false if no exceptions other than DEBUG or PERF were found
        return false;
    }

    /**
     * Sets the global exception handlers for error, warning, and debug levels.
     * This method configures the default handlers for each log level, replacing any existing handlers.
     *
     * @param error The exception handler to be used for error level logs, or null to use the default handler.
     * @param warn  The exception handler to be used for warning level logs, or null to use the default handler.
     * @param debug The exception handler to be used for debug level logs, or null to use the default handler.
     */
    public static void setExceptionHandlers(@Nullable final ExceptionHandler error,
                                            @Nullable final ExceptionHandler warn,
                                            @Nullable final ExceptionHandler debug) {

        // Set the default exception handler for error level logs
        ERROR.defaultHandler(error);
        // Set the default exception handler for warning level logs
        WARN.defaultHandler(warn);
        // Check if the current debug handler is a thread-localized handler
        if (DEBUG instanceof ThreadLocalisedExceptionHandler)
            // Set the default exception handler for debug level logs
            ((ThreadLocalisedExceptionHandler) DEBUG).defaultHandler(debug);
    }

    /**
     * Sets the global exception handlers for error, warning, debug, and performance levels.
     * This method configures the default handlers for each log level, replacing any existing handlers.
     *
     * @param error The exception handler to be used for error level logs, or null to use the default handler.
     * @param warn  The exception handler to be used for warning level logs, or null to use the default handler.
     * @param debug The exception handler to be used for debug level logs, or null to use the default handler.
     * @param perf  The exception handler to be used for performance level logs, or null to use the default handler.
     */
    public static void setExceptionHandlers(@Nullable final ExceptionHandler error,
                                            @Nullable final ExceptionHandler warn,
                                            @Nullable final ExceptionHandler debug,
                                            @Nullable final ExceptionHandler perf) {
        // Set the exception handlers for error, warning, and debug levels
        setExceptionHandlers(error, warn, debug);
        // Set the default exception handler for performance level logs
        PERF.defaultHandler(perf);
    }

    /**
     * Sets the thread-local exception handlers for error, warning, and debug levels.
     * This allows different threads to have their own handlers for these log levels.
     *
     * @param error The thread-local exception handler to be used for error level logs, or null to use the default handler.
     * @param warn  The thread-local exception handler to be used for warning level logs, or null to use the default handler.
     * @param debug The thread-local exception handler to be used for debug level logs, or null to use the default handler.
     */
    public static void setThreadLocalExceptionHandlers(@Nullable final ExceptionHandler error,
                                                       @Nullable final ExceptionHandler warn,
                                                       @Nullable final ExceptionHandler debug) {
        // Set the thread-local exception handler for error level logs
        ERROR.threadLocalHandler(error);
        // Set the thread-local exception handler for warning level logs
        WARN.threadLocalHandler(warn);
        // Check if the current debug handler is a thread-localized handler
        if (DEBUG instanceof ThreadLocalisedExceptionHandler)
            // Set the thread-local exception handler for debug level logs
            ((ThreadLocalisedExceptionHandler) DEBUG).threadLocalHandler(debug);
    }

    /**
     * Sets the thread-local exception handlers for error, warning, debug, and performance levels.
     * This allows different threads to have their own handlers for these log levels.
     *
     * @param error The thread-local exception handler to be used for error level logs, or null to use the default handler.
     * @param warn  The thread-local exception handler to be used for warning level logs, or null to use the default handler.
     * @param debug The thread-local exception handler to be used for debug level logs, or null to use the default handler.
     * @param perf  The thread-local exception handler to be used for performance level logs, or null to use the default handler.
     */
    public static void setThreadLocalExceptionHandlers(@Nullable final ExceptionHandler error,
                                                       @Nullable final ExceptionHandler warn,
                                                       @Nullable final ExceptionHandler debug,
                                                       @Nullable final ExceptionHandler perf) {
        // Set the thread-local exception handlers for error, warning, and debug levels
        setThreadLocalExceptionHandlers(error, warn, debug);
        // Set the thread-local exception handler for performance level logs
        PERF.threadLocalHandler(perf);
    }

    /**
     * Returns an ExceptionHandler for errors, this prints as System.err or ERROR level logging.
     * In tests these messages are usually captured and checked that the error expected and only those expected are produced.
     *
     * @return the ERROR exception handler
     */
    @NotNull
    public static ExceptionHandler error() {
        return ERROR;
    }

    /**
     * Returns an ExceptionHandler for warnings, this prints as System.out or WARN level logging.
     * In tests these messages are usually captured and checked that the warning expected and only those expected are produced.
     *
     * @return the WARN exception handler
     */
    @NotNull
    public static ExceptionHandler warn() {
        return WARN;
    }

    /**
     * Returns an ExceptionHandler for startup messages, this prints as System.out or INFO level logging.
     * In tests these messages are generally not captured for checking.
     *
     * @return the STARTUP exception handler
     */
    @NotNull
    public static ExceptionHandler startup() {
        // TODO, add a startup level?
        return PERF;
    }

    /**
     * Returns an ExceptionHandler for performance messages, this prints as System.out or INFO level logging.
     * In tests these messages are generally not captured for checking, but a few tests may check performance metrics are reported.
     *
     * @return the PERF exception handler
     */
    @NotNull
    public static ExceptionHandler perf() {
        return PERF;
    }

    /**
     * Returns an ExceptionHandler for debug messages, this prints as System.out or DEBUG level logging.
     * In tests these messages are generally not captured for checking.
     *
     * @return the DEBUG exception handler
     */
    @NotNull
    public static ExceptionHandler debug() {
        return DEBUG;
    }

    /**
     * Dumps all recorded exceptions to the log using the WARN log level. Each exception is logged with its details
     * including the class, message, and throwable. If an exception occurred multiple times, the number of occurrences
     * is also logged. After logging, all exception handlers are reset to their default settings.
     *
     * @param exceptions A map containing exceptions as keys and their occurrence counts as values.
     */
    public static void dumpException(@NotNull final Map<ExceptionKey, Integer> exceptions) {
        // Get the WARN level SLF4J exception handler
        final Slf4jExceptionHandler warn = Slf4jExceptionHandler.WARN;

        // Iterate over each entry in the exceptions map
        for (@NotNull Entry<ExceptionKey, Integer> entry : exceptions.entrySet()) {
            final ExceptionKey key = entry.getKey();
            // Log the exception details including level, class, message, and throwable
            warn.on(Jvm.class, key.level() + " " + key.clazz().getSimpleName() + " " + key.message(), key.throwable());
            final Integer value = entry.getValue();
            // If the exception occurred more than once, log the repeat count
            if (value > 1)
                warn.on(Jvm.class, "Repeated " + value + " times");
        }
        // Reset all exception handlers to their default settings
        resetExceptionHandlers();
    }

    /**
     * Checks if debug level logging is enabled for the specified class.
     *
     * @param aClass The class for which to check if debug logging is enabled.
     * @return {@code true} if debug logging is enabled for the specified class; {@code false} otherwise.
     */
    public static boolean isDebugEnabled(final Class<?> aClass) {
        // Check if the debug exception handler is enabled for the specified class
        return DEBUG.isEnabled(aClass);
    }

    /**
     * Checks if performance level logging is enabled for the specified class.
     *
     * @param aClass The class for which to check if performance logging is enabled.
     * @return {@code true} if performance logging is enabled for the specified class; {@code false} otherwise.
     */
    public static boolean isPerfEnabled(final Class<?> aClass) {
        // Check if the performance exception handler is enabled for the specified class
        return PERF.isEnabled(aClass);
    }

    /**
     * Determines the maximum direct memory available to the JVM. This method attempts to use internal JVM classes
     * to fetch the value of the direct memory limit. It first tries to use the Java 9+ class `jdk.internal.misc.VM`,
     * and if that fails, it falls back to the pre-Java 9 class `sun.misc.VM`.
     *
     * @return The maximum direct memory available in bytes. Returns 0 if the value could not be determined.
     */
    private static long maxDirectMemory0() {
        try {
            final Class<?> clz;
            // Check if the runtime environment is Java 9 or later
            if (isJava9Plus()) {
                clz = Class.forName("jdk.internal.misc.VM");
            } else {
                clz = Class.forName("sun.misc.VM");
            }

            // Retrieve the field representing direct memory limit
            final Field f = getField(clz, "directMemory");
            // Return the value of the direct memory field
            return f.getLong(null);
        } catch (Exception e) {
            // Ignore any exceptions during reflection
        }
        // Log an error message if unable to determine max direct memory
        System.err.println(Jvm.class.getName() + ": Unable to determine max direct memory");
        return 0L;
    }

    /**
     * Adds the provided {@code signalHandler} to an internal chain of handlers that will be invoked
     * upon detecting system signals (e.g. HUP, INT, TERM).
     * <p>
     * Not all signals are available on all operating systems.
     *
     * @param signalHandler to call on a signal
     */
    public static void addSignalHandler(final SignalHandler signalHandler) {
        final SignalHandler signalHandler2 = signal -> {
            Jvm.warn().on(signalHandler.getClass(), "Signal " + signal + " triggered for " + signalHandler);
            signalHandler.handle(signal);
        };
        signalHandlerGlobal.handlers2.add(signalHandler2);
        InitSignalHandlers.init();
    }

    /**
     * Inserts a low-cost Java safe-point in the code path if -Djvm.safepoint.enabled
     */
    public static void safepoint() {
        if (SAFEPOINT_ENABLED) {
            if (Jvm.isAzulZing())
                s_blackHole = Thread.currentThread();
            Safepoint.force();
        }
    }

    public static boolean areOptionalSafepointsEnabled() {
        return SAFEPOINT_ENABLED;
    }

    /**
     * Returns if there is a class name that ends with the provided {@code endsWith} string
     * when examining the current stack trace of depth at most up to the provided {@code maxDepth}.
     *
     * @param endsWith to test against the current stack trace
     * @param maxDepth to examine
     * @return if there is a class name that ends with the provided {@code endsWith} string
     * when examining the current stack trace of depth at most up to the provided {@code maxDepth}
     */
    public static boolean stackTraceEndsWith(final String endsWith, final int maxDepth) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = maxDepth + 2; i < stackTrace.length; i++)
            if (stackTrace[i].getClassName().endsWith(endsWith))
                return true;
        return false;
    }

    /**
     * Returns if the JVM runs on a CPU using the ARM architecture.
     *
     * @return if the JVM runs on a CPU using the ARM architecture
     */
    public static boolean isArm() {
        return IS_ARM;
    }

    /**
     * Returns if the JVM runs on a CPU using a Mac ARM architecture.
     *
     * @return if the JVM runs on a CPU using the Mac ARM architecture e.g. Apple M1.
     */
    public static boolean isMacArm() {
        return IS_MAC_ARM;
    }

    /**
     * Acquires and returns the ClassMetrics for the provided {@code clazz}.
     *
     * @param clazz for which ClassMetrics shall be acquired
     * @return the ClassMetrics for the provided {@code clazz}
     * @throws IllegalArgumentException if no ClassMetrics can be acquired
     * @see ClassMetrics
     */
    @NotNull
    public static ClassMetrics classMetrics(final Class<?> clazz) throws IllegalArgumentException {
        return CLASS_METRICS_MAP.computeIfAbsent(clazz, Jvm::getClassMetrics);
    }

    /**
     * Computes and returns the memory layout metrics for a given class.
     * This method analyzes the memory offsets of the primitive fields in the class and its superclasses
     * to determine the start and end offsets, which are used to calculate the memory footprint.
     *
     * @param c The class for which to compute memory layout metrics.
     * @return An instance of {@link ClassMetrics} representing the memory layout of the class.
     */
    private static ClassMetrics getClassMetrics(final Class<?> c) {
        assert !c.isArray(); // Ensure the class is not an array
        final Class<?> superclass = c.getSuperclass();
        int start = Integer.MAX_VALUE; // Initialize start offset
        int end = 0; // Initialize end offset

        // Iterate over all declared fields in the class
        for (Field f : c.getDeclaredFields()) {
            // Skip static, transient, or non-primitive fields
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0 || !f.getType().isPrimitive())
                continue;
            // Compute the offset of the field
            int start0 = Math.toIntExact(UnsafeMemory.unsafeObjectFieldOffset(f));
            // Get the size of the primitive type
            int size = PRIMITIVE_SIZE.get(f.getType());
            start = Math.min(start0, start); // Update start offset
            end = Math.max(start0 + size, end); // Update end offset
        }

        // Recursively calculate metrics for the superclass, if any
        if (superclass != null && superclass != Object.class) {
            final ClassMetrics cm0 = getClassMetrics(superclass);
            start = Math.min(cm0.offset(), start);
            end = Math.max(cm0.offset() + cm0.length(), end);
            validateClassMetrics(superclass, start, end);
        }

        validateClassMetrics(c, start, end);

        return new ClassMetrics(start, end - start);
    }

    /**
     * Validates the class metrics to ensure there are no non-primitive fields within the range of primitive fields.
     * This is necessary to guarantee that the class is suitable for raw memory copies.
     *
     * @param c     The class being validated.
     * @param start The start offset of primitive fields.
     * @param end   The end offset of primitive fields.
     */
    private static void validateClassMetrics(final Class<?> c,
                                             final int start,
                                             final int end) {
        // Iterate over all declared fields in the class
        for (Field f : c.getDeclaredFields()) {
            // Skip static or primitive fields
            if ((f.getModifiers() & Modifier.STATIC) != 0 || f.getType().isPrimitive())
                continue;
            // Compute the offset of the field
            final int start0 = Math.toIntExact(UnsafeMemory.unsafeObjectFieldOffset(f));
            // If a non-primitive field falls within the range, throw an exception
            if (start <= start0 && start0 < end) {
                rethrow(new IllegalArgumentException(c + " is not suitable for raw copies due to " + f));
            }
        }
    }

    /**
     * Returns the user's home directory (e.g., "/home/alice") or "." if the user's home directory cannot be determined.
     *
     * @return The user's home directory or "." if it cannot be determined.
     */
    @NotNull
    public static String userHome() {
        return System.getProperty("user.home", ".");
    }

    /**
     * Determines if the specified class should not be chained, based on its annotations or if it is a Java core class.
     *
     * @param tClass The class to check for the {@link DontChain} annotation or if it is a core Java class.
     * @return {@code true} if the class should not be chained; {@code false} otherwise.
     */
    public static boolean dontChain(final Class<?> tClass) {
        return tClass.getAnnotation(DontChain.class) != null || tClass.getName().startsWith("java");
    }

    /**
     * Returns if certain chronicle resources (such as memory regions) are traced.
     * <p>
     * Tracing resources incurs slightly less performance but provides a means
     * of detecting proper release of resources.
     *
     * @return true if specific chronicle resources are traced, false otherwise.
     */
    public static boolean isResourceTracing() {
        return RESOURCE_TRACING;
    }

    /**
     * Sets the state of resource tracing. This is especially useful for testing scenarios.
     * <p>
     * Note: Enabling resource tracing can exert significant pressure on the garbage collector during stress tests.
     *
     * @param resourceTracing true to enable resource tracing, false to disable.
     */
    public static void setResourceTracing(boolean resourceTracing) {
        RESOURCE_TRACING = resourceTracing;
    }


    /**
     * Guarantees that Jvm class is initialized before property is read.
     *
     * @see System#getProperty(String)
     */
    public static String getProperty(final String systemPropertyKey) {
        init();

        return System.getProperty(systemPropertyKey);
    }

    /**
     * Guarantees that Jvm class is initialized before property is read.
     *
     * @see System#getProperty(String, String)
     */
    public static String getProperty(final String systemPropertyKey, final String defaultValue) {
        init();

        return System.getProperty(systemPropertyKey, defaultValue);
    }

    /**
     * Guarantees that Jvm class is initialized before property is read.
     *
     * @see Long#getLong(String, Long)
     */
    public static Long getLong(final String systemPropertyKey, final Long defVal) {
        init();

        return Long.getLong(systemPropertyKey, defVal);
    }

    /**
     * Guarantees that Jvm class is initialized before property is read.
     *
     * @see Integer#getInteger(String, Integer)
     */
    public static Integer getInteger(final String systemPropertyKey, final Integer defVal) {
        init();

        return Integer.getInteger(systemPropertyKey, defVal);
    }

    /**
     * Returns if a System Property with the provided {@code systemPropertyKey}
     * either exists, is set to "yes" or is set to "true".
     * <p>
     * This provides a more permissive boolean System systemPropertyKey flag where
     * {@code -Dflag} {@code -Dflag=true} {@code -Dflag=yes} are all accepted.
     * <p>
     * Guarantees that Jvm class is initialized before property is read.
     *
     * @param systemPropertyKey name to lookup
     * @return if a System Property with the provided {@code systemPropertyKey}
     * either exists, is set to "yes" or is set to "true"
     */
    public static boolean getBoolean(final String systemPropertyKey) {
        return getBoolean(systemPropertyKey, false);
    }

    /**
     * Returns if a System Property with the provided {@code systemPropertyKey}
     * either exists, is set to "yes" or is set to "true" or, if it does not exist,
     * returns the provided {@code defaultValue}.
     * <p>
     * This provides a more permissive boolean System systemPropertyKey flag where
     * {@code -Dflag} {@code -Dflag=true} {@code -Dflag=yes} are all accepted.
     * <p>
     * Guarantees that Jvm class is initialized before property is read.
     *
     * @param systemPropertyKey name to lookup
     * @param defaultValue      value to be used if unknown
     * @return if a System Property with the provided {@code systemPropertyKey}
     * either exists, is set to "yes" or is set to "true" or, if it does not exist,
     * returns the provided {@code defaultValue}.
     */
    public static boolean getBoolean(final String systemPropertyKey, final boolean defaultValue) {
        final String value = Jvm.getProperty(systemPropertyKey);
        if (value == null)
            return defaultValue;
        if (value.isEmpty())
            return true;
        final String trim = value.trim();
        return defaultValue
                ? !ObjectUtils.isFalse(trim)
                : ObjectUtils.isTrue(trim);
    }

    /**
     * Parse a string as a decimal memory size with an optional scale.
     * K/k = * 2<sup>10</sup>, M/m = 2<sup>20</sup>, G/g = 2<sup>10</sup>, T/t = 2<sup>40</sup>
     *
     * <p>
     * trailing B/b/iB/ib are ignored.
     *      <table>
     *         <tr><td>100</td><td>100 bytes</td></tr>
     *         <tr><td>100b</td><td>100 bytes</td></tr>
     *         <tr><td>0.5kb</td><td>512 bytes</td></tr>
     *         <tr><td>0.125MB</td><td>128 KiB</td></tr>
     *         <tr><td>2M</td><td>2 MiB</td></tr>
     *         <tr><td>0.75GiB</td><td>768 MiB</td></tr>
     *         <tr><td>0.001TiB</td><td>1.024 GiB</td></tr>
     *     </table>
     * 
     *
     * @param value size to parse
     * @return the size
     * @throws IllegalArgumentException if the string could not be parsed
     */
    public static long parseSize(@NotNull String value) throws IllegalArgumentException {
        long factor = 1;

        if (value.length() > 1) {
            char last = value.charAt(value.length() - 1);
            // assume we meant bytes, not bits
            if (last == 'b' || last == 'B') {
                value = value.substring(0, value.length() - 1);
                last = value.charAt(value.length() - 1);
            }
            if (last == 'i') {
                value = value.substring(0, value.length() - 1);
                last = value.charAt(value.length() - 1);
            }
            if (Character.isLetter(last)) {
                switch (last) {
                    case 't':
                    case 'T':
                        factor = 1L << 40;
                        break;
                    case 'g':
                    case 'G':
                        factor = 1L << 30;
                        break;
                    case 'm': // technically milli, but we will assume mega
                    case 'M':
                        factor = 1L << 20;
                        break;
                    case 'k':
                    case 'K':
                        factor = 1L << 10;
                        break;
                    default:
                        throw new IllegalArgumentException("Unrecognised suffix for size " + value);
                }
                value = value.substring(0, value.length() - 1);
            }
        }
        double number = Double.parseDouble(value.trim());
        factor *= number;
        return factor;
    }

    /**
     * Uses Jvm.parseSize to parse a system property or returns defaultValue if not present, empty or unparseable.
     *
     * @param property     to look up
     * @param defaultValue to use otherwise
     * @return the size in bytes as a long
     */
    public static long getSize(final String property, final long defaultValue) {
        final String value = Jvm.getProperty(property);
        if (value == null || value.length() <= 0)
            return defaultValue;
        try {
            return parseSize(value);
        } catch (IllegalArgumentException iae) {
            Jvm.warn().on(Jvm.class, "Unable to parse the property " + property + " as a size " + iae.getMessage() + " using " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Returns the native address of the provided {@code byteBuffer}.
     * <p>
     * <em>Use with caution!</em>. Native address should always be carefully
     * guarded to prevent unspecified results or even JVM crashes.
     *
     * @param byteBuffer from which to extract the native address
     * @return the native address of the provided {@code byteBuffer}
     */
    public static long address(@NotNull final ByteBuffer byteBuffer) {
        return DirectBufferUtil.addressOrThrow(byteBuffer);
    }

    /**
     * Returns the array byte base offset used by this JVM.
     * <p>
     * The value is the number of bytes that precedes the actual
     * memory layout of a {@code byte[] } array in a java array object.
     * <p>
     * <em>Use with caution!</em>. Native address should always be carefully
     * guarded to prevent unspecified results or even JVM crashes.
     *
     * @return the array byte base offset used by this JVM
     */
    public static int arrayByteBaseOffset() {
        return Unsafe.ARRAY_BYTE_BASE_OFFSET;
    }

    /**
     * Employs a best-effort of preventing the provided {@code fc } from being automatically closed
     * whenever the current thread gets interrupted.
     * <p>
     * If the effort failed, the provided {@code clazz} is used for logging purposes.
     *
     * @param clazz to use for logging should the effort fail.
     * @param fc    to prevent from automatically closing upon interrupt.
     */
    public static void doNotCloseOnInterrupt(final Class<?> clazz, final FileChannel fc) {
        if (Jvm.isJava9Plus())
            doNotCloseOnInterrupt9(clazz, fc);
        else
            doNotCloseOnInterrupt8(clazz, fc);
    }

    /**
     * Disables closing of a FileChannel on thread interrupt for Java 8 and earlier.
     *
     * @param clazz The class from which this method is called.
     * @param fc    The file channel for which to disable closing on interrupt.
     */
    private static void doNotCloseOnInterrupt8(final Class<?> clazz, final FileChannel fc) {
        try {
            // Access the 'interruptor' field of AbstractInterruptibleChannel
            final Field field = AbstractInterruptibleChannel.class.getDeclaredField("interruptor");
            ClassUtil.setAccessible(field);
            final CommonInterruptible ci = new CommonInterruptible(clazz, fc);
            // Set a custom interruptible to prevent channel closing
            field.set(fc, (Interruptible) thread -> ci.interrupt());
        } catch (Throwable e) {
            // Log a warning if unable to disable close on interrupt
            Jvm.warn().on(clazz, "Couldn't disable close on interrupt", e);
        }
    }

    // based on a solution by https://stackoverflow.com/users/9199167/max-vollmer
    // https://stackoverflow.com/a/52262779/57695
    /**
     * Disables closing of a FileChannel on thread interrupt for Java 9 and later.
     * Utilizes a dynamic proxy to override the default interrupt behavior.
     *
     * @param clazz The class from which this method is called.
     * @param fc    The file channel for which to disable closing on interrupt.
     */
    private static void doNotCloseOnInterrupt9(final Class<?> clazz, final FileChannel fc) {
        try {
            // Access the 'interruptor' field of AbstractInterruptibleChannel
            final Field field = AbstractInterruptibleChannel.class.getDeclaredField("interruptor");
            final Class<?> interruptibleClass = field.getType();
            ClassUtil.setAccessible(field);
            final CommonInterruptible ci = new CommonInterruptible(clazz, fc);
            // Create a proxy to override interrupt behavior
            field.set(fc, Proxy.newProxyInstance(
                    interruptibleClass.getClassLoader(),
                    new Class[]{interruptibleClass},
                    (p, m, a) -> {
                        if (m.getDeclaringClass() != Object.class)
                            ci.interrupt();
                        return ObjectUtils.defaultValue(m.getReturnType());
                    }));
        } catch (Throwable e) {
            // Log a warning if unable to disable close on interrupt
            Jvm.warn().on(clazz, "Couldn't disable close on interrupt", e);
        }
    }

    /**
     * Ensures that all the jars and other resources are added to the class path of the classloader
     * associated by the provided {@code clazz}.
     *
     * @param clazz to use as a template.
     */
    public static void addToClassPath(@NotNull final Class<?> clazz) {
        ClassLoader cl = clazz.getClassLoader();
        if (!(cl instanceof URLClassLoader))
            return;
        String property = Jvm.getProperty(JAVA_CLASS_PATH);
        Set<String> jcp = new LinkedHashSet<>();
        Collections.addAll(jcp, property.split(File.pathSeparator));
        jcp.addAll(jcp.stream()
                .map(f -> new File(f).getAbsolutePath())
                .collect(toList()));

        URLClassLoader ucl = (URLClassLoader) cl;
        StringBuilder classpath = new StringBuilder(property);
        for (URL url : ucl.getURLs()) {
            try {
                String path = Paths.get(url.toURI()).toString();
                if (!jcp.contains(path)) {
                    if (isDebugEnabled(Jvm.class))
                        debug().on(Jvm.class, "Adding " + path + " to the classpath");
                    classpath.append(File.pathSeparator).append(path);
                }
            } catch (Throwable e) {
                debug().on(Jvm.class, "Could not add URL " + url + " to classpath");
            }
        }
        System.setProperty(JAVA_CLASS_PATH, classpath.toString());
    }

    /**
     * Returns the System Property associated with the provided {@code systemPropertyKey}
     * parsed as a {@code double} or, if no such parsable System Property exists,
     * returns the provided {@code defaultValue}.
     * <p>
     * Guarantees that Jvm class is initialized before property is read.
     *
     * @param systemPropertyKey to lookup in the System Properties
     * @param defaultValue      to be used if no parsable key association exists
     * @return the System Property associated with the provided {@code systemPropertyKey}
     * parsed as a {@code double} or, if no such parsable System Property exists,
     * returns the provided {@code defaultValue}
     */
    public static double getDouble(final String systemPropertyKey, final double defaultValue) {
        final String value = Jvm.getProperty(systemPropertyKey);
        if (value != null)
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                Jvm.debug().on(Jvm.class, "Unable to parse property " + systemPropertyKey + " as a double " + e);
            }
        return defaultValue;
    }

    /**
     * Returns if a process with the provided {@code pid} process id is alive.
     *
     * @param pid the process id (pid) of the process to check
     * @return if a process with the provided {@code pid} process id is alive
     */
    public static boolean isProcessAlive(long pid) {
        if (isWindows()) {
            final String command = "cmd /c tasklist /FI \"PID eq " + pid + "\"";
            return isProcessAlive0(pid, command);
        }
        if (isLinux() && PROC_EXISTS) {
            return new File("/proc/" + pid).exists();
        }
        if (isMacOSX() || isLinux()) {
            final String command = "ps -p " + pid;
            return isProcessAlive0(pid, command);
        }

        throw new UnsupportedOperationException("Not supported on this OS");
    }

    /**
     * Checks if a process with the given PID is alive by executing the specified command.
     *
     * @param pid     The process ID to check.
     * @param command The command to execute for checking the process.
     * @return {@code true} if the process is alive; {@code false} otherwise.
     */
    private static boolean isProcessAlive0(final long pid, final String command) {

        try {
            // Execute the command to list processes
            InputStreamReader isReader = new InputStreamReader(getRuntime().exec(command).getInputStream());
            final BufferedReader bReader = new BufferedReader(isReader);
            String strLine;
            // Read the command output line by line
            while ((strLine = bReader.readLine()) != null) {
                // Check if the line contains the PID
                if (strLine.contains(" " + pid + " ") || strLine.startsWith(pid + " ")) {
                    return true;
                }
            }

            return false;
        } catch (Exception ex) {
            // Assume process is alive if an exception occurs
            return true;
        }
    }

    /**
     * Checks if the current JVM is running on Azul Zing.
     *
     * @return {@code true} if running on Azul Zing; {@code false} otherwise.
     */
    public static boolean isAzulZing() {
        return IS_AZUL_ZING;
    }

    /**
     * Checks if the current JVM is running on Azul Zulu.
     *
     * @return {@code true} if running on Azul Zulu; {@code false} otherwise.
     */
    public static boolean isAzulZulu() {
        return IS_AZUL_ZULU;
    }

    /**
     * Returns the size of the object header in the JVM.
     * This size is calculated based on the offset of the first field of a class.
     *
     * @return The size of the object header.
     */
    public static int objectHeaderSize() {
        return ObjectHeaderSizeHolder.getSize();
    }


    /**
     * Calculates the object header size for a given class type.
     * If the class type is an array, it returns the array base offset; otherwise, it returns the object header size.
     *
     * @param type The class for which the object header size is to be calculated.
     * @return The object header size or array base offset, depending on the class type.
     */
    public static int objectHeaderSize(Class type) {
        return ObjectHeaderSizeHolder.objectHeaderSize(type);
    }

    /**
     * @return Obtain the model of CPU on Linux or the os.arch on other OSes.
     */
    public static String getCpuClass() {
        return CpuClass.getCpuModel();
    }

    /**
     * Was assertion enabled for the Jvm class when it was initialised.
     *
     * @return if assertions were enabled.
     */
    public static boolean isAssertEnabled() {
        return ASSERT_ENABLED;
    }

    /**
     * Determines if the current thread is a support thread such as "Finalizer" or if its name contains a tilde "~".
     *
     * @return {@code true} if the thread is a support thread; {@code false} otherwise.
     */
    public static boolean supportThread() {
        String name = Thread.currentThread().getName();
        return "Finalizer".equals(name) || name.contains("~");
    }

    /**
     * park the current thread, and stay parked
     */
    public static void park() {
        // LockSupport.park can spuriously return, so we execute in a loop
        while (!Thread.currentThread().isInterrupted())
            LockSupport.park();
    }

    /**
     * Retrieve an annotation of the specified {@code annotationType} that is present on the given
     * {@code annotatedElement}, including considering nested annotations and method inheritance.
     * If the annotation isn't found, this method returns {@code null}.
     *
     * @param annotatedElement the element (e.g., class, method, field) to inspect for annotations.
     * @param annotationType   the desired annotation's type.
     * @param <A>              denotes the annotation type.
     * @return the found annotation of type {@code A} or null if not present.
     */
    public static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        return AnnotationFinder.findAnnotation(annotatedElement, annotationType);
    }

    /**
     * Checks if the given class represents a lambda expression.
     *
     * <p>Java creates synthetic classes for lambda expressions.
     * Typically, the name of these classes contains the "$$Lambda" substring.
     * Note that this naming convention is JVM-specific and can change
     * in future versions. The approach is known to work up to Java 21.
     * </p>
     *
     * @param clazz the class to be checked.
     * @return {@code true} if the class is a lambda, {@code false} otherwise.
     */
    public static boolean isLambdaClass(Class<?> clazz) {
        // Lambdas are marked as synthetic in the JVM
        if (!clazz.isSynthetic()) {
            return false;
        }

    // Check for the typical lambda class name pattern.
    // Note: Relying on the class name can be brittle as it's
    // an implementation detail of the JVM.
    return clazz.getName().contains("$$Lambda");
}

    /**
     * Interface for handling signals.
     * Implementations of this interface define how specific signals should be handled.
     */
    public interface SignalHandler {
        /**
         * Handle a Signal.
         *
         * @param signal The signal to handle.
         */
        void handle(String signal);
    }

    /**
     * Helper class used to prevent closing a {@link FileChannel} upon thread interruption.
     * This is done by overriding the default behavior when an interrupt occurs.
     */
    static final class CommonInterruptible {
        // Thread-local flag to prevent reentrant interrupt handling
        static final ThreadLocal<AtomicBoolean> insideTL = ThreadLocal.withInitial(AtomicBoolean::new);
        private final Class<?> clazz;
        private final FileChannel fc;

        /**
         * Constructs a new instance of {@code CommonInterruptible}.
         *
         * @param clazz The class from which this instance is created.
         * @param fc    The file channel to protect from closing on interrupt.
         */
        CommonInterruptible(Class<?> clazz, FileChannel fc) {
            this.clazz = clazz;
            this.fc = fc;
        }

        /**
         * Custom interrupt handler that prevents the file channel from being closed on thread interrupt.
         */
        public void interrupt() {
            // Check if currently inside an interrupt handling block to prevent reentry
            final AtomicBoolean inside = insideTL.get();
            if (inside.get())
                return;
            inside.set(true);

            // Check if the current thread is interrupted
            boolean interrupted = Thread.currentThread().isInterrupted();

            // Log debug information if debugging is enabled
            if (Jvm.isDebugEnabled(getClass()))
                Jvm.debug().on(clazz, fc + " not closed on interrupt, interrupted= " + interrupted);
            inside.set(false);
        }
    }

    // from https://stackoverflow.com/questions/62550828/is-there-a-lightweight-method-which-adds-a-safepoint-in-java-9
    /**
     * Utility class that forces a safepoint in Java 9 and above.
     * This class uses a simple trick to introduce a lightweight safepoint, useful for certain low-level operations.
     */
    @SuppressWarnings("CanBeFinal")
    static final class Safepoint {
        // Volatile field to ensure visibility and ordering
        private static volatile int one = 1;

        /**
         * Suppresses default constructor, ensuring non-instantiability.
         */
        private Safepoint() {
        }

        /**
         * Forces a safepoint. The loop introduces a safepoint without doing any work.
         * This trick only works in Java 9 and later.
         */
        public static void force() {
            // Empty loop that forces a safepoint
            for (int i = 0; i < one; i++) ;
        }
    }

    /**
     * Class responsible for initializing signal handlers for the application.
     * This class sets up handlers for various signals such as HUP, INT, and TERM.
     */
    static final class InitSignalHandlers {

        static {
            // Set up signal handlers during class loading
            if (!OS.isWindows()) {
                // Not available on Windows
                addSignalHandler("HUP", signalHandlerGlobal);
            }
            addSignalHandler("INT", signalHandlerGlobal);
            addSignalHandler("TERM", signalHandlerGlobal);

        }

        /**
         * Suppresses default constructor, ensuring non-instantiability.
         */
        private InitSignalHandlers() {
        }

        /**
         * Initializes the signal handlers by triggering the static block.
         */
        static void init() {
            // Trigger static initialization block
        }

        /**
         * Adds a signal handler for the specified signal.
         *
         * @param sig            The name of the signal to handle.
         * @param signalHandler  The handler to associate with the signal.
         */
        private static void addSignalHandler(final String sig, final sun.misc.SignalHandler signalHandler) {
            try {
                // Register the signal handler
                Signal.handle(new Signal(sig), signalHandler);

            } catch (IllegalArgumentException e) {
                // Handle the case when -Xrs is specified
                Jvm.warn().on(signalHandler.getClass(), "Unable to add a signal handler", e);
            }
        }

    }

    /**
     * A signal handler that chains multiple signal handlers together.
     * When a signal is received, it passes the signal to all registered handlers.
     */
    static final class ChainedSignalHandler implements sun.misc.SignalHandler {
        final List<sun.misc.SignalHandler> handlers = new CopyOnWriteArrayList<>();
        final List<SignalHandler> handlers2 = new CopyOnWriteArrayList<>();

        /**
         * Handles the received signal by passing it to all registered handlers.
         *
         * @param signal The signal to handle.
         */
        @Override
        public void handle(final Signal signal) {
            // Handle signal using Java signal handlers
            for (sun.misc.SignalHandler handler : handlers) {
                try {
                    if (handler != null)
                        handler.handle(signal);
                } catch (Throwable t) {
                    Jvm.warn().on(this.getClass(), "Problem handling signal", t);
                }
            }
            // Handle signal using custom signal handlers
            for (SignalHandler handler : handlers2) {
                try {
                    if (handler != null)
                        handler.handle(signal.getName());
                } catch (Throwable t) {
                    Jvm.warn().on(this.getClass(), "Problem handling signal", t);
                }
            }
        }
    }

    /**
     * Checks if the current execution is a JUnit test.
     * This is determined by scanning the stack trace for any classes that contain ".junit" in their name.
     *
     * @return {@code true} if the current execution is within a JUnit test; {@code false} otherwise.
     */
    private static boolean isJUnitTest0() {
        // Iterate over all stack traces of all threads
        for (StackTraceElement[] stackTrace : Thread.getAllStackTraces().values()) {
            for (StackTraceElement element : stackTrace) {
                // Check if any stack trace element contains ".junit"
                if (element.getClassName().contains(".junit")) {
                    return true;
                }
            }
        }
        return false;
    }
}
