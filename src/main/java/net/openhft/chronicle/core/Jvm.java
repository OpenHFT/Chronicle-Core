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
import net.openhft.chronicle.core.internal.Bootstrap;
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
 * Utility class to access information in the JVM.
 */
public final class Jvm {

    public static final String JAVA_CLASS_PATH = "java.class.path";
    public static final String SYSTEM_PROPERTIES = "system.properties";
    // These are the exception handlers used initially, and restored when resetExceptionHandlers() is called
    private static final ExceptionHandler DEFAULT_ERROR_EXCEPTION_HANDLER = Slf4jExceptionHandler.ERROR;
    private static final ExceptionHandler DEFAULT_WARN_EXCEPTION_HANDLER = Slf4jExceptionHandler.WARN;
    private static final ExceptionHandler DEFAULT_PERF_EXCEPTION_HANDLER = Slf4jExceptionHandler.PERF;
    private static final ExceptionHandler DEFAULT_DEBUG_EXCEPTION_HANDLER = Slf4jExceptionHandler.DEBUG;
    private static final String PROC = "/proc";
    private static final List<String> INPUT_ARGUMENTS = getRuntimeMXBean().getInputArguments();
    private static final String INPUT_ARGUMENTS2 = " " + String.join(" ", INPUT_ARGUMENTS);
    private static final boolean IS_DEBUG = Jvm.getBoolean("debug", INPUT_ARGUMENTS2.contains("jdwp"));
    // e.g-verbose:gc  -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=dumponexit=true,filename=myrecording.jfr,settings=profile -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints
    private static final boolean IS_FLIGHT_RECORDER = Jvm.getBoolean("jfr", INPUT_ARGUMENTS2.contains(" -XX:+FlightRecorder"));
    private static final boolean IS_COVERAGE = INPUT_ARGUMENTS2.contains("coverage");
    private static final int COMPILE_THRESHOLD = getCompileThreshold0();
    private static final boolean REPORT_UNOPTIMISED;
    private static final Supplier<Long> reservedMemory;
    private static final boolean DISABLE_DEBUG = Jvm.getBoolean("disable.debug.info");
    @NotNull
    private static final ThreadLocalisedExceptionHandler ERROR = new ThreadLocalisedExceptionHandler(DEFAULT_ERROR_EXCEPTION_HANDLER);
    @NotNull
    private static final ThreadLocalisedExceptionHandler WARN = new ThreadLocalisedExceptionHandler(DEFAULT_WARN_EXCEPTION_HANDLER);
    @NotNull
    private static final ThreadLocalisedExceptionHandler PERF = new ThreadLocalisedExceptionHandler(DEFAULT_PERF_EXCEPTION_HANDLER);
    @NotNull
    private static final ExceptionHandler DEBUG;
    private static final long MAX_DIRECT_MEMORY;
    private static final boolean SAFEPOINT_ENABLED;
    private static final Map<Class<?>, ClassMetrics> CLASS_METRICS_MAP = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Integer> PRIMITIVE_SIZE = ofUnmodifiable(
            entry(boolean.class, 1),
            entry(byte.class, Byte.BYTES),
            entry(char.class, Character.BYTES),
            entry(short.class, Short.BYTES),
            entry(int.class, Integer.BYTES),
            entry(float.class, Float.BYTES),
            entry(long.class, Long.BYTES),
            entry(double.class, Double.BYTES)
    );
    private static final MethodHandle onSpinWaitMH;
    private static final ChainedSignalHandler signalHandlerGlobal;
    private static boolean RESOURCE_TRACING;
    private static final boolean PROC_EXISTS = new File(PROC).exists();
    @SuppressWarnings("unused")
    private static volatile Thread s_blackHole;

    static {
        Logger logger = LoggerFactory.getLogger(Jvm.class);

        if (!isJUnitTest0()) {
            // Eagerly initialise Posix & Affinity
            try {
                PosixAPI.posix();
            } catch (Error e) {
                logger.debug("Unable to load PosixAPI ", e);
            }

            try {
                Class.forName("net.openhft.affinity.Affinity");
            } catch (ClassNotFoundException e) {
                logger.trace("Unable to load Affinity", e);
            }
        }
        if (DISABLE_DEBUG) {
            DEBUG = NullExceptionHandler.NOTHING;
        } else {
            DEBUG = new ThreadLocalisedExceptionHandler(DEFAULT_DEBUG_EXCEPTION_HANDLER);
        }

        findAndLoadSystemProperties();

        MAX_DIRECT_MEMORY = maxDirectMemory0();

        Supplier<Long> reservedMemoryGetter;
        try {
            final Class<?> bitsClass = Class.forName("java.nio.Bits");
            final Field firstTry = getFieldOrNull(bitsClass, "reservedMemory");
            final Field f = firstTry != null ? firstTry : getField(bitsClass, "RESERVED_MEMORY");
            if (f.getType() == AtomicLong.class) {
                AtomicLong reservedMemory = (AtomicLong) f.get(null);
                reservedMemoryGetter = reservedMemory::get;
            } else {
                reservedMemoryGetter = ThrowingSupplier.asSupplier(() -> f.getLong(null));
            }
        } catch (Exception e) {
            System.err.println(Jvm.class.getName() + ": Unable to determine the reservedMemory value, will always report 0");
            reservedMemoryGetter = () -> 0L;
        }
        reservedMemory = reservedMemoryGetter;
        signalHandlerGlobal = new ChainedSignalHandler();

        onSpinWaitMH = getOnSpinWait();

        boolean disablePerfInfo = Jvm.getBoolean("disable.perf.info");
        if (disablePerfInfo)
            PERF.defaultHandler(NullExceptionHandler.NOTHING);

        SAFEPOINT_ENABLED = Jvm.getBoolean("jvm.safepoint.enabled");

        RESOURCE_TRACING = Jvm.getBoolean("jvm.resource.tracing");

        if (DISABLE_DEBUG)
            logger.info("-Ddisable.debug.info turned of debug logging");
        if (logger.isInfoEnabled())
            logger.info("Chronicle core loaded from " + Jvm.class.getProtectionDomain().getCodeSource().getLocation());
        if (RESOURCE_TRACING && !Jvm.getBoolean("disable.resource.warning"))
            logger.warn("Resource tracing is turned on. If you are performance testing or running in PROD you probably don't want this");
        REPORT_UNOPTIMISED = Jvm.getBoolean("report.unoptimised");

        ChronicleInit.postInit();
    }

    private static MethodHandle getOnSpinWait() {
        MethodType voidType = MethodType.methodType(void.class);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            if (isJava9Plus())
                return lookup.findStatic(Thread.class, "onSpinWait", voidType);
        } catch (Exception ignored) {
        }
        try {
            return lookup.findStatic(Safepoint.class, "force", voidType);
        } catch (Exception ignored) {
        }
        return null;
    }

    // Suppresses default constructor, ensuring non-instantiability.
    private Jvm() {
    }

    public static void reportUnoptimised() {
        if (!REPORT_UNOPTIMISED)
            return;
        final StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        int i = 0;
        while (i < stes.length)
            if (stes[i++].getMethodName().equals("reportUnoptimised"))
                break;
        while (i < stes.length)
            if (stes[i++].getMethodName().equals("<clinit>"))
                break;

        Jvm.warn().on(Jvm.class, "Reporting usage of unoptimised method " + stes[i]);
    }

    private static void findAndLoadSystemProperties() {
        String systemProperties = Jvm.getProperty(SYSTEM_PROPERTIES);
        boolean wasSet = true;
        if (systemProperties == null) {
            if (new File(SYSTEM_PROPERTIES).exists())
                systemProperties = SYSTEM_PROPERTIES;
            else if (new File("../" + SYSTEM_PROPERTIES).exists())
                systemProperties = "../" + SYSTEM_PROPERTIES;
            else {
                systemProperties = SYSTEM_PROPERTIES;
                wasSet = false;
            }
        }
        loadSystemProperties(systemProperties, wasSet);
    }

    public static void init() {
        // force static initialisation
        ChronicleInit.init();
    }

    private static void loadSystemProperties(final String name, final boolean wasSet) {
        try {
            final ClassLoader classLoader = Jvm.class.getClassLoader();
            InputStream is0 = classLoader == null ? null : classLoader.getResourceAsStream(name);
            if (is0 == null) {
                File file = new File(name);
                if (file.exists())
                    is0 = new FileInputStream(file);
            }
            try (InputStream is = is0) {
                if (is == null) {
                    (wasSet ? Slf4jExceptionHandler.WARN : Slf4jExceptionHandler.DEBUG)
                            .on(Jvm.class, "No " + name + " file found");

                } else {
                    final Properties prop = new Properties();
                    prop.load(is);
                    // if user has specified a property using -D then don't overwrite it from system.properties
                    prop.forEach((o, o2) -> System.getProperties().putIfAbsent(o, o2));
                    Slf4jExceptionHandler.DEBUG.on(Jvm.class, "Loaded " + name + " with " + prop);
                }
            }
        } catch (Exception e) {
            Slf4jExceptionHandler.WARN.on(Jvm.class, "Error loading " + name, e);
        }
    }

    private static int getCompileThreshold0() {
        for (String inputArgument : INPUT_ARGUMENTS) {
            final String prefix = "-XX:CompileThreshold=";
            if (inputArgument.startsWith(prefix)) {
                try {
                    return Integer.parseInt(inputArgument.substring(prefix.length()));
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            }
        }
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
        System.err.println(Jvm.class.getName() + ": Unable to determine PID, picked 1 as a PID");
        return rpid;
    }

    /**
     * Cast any Throwable (e.g. a checked exception) to a RuntimeException.
     *
     * @param throwable to cast
     * @param <T>       the type of the Throwable
     * @return this method will never return a Throwable instance, it will just throw it.
     * @throws T the throwable as an unchecked throwable
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException rethrow(Throwable throwable) throws T {
        throw (T) throwable; // rely on vacuous cast
    }

    /**
     * Append the provided {@code StackTraceElements} to the provided {@code stringBuilder} trimming some internal methods.
     *
     * @param stringBuilder      to append to
     * @param stackTraceElements stack trace elements
     */
    public static void trimStackTrace(@NotNull final StringBuilder stringBuilder, @NotNull final StackTraceElement... stackTraceElements) {
        final int first = trimFirst(stackTraceElements);
        final int last = trimLast(first, stackTraceElements);
        for (int i = first; i <= last; i++)
            stringBuilder.append("\n\tat ").append(stackTraceElements[i]);
    }

    static int trimFirst(@NotNull final StackTraceElement[] stes) {
        if (stes.length > 2 && stes[1].getMethodName().endsWith("afepoint"))
            return 2;
        int first = 0;
        for (; first < stes.length; first++)
            if (!isInternal(stes[first].getClassName()))
                break;
        return Math.max(0, first - 2);
    }

    public static int trimLast(final int first, @NotNull final StackTraceElement[] stes) {
        int last = stes.length - 1;
        for (; first < last; last--)
            if (!isInternal(stes[last].getClassName()))
                break;
        if (last < stes.length - 1) last++;
        return last;
    }

    static boolean isInternal(@NotNull final String className) {
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
                                   final Class<?>... argTypes) {
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
        return uncheckedCast(target);
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

    public static void resetExceptionHandlers() {
        setErrorExceptionHandler(DEFAULT_ERROR_EXCEPTION_HANDLER);
        setWarnExceptionHandler(DEFAULT_WARN_EXCEPTION_HANDLER);
        setDebugExceptionHandler(DEFAULT_DEBUG_EXCEPTION_HANDLER);
        setPerfExceptionHandler(DEFAULT_PERF_EXCEPTION_HANDLER);
    }

    public static void setErrorExceptionHandler(ExceptionHandler exceptionHandler) {
        ERROR.defaultHandler(exceptionHandler).resetThreadLocalHandler();
    }

    public static void setWarnExceptionHandler(ExceptionHandler exceptionHandler) {
        WARN.defaultHandler(exceptionHandler).resetThreadLocalHandler();
    }

    public static void setDebugExceptionHandler(ExceptionHandler exceptionHandler) {
        if (DEBUG instanceof ThreadLocalisedExceptionHandler)
            ((ThreadLocalisedExceptionHandler) DEBUG).defaultHandler(exceptionHandler).resetThreadLocalHandler();
    }

    public static void setPerfExceptionHandler(ExceptionHandler exceptionHandler) {
        PERF.defaultHandler(exceptionHandler).resetThreadLocalHandler();
    }

    public static void disableDebugHandler() {
        setDebugExceptionHandler(null);
    }

    public static void disablePerfHandler() {
        setPerfExceptionHandler(null);
    }

    public static void disableWarnHandler() {
        setWarnExceptionHandler(null);
    }

    @NotNull
    public static Map<ExceptionKey, Integer> recordExceptions() {
        return recordExceptions(true);
    }

    @NotNull
    public static Map<ExceptionKey, Integer> recordExceptions(boolean debug) {
        return recordExceptions(debug, false);
    }

    @NotNull
    public static Map<ExceptionKey, Integer> recordExceptions(boolean debug, boolean exceptionsOnly) {
        return recordExceptions(debug, exceptionsOnly, true);
    }

    @NotNull
    public static Map<ExceptionKey, Integer> recordExceptions(final boolean debug,
                                                              final boolean exceptionsOnly,
                                                              final boolean logToSlf4j) {
        final Map<ExceptionKey, Integer> map = Collections.synchronizedMap(new LinkedHashMap<>());
        setErrorExceptionHandler(recordingExceptionHandler(LogLevel.ERROR, map, exceptionsOnly, logToSlf4j));
        setWarnExceptionHandler(recordingExceptionHandler(LogLevel.WARN, map, exceptionsOnly, logToSlf4j));
        setPerfExceptionHandler(debug
                ? recordingExceptionHandler(LogLevel.PERF, map, exceptionsOnly, logToSlf4j)
                : logToSlf4j ? Slf4jExceptionHandler.PERF : NullExceptionHandler.NOTHING);
        setDebugExceptionHandler(debug
                ? recordingExceptionHandler(LogLevel.DEBUG, map, exceptionsOnly, logToSlf4j)
                : logToSlf4j ? Slf4jExceptionHandler.DEBUG : NullExceptionHandler.NOTHING);
        return map;
    }

    private static ExceptionHandler recordingExceptionHandler(final LogLevel logLevel,
                                                              final Map<ExceptionKey, Integer> map,
                                                              final boolean exceptionsOnly,
                                                              final boolean logToSlf4j) {
        final ExceptionHandler eh = new RecordingExceptionHandler(logLevel, map, exceptionsOnly);
        if (logToSlf4j)
            return new ChainedExceptionHandler(eh, Slf4jExceptionHandler.valueOf(logLevel));
        return eh;
    }

    public static boolean hasException(@NotNull final Map<ExceptionKey, Integer> exceptions) {

        final Iterator<ExceptionKey> iterator = exceptions.keySet().iterator();
        while (iterator.hasNext()) {
            final ExceptionKey k = iterator.next();
            if (k.level() != LogLevel.DEBUG && k.level() != LogLevel.PERF)
                return true;
        }

        return false;
    }

    public static void setExceptionHandlers(@Nullable final ExceptionHandler error,
                                            @Nullable final ExceptionHandler warn,
                                            @Nullable final ExceptionHandler debug) {

        ERROR.defaultHandler(error);
        WARN.defaultHandler(warn);
        if (DEBUG instanceof ThreadLocalisedExceptionHandler)
            ((ThreadLocalisedExceptionHandler) DEBUG).defaultHandler(debug);
    }

    public static void setExceptionHandlers(@Nullable final ExceptionHandler error,
                                            @Nullable final ExceptionHandler warn,
                                            @Nullable final ExceptionHandler debug,
                                            @Nullable final ExceptionHandler perf) {
        setExceptionHandlers(error, warn, debug);
        PERF.defaultHandler(perf);
    }

    public static void setThreadLocalExceptionHandlers(@Nullable final ExceptionHandler error,
                                                       @Nullable final ExceptionHandler warn,
                                                       @Nullable final ExceptionHandler debug) {
        ERROR.threadLocalHandler(error);
        WARN.threadLocalHandler(warn);
        if (DEBUG instanceof ThreadLocalisedExceptionHandler)
            ((ThreadLocalisedExceptionHandler) DEBUG).threadLocalHandler(debug);
    }

    public static void setThreadLocalExceptionHandlers(@Nullable final ExceptionHandler error,
                                                       @Nullable final ExceptionHandler warn,
                                                       @Nullable final ExceptionHandler debug,
                                                       @Nullable final ExceptionHandler perf) {
        setThreadLocalExceptionHandlers(error, warn, debug);
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

    public static void dumpException(@NotNull final Map<ExceptionKey, Integer> exceptions) {
        final Slf4jExceptionHandler warn = Slf4jExceptionHandler.WARN;
        for (@NotNull Entry<ExceptionKey, Integer> entry : exceptions.entrySet()) {
            final ExceptionKey key = entry.getKey();
            warn.on(Jvm.class, key.level() + " " + key.clazz().getSimpleName() + " " + key.message(), key.throwable());
            final Integer value = entry.getValue();
            if (value > 1)
                warn.on(Jvm.class, "Repeated " + value + " times");
        }
        resetExceptionHandlers();
    }

    public static boolean isDebugEnabled(final Class<?> aClass) {
        return DEBUG.isEnabled(aClass);
    }

    public static boolean isPerfEnabled(final Class<?> aClass) {
        return PERF.isEnabled(aClass);
    }

    private static long maxDirectMemory0() {
        try {
            final Class<?> clz;
            if (isJava9Plus()) {
                clz = Class.forName("jdk.internal.misc.VM");
            } else {
                clz = Class.forName("sun.misc.VM");
            }

            final Field f = getField(clz, "directMemory");
            return f.getLong(null);
        } catch (Exception e) {
            // ignore
        }
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

    private static ClassMetrics getClassMetrics(final Class<?> c) {
        assert !c.isArray();
        final Class<?> superclass = c.getSuperclass();
        int start = Integer.MAX_VALUE;
        int end = 0;
        for (Field f : c.getDeclaredFields()) {
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0 || !f.getType().isPrimitive())
                continue;
            int start0 = Math.toIntExact(UnsafeMemory.unsafeObjectFieldOffset(f));
            int size = PRIMITIVE_SIZE.get(f.getType());
            start = Math.min(start0, start);
            end = Math.max(start0 + size, end);
        }
        if (superclass != null && superclass != Object.class) {
            final ClassMetrics cm0 = getClassMetrics(superclass);
            start = Math.min(cm0.offset(), start);
            end = Math.max(cm0.offset() + cm0.length(), end);
            validateClassMetrics(superclass, start, end);
        }

        validateClassMetrics(c, start, end);

        return new ClassMetrics(start, end - start);
    }

    private static void validateClassMetrics(final Class<?> c,
                                             final int start,
                                             final int end) {
        for (Field f : c.getDeclaredFields()) {
            if ((f.getModifiers() & Modifier.STATIC) != 0 || f.getType().isPrimitive())
                continue;
            final int start0 = Math.toIntExact(UnsafeMemory.unsafeObjectFieldOffset(f));
            if (start <= start0 && start0 < end) {
                rethrow(new IllegalArgumentException(c + " is not suitable for raw copies due to " + f));
            }
        }
    }

    /**
     * Returns the user's home directory (e.g. "/home/alice") or "."
     * if the user's home director cannot be determined.
     *
     * @return the user's home directory (e.g. "/home/alice") or "."
     * if the user's home director cannot be determined
     */
    @NotNull
    public static String userHome() {
        return System.getProperty("user.home", ".");
    }

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
     *         <caption>
     *             This table illustrates examples of various string inputs representing memory sizes and their corresponding parsed outputs in bytes.
     *             It demonstrates how strings with different suffixes and formats are converted to their respective byte equivalents using factors
     *             like kilobytes (KB), megabytes (MB), gigabytes (GB), and terabytes (TB), where these units are powers of 2.
     *         </caption>
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
        factor *= (long) number;
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

    private static void doNotCloseOnInterrupt8(final Class<?> clazz, final FileChannel fc) {
        try {
            final Field field = AbstractInterruptibleChannel.class
                    .getDeclaredField("interruptor");
            ClassUtil.setAccessible(field);
            final CommonInterruptible ci = new CommonInterruptible(clazz, fc);
            field.set(fc, (Interruptible) thread -> ci.interrupt());
        } catch (Throwable e) {
            Jvm.warn().on(clazz, "Couldn't disable close on interrupt", e);
        }
    }

    // based on a solution by https://stackoverflow.com/users/9199167/max-vollmer
    // https://stackoverflow.com/a/52262779/57695
    private static void doNotCloseOnInterrupt9(final Class<?> clazz, final FileChannel fc) {
        try {
            final Field field = AbstractInterruptibleChannel.class.getDeclaredField("interruptor");
            final Class<?> interruptibleClass = field.getType();
            ClassUtil.setAccessible(field);
            final CommonInterruptible ci = new CommonInterruptible(clazz, fc);
            Class<?>[] interfaces = {interruptibleClass};
            field.set(fc, Proxy.newProxyInstance(
                    interruptibleClass.getClassLoader(),
                    interfaces,
                    (p, m, a) -> {
                        if (m.getDeclaringClass() != Object.class)
                            ci.interrupt();
                        return ObjectUtils.defaultValue(m.getReturnType());
                    }));
        } catch (Throwable e) {
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

    private static boolean isProcessAlive0(final long pid, final String command) {

        try {
            InputStreamReader isReader = new InputStreamReader(
                    getRuntime().exec(command).getInputStream());

            final BufferedReader bReader = new BufferedReader(isReader);
            String strLine;
            while ((strLine = bReader.readLine()) != null) {
                if (strLine.contains(" " + pid + " ") || strLine.startsWith(pid + " ")) {
                    return true;
                }
            }

            return false;
        } catch (Exception ex) {
            return true;
        }
    }

    public static boolean isAzulZing() {
        return IS_AZUL_ZING;
    }

    public static boolean isAzulZulu() {
        return IS_AZUL_ZULU;
    }

    /**
     * Returns the size of the object header in the JVM.
     * This size is calculated based on the offset of the first field of a class.
     *
     * @return The size of the object header.
     */
    @Deprecated(/* to be removed in x.27, use net.openhft.chronicle.bytes.BytesUtil.triviallyCopyableStart */)
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
    @Deprecated(/* to be removed in x.27, use net.openhft.chronicle.bytes.BytesUtil.triviallyCopyableStart for POJOs */)
    public static int objectHeaderSize(Class<?> type) {
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
     * Performs an unchecked cast of an object to the target type {@code T}. This method
     * bypasses generic type checks, allowing for casting without explicit type checking,
     * offering a convenient way to avoid compiler warnings for unchecked casts.
     * <p>
     * Note: Use with caution as improper use can lead to {@link ClassCastException} at runtime
     * if the object is not of type {@code T}. Intended for situations where the type safety is
     * guaranteed through other means but cannot be expressed without generic type warnings.
     *
     * @param <T> the target type to cast to
     * @param o   the object to be cast
     * @return the casted object of type {@code T}
     * @throws ClassCastException if the object cannot be casted to the target type {@code T}
     *                            (a runtime risk due to type erasure)
     */
    @SuppressWarnings("unchecked")
    public static <T> T uncheckedCast(Object o) {
        return (T) o;
    }

    /**
     * Performs an unchecked cast of an array of objects to an array of the target type {@code T[]}.
     * This method bypasses generic array type checks, facilitating casting without explicit array
     * type checking.
     * <p>
     * Note: Use with caution as improper use can lead to {@link ClassCastException} at runtime
     * if the objects in the array cannot be cast to type {@code T[]}. This method is useful when
     * the programmer is confident in the implicit type safety of the operation but wishes to avoid
     * compiler warnings about unchecked operations.
     *
     * @param <T> the target component type of the array to cast to
     * @param o   the object array to be cast
     * @return the casted object array of type {@code T[]}
     * @throws ClassCastException if the objects in the array cannot be cast to the component type {@code T[]}
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] uncheckedCast(Object[] o) {
        return (T[]) o;
    }

    /**
     * Performs an unchecked cast of a {@code Class} object to {@code Class<T>}. This operation
     * is particularly useful in scenarios involving reflection where generic type parameters are
     * known but cannot be statically enforced by the compiler.
     * <p>
     * By bypassing compile-time generic type checks, it provides a way to work with generic types
     * in a dynamic context at the cost of compile-time type safety.
     * <p>
     * Note: Use with caution as improper use can lead to a {@link ClassCastException} at runtime
     * if the class object cannot actually be cast to {@code Class<T>}. This method should be
     * employed when there is certainty about the underlying type compatibility.
     *
     * @param <T> the target generic type to cast the class to
     * @param o   the class object to be cast
     * @return the class object cast to {@code Class<T>}
     * @throws ClassCastException if the class object cannot be cast to {@code Class<T>}
     *                            (a runtime risk inherent to unchecked casting)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Class<T> uncheckedCast(Class<?> o) {
        return (Class<T>) o;
    }

    /**
     * Retrieves the unique identifier of the current thread.
     * <p>
     * This method provides a straightforward way to obtain the ID of the thread
     * from which the method is called. Currently, it uses {@link Thread#currentThread()}
     * and {@link Thread#getId()} to accomplish this. The method is safe to use across
     * various versions of Java.
     * <p>
     * Note: In future versions, this implementation may be updated to use {@code Thread.threadId()}
     * or another updated mechanism for obtaining the thread ID, as newer Java versions from version 19
     * deprecate the current approach.
     *
     * @return the identifier of the current thread
     */
    @SuppressWarnings("deprecation")
    public static long currentThreadId() {
        return Thread.currentThread().getId();
    }

    public interface SignalHandler {
        /**
         * Handle a Signal
         *
         * @param signal to handle
         */
        void handle(String signal);
    }

    static final class CommonInterruptible {
        static final ThreadLocal<AtomicBoolean> insideTL = ThreadLocal.withInitial(AtomicBoolean::new);
        private final Class<?> clazz;
        private final FileChannel fc;

        CommonInterruptible(Class<?> clazz, FileChannel fc) {
            this.clazz = clazz;
            this.fc = fc;
        }

        public void interrupt() {
            final AtomicBoolean inside = insideTL.get();
            if (inside.get())
                return;
            inside.set(true);
            boolean interrupted = Thread.currentThread().isInterrupted();
            if (Jvm.isDebugEnabled(getClass()))
                Jvm.debug().on(clazz, fc + " not closed on interrupt, interrupted= " + interrupted);
            inside.set(false);
        }
    }

    // from https://stackoverflow.com/questions/62550828/is-there-a-lightweight-method-which-adds-a-safepoint-in-java-9
    @SuppressWarnings("CanBeFinal")
    static final class Safepoint {

        // must be volatile
        private static volatile int one = 1;

        // Suppresses default constructor, ensuring non-instantiability.
        private Safepoint() {
        }

        public static void force() {
            // trick only works from Java 9+
            for (int i = 0; i < one; i++) ;
        }
    }

    static final class InitSignalHandlers {

        static {
            if (!OS.isWindows()) {
                // Not available on Windows.
                addSignalHandler("HUP", signalHandlerGlobal);
            }
            addSignalHandler("INT", signalHandlerGlobal);
            addSignalHandler("TERM", signalHandlerGlobal);

        }

        // Suppresses default constructor, ensuring non-instantiability.
        private InitSignalHandlers() {
        }

        static void init() {
            // trigger static block
        }

        private static void addSignalHandler(final String sig, final sun.misc.SignalHandler signalHandler) {
            try {
                Signal.handle(new Signal(sig), signalHandler);

            } catch (IllegalArgumentException e) {
                // When -Xrs is specified the user is responsible for
                // ensuring that shutdown hooks are run by calling
                // System.exit()
                Jvm.warn().on(signalHandler.getClass(), "Unable add a signal handler", e);
            }
        }

    }

    static final class ChainedSignalHandler implements sun.misc.SignalHandler {
        final List<sun.misc.SignalHandler> handlers = new CopyOnWriteArrayList<>();
        final List<SignalHandler> handlers2 = new CopyOnWriteArrayList<>();

        @Override
        public void handle(final Signal signal) {
            for (sun.misc.SignalHandler handler : handlers) {
                try {
                    if (handler != null)
                        handler.handle(signal);
                } catch (Throwable t) {
                    Jvm.warn().on(this.getClass(), "Problem handling signal", t);
                }
            }
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

    private static boolean isJUnitTest0() {
        for (StackTraceElement[] stackTrace : Thread.getAllStackTraces().values()) {
            for (StackTraceElement element : stackTrace) {
                if (element.getClassName().contains(".junit")) {
                    return true;
                }
            }
        }
        return false;
    }
}
