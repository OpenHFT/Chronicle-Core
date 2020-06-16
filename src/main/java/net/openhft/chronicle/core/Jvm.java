/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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
import net.openhft.chronicle.core.onoes.*;
import net.openhft.chronicle.core.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

/**
 * Utility class to access information in the JVM.
 */
public enum Jvm {
    ;

    private static final List<String> INPUT_ARGUMENTS = getRuntimeMXBean().getInputArguments();
    private static final int COMPILE_THRESHOLD = getCompileThreshold0();
    private static final boolean IS_DEBUG = INPUT_ARGUMENTS.toString().contains("jdwp") || Boolean.getBoolean("debug");

    // e.g-verbose:gc  -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=dumponexit=true,filename=myrecording.jfr,settings=profile -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints
    private static final boolean IS_FLIGHT_RECORDER = (" " + getRuntimeMXBean().getInputArguments()).contains(" -XX:+FlightRecorder") || Boolean.getBoolean("jfr");
    ;
    private static final Supplier<Long> reservedMemory;
    private static final boolean IS_64BIT = is64bit0();
    private static final int PROCESS_ID = getProcessId0();
    @NotNull
    private static final ThreadLocalisedExceptionHandler FATAL = new ThreadLocalisedExceptionHandler(Slf4jExceptionHandler.FATAL);
    @NotNull
    private static final ThreadLocalisedExceptionHandler WARN = new ThreadLocalisedExceptionHandler(Slf4jExceptionHandler.WARN);
    @NotNull
    private static final ThreadLocalisedExceptionHandler PERF = new ThreadLocalisedExceptionHandler(Slf4jExceptionHandler.PERF);
    @NotNull
    private static final ThreadLocalisedExceptionHandler DEBUG = new ThreadLocalisedExceptionHandler(Slf4jExceptionHandler.DEBUG);

    private static final int JVM_JAVA_MAJOR_VERSION;
    private static final boolean IS_JAVA_9_PLUS;
    private static final boolean IS_JAVA_12_PLUS;
    private static final boolean IS_JAVA_14_PLUS;
    private static final long MAX_DIRECT_MEMORY;
    private static final boolean SAFEPOINT_ENABLED;
    private static final boolean IS_ARM = Boolean.getBoolean("jvm.isarm") ||
            System.getProperty("os.arch", "?").startsWith("arm") || System.getProperty("os.arch", "?").startsWith("aarch");
    private static final Map<Class, ClassMetrics> CLASS_METRICS_MAP =
            new ConcurrentHashMap<>();
    private static final Map<Class, Integer> PRIMITIVE_SIZE = new HashMap<Class, Integer>() {{
        put(boolean.class, 1);
        put(byte.class, 1);
        put(char.class, 2);
        put(short.class, 2);
        put(int.class, 4);
        put(float.class, 4);
        put(long.class, 8);
        put(double.class, 8);
    }};

    private static final MethodHandle setAccessible0_Method;
    private static final MethodHandle onSpinWaitMH;
    private static final ChainedSignalHandler signalHandlerGlobal;
    private static final boolean RESOURCE_TRACING;

    static {
        JVM_JAVA_MAJOR_VERSION = getMajorVersion0();
        IS_JAVA_9_PLUS = JVM_JAVA_MAJOR_VERSION > 8; // IS_JAVA_9_PLUS value is used in maxDirectMemory0 method.
        IS_JAVA_12_PLUS = JVM_JAVA_MAJOR_VERSION > 11;
        IS_JAVA_14_PLUS = JVM_JAVA_MAJOR_VERSION > 13;
        MAX_DIRECT_MEMORY = maxDirectMemory0();

        Supplier<Long> reservedMemoryGetter;
        try {
            final Class<?> bitsClass = Class.forName("java.nio.Bits");
            Field f;
            try {
                f = bitsClass.getDeclaredField("reservedMemory");
            } catch (NoSuchFieldException e) {
                f = bitsClass.getDeclaredField("RESERVED_MEMORY");
            }
            long offset = UNSAFE.staticFieldOffset(f);
            Object base = UNSAFE.staticFieldBase(f);
            if (f.getType() == AtomicLong.class) {
                AtomicLong reservedMemory = (AtomicLong) UNSAFE.getObject(base, offset);
                reservedMemoryGetter = reservedMemory::get;
            } else {
                reservedMemoryGetter = () -> UNSAFE.getLong(base, offset);
            }
        } catch (Exception e) {
            System.err.println(Jvm.class.getName() + ": Unable to determine the reservedMemory value, will always report 0");
            reservedMemoryGetter = () -> 0L;
        }
        reservedMemory = reservedMemoryGetter;
        signalHandlerGlobal = new ChainedSignalHandler();

        MethodHandle onSpinWait = null;
        if (IS_JAVA_9_PLUS) {
            try {
                onSpinWait = MethodHandles.lookup()
                        .findStatic(Thread.class, "onSpinWait", MethodType.methodType(Void.TYPE));
            } catch (Exception ignored) {
            }
        }
        onSpinWaitMH = onSpinWait;
        setAccessible0_Method = get_setAccessible0_Method();

        findAndLoadSystemProperties();

        SAFEPOINT_ENABLED = Boolean.getBoolean("jvm.safepoint.enabled");

        String resourceTracing = System.getProperty("jvm.resource.tracing");
        RESOURCE_TRACING = resourceTracing != null &&
                (resourceTracing.isEmpty() || Boolean.parseBoolean(resourceTracing));
    }

    private static void findAndLoadSystemProperties() {
        String systemProperties = System.getProperty("system.properties");
        boolean wasSet = true;
        if (systemProperties == null) {
            if (new File("system.properties").exists())
                systemProperties = "system.properties";
            else if (new File("../system.properties").exists())
                systemProperties = "../system.properties";
            else {
                systemProperties = "system.properties";
                wasSet = false;
            }
        }
        loadSystemProperties(systemProperties, wasSet);
    }

    private static MethodHandle get_setAccessible0_Method() {
        if (!IS_JAVA_9_PLUS) {
            return null;
        }
        MethodType signature = MethodType.methodType(boolean.class, boolean.class);
        try {
            // Access privateLookupIn() reflectively to support compilation with JDK 8
            Method privateLookupIn = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) privateLookupIn.invoke(null, AccessibleObject.class, MethodHandles.lookup());
            return lookup.findVirtual(AccessibleObject.class, "setAccessible0", signature);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void init() {
        // force static initialisation
    }

    private static void loadSystemProperties(String name, boolean wasSet) {
        try {
            ClassLoader classLoader = Jvm.class.getClassLoader();
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
                    Properties prop = new Properties();
                    prop.load(is);
                    System.getProperties().putAll(prop);
                    Slf4jExceptionHandler.DEBUG.on(Jvm.class, "Loaded " + name + " with " + prop);
                }
            }
        } catch (Exception e) {
            Slf4jExceptionHandler.WARN.on(Jvm.class, "Error loading " + name, e);
        }
    }

    private static int getCompileThreshold0() {
        for (@NotNull String inputArgument : INPUT_ARGUMENTS) {
            @NotNull String prefix = "-XX:CompileThreshold=";
            if (inputArgument.startsWith(prefix)) {
                return Integer.parseInt(inputArgument.substring(prefix.length()));
            }
        }
        return 10000;
    }

    public static int compileThreshold() {
        return COMPILE_THRESHOLD;
    }

    public static int majorVersion() {
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

    private static boolean is64bit0() {
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

    public static int getProcessId() {
        return PROCESS_ID;
    }

    private static int getProcessId0() {
        String pid = null;
        final File self = new File("/proc/self");
        try {
            if (self.exists()) {
                pid = self.getCanonicalFile().getName();
            }
        } catch (IOException ignored) {
        }

        if (pid == null) {
            pid = getRuntimeMXBean().getName().split("@", 0)[0];
        }

        if (pid == null) {
            int rpid = new Random().nextInt(1 << 16);
            System.err.println(Jvm.class.getName() + ": Unable to determine PID, picked a random number=" + rpid);
            return rpid;
        } else {
            return Integer.parseInt(pid);
        }
    }

    /**
     * Cast a CheckedException as an unchecked one.
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
     * Append the StackTraceElements to the StringBuilder trimming some internal methods.
     *
     * @param sb   to append to
     * @param stes stack trace elements
     */
    public static void trimStackTrace(@NotNull StringBuilder sb, @NotNull StackTraceElement... stes) {
        int first = trimFirst(stes);
        int last = trimLast(first, stes);
        for (int i = first; i <= last; i++)
            sb.append("\n\tat ").append(stes[i]);
    }

    static int trimFirst(@NotNull StackTraceElement[] stes) {
        if (stes.length > 2 && stes[1].getMethodName().endsWith("afepoint"))
            return 2;
        int first = 0;
        for (; first < stes.length; first++)
            if (!isInternal(stes[first].getClassName()))
                break;
        return Math.max(0, first - 2);
    }

    public static int trimLast(int first, @NotNull StackTraceElement[] stes) {
        int last = stes.length - 1;
        for (; first < last; last--)
            if (!isInternal(stes[last].getClassName()))
                break;
        if (last < stes.length - 1) last++;
        return last;
    }

    static boolean isInternal(@NotNull String className) {
        return className.startsWith("jdk.") || className.startsWith("sun.") || className.startsWith("java.");
    }

    /**
     * @return is the JVM in debug mode.
     */
    @SuppressWarnings("SameReturnValue")
    public static boolean isDebug() {
        return IS_DEBUG;
    }

    /**
     * @return is the JVM in flight recorder mode.
     */
    @SuppressWarnings("SameReturnValue")
    public static boolean isFlightRecorder() {
        return IS_FLIGHT_RECORDER;
    }

    /**
     * Silently pause for milli seconds.
     *
     * @param millis to sleep for.
     */
    public static void pause(long millis) {
        if (millis <= 0) {
            Thread.yield();
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Pause in a busy loop for a very short time.
     */
    public static void nanoPause() {
        if (onSpinWaitMH == null) {
            safepoint();
        } else {
            try {
                onSpinWaitMH.invokeExact();
            } catch (Throwable throwable) {
                throw new AssertionError(throwable);
            }
        }
    }

    /**
     * This method is designed to be used when the time to be
     * waited is very small, typically under a millisecond.
     *
     * @param micros Time in micros
     */
    public static void busyWaitMicros(long micros) {
        busyWaitUntil(System.nanoTime() + (micros * 1_000));
    }

    /**
     * This method is designed to be used when the time to be
     * waited is very small, typically under a millisecond.
     *
     * @param waitUntil nanosecond precision counter value to await.
     */
    public static void busyWaitUntil(long waitUntil) {
        while (waitUntil > System.nanoTime()) {
            Jvm.nanoPause();
        }
    }

    /**
     * Get the Field for a class by name.
     *
     * @param clazz to get the field for
     * @param name  of the field
     * @return the Field.
     */
    public static Field getField(@NotNull Class clazz, @NotNull String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            setAccessible(field);
            return field;

        } catch (NoSuchFieldException e) {
            Class superclass = clazz.getSuperclass();
            if (superclass != null)
                try {
                    return getField(superclass, name);
                } catch (Exception ignored) {
                }
            throw new AssertionError(e);
        }
    }

    public static Method getMethod(@NotNull Class clazz, @NotNull String name, Class... args) {
        return getMethod0(clazz, name, args, true);
    }

    private static Method getMethod0(@NotNull Class clazz, @NotNull String name, Class[] args, boolean first) {
        try {
            Method method = clazz.getDeclaredMethod(name, args);
            if (!Modifier.isPublic(method.getModifiers()) ||
                    !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
                setAccessible(method);
            return method;

        } catch (NoSuchMethodException e) {
            Class superclass = clazz.getSuperclass();
            if (superclass != null)
                try {
                    Method m = getMethod0(superclass, name, args, false);
                    if (m != null)
                        return m;
                } catch (Exception ignored) {
                }
            if (first)
                throw new AssertionError(e);
            return null;
        }
    }

    public static void setAccessible(AccessibleObject h) {
        if (IS_JAVA_9_PLUS)
            try {
                boolean newFlag = (boolean) setAccessible0_Method.invokeExact(h, true);
                assert newFlag;
            } catch (Throwable throwable) {
                Jvm.rethrow(throwable);
            }
        else
            h.setAccessible(true);
    }

    public static <V> V getValue(@NotNull Object obj, @NotNull String name) {
        Class<?> aClass = obj.getClass();
        for (String n : name.split("/")) {
            Field f = getField(aClass, n);
            try {
                obj = f.get(obj);
                if (obj == null)
                    return null;
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
            aClass = obj.getClass();
        }
        return (V) obj;
    }

    /**
     * Log the stack trace of the thread holding a lock.
     *
     * @param lock to log
     * @return the lock.toString plus a stack trace.
     */
    public static String lockWithStack(@NotNull ReentrantLock lock) {
        @Nullable Thread t = getValue(lock, "sync/exclusiveOwnerThread");
        if (t == null) {
            return lock.toString();
        }
        @NotNull StringBuilder ret = new StringBuilder();
        ret.append(lock).append(" running at");
        trimStackTrace(ret, t.getStackTrace());
        return ret.toString();
    }

    /**
     * @return The size of memory used by direct ByteBuffers i.e. ByteBuffer.allocateDirect()
     */
    public static long usedDirectMemory() {
        return reservedMemory.get();
    }

    /**
     * @return The size of memory used by UnsafeMemory.allocate()
     */
    public static long usedNativeMemory() {
        return UnsafeMemory.INSTANCE.nativeMemoryUsed();
    }

    public static long maxDirectMemory() {
        return MAX_DIRECT_MEMORY;
    }

    public static boolean is64bit() {
        return IS_64BIT;
    }

    public static void resetExceptionHandlers() {
        FATAL.defaultHandler(Slf4jExceptionHandler.FATAL).resetThreadLocalHandler();
        WARN.defaultHandler(Slf4jExceptionHandler.WARN).resetThreadLocalHandler();
        DEBUG.defaultHandler(Slf4jExceptionHandler.DEBUG).resetThreadLocalHandler();
        PERF.defaultHandler(Slf4jExceptionHandler.DEBUG).resetThreadLocalHandler();
    }

    public static void disableDebugHandler() {
        DEBUG.defaultHandler(null).resetThreadLocalHandler();
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
    public static Map<ExceptionKey, Integer> recordExceptions(boolean debug, boolean exceptionsOnly,
                                                              boolean logToSlf4j) {
        @NotNull Map<ExceptionKey, Integer> map = Collections.synchronizedMap(new LinkedHashMap<>());
        FATAL.defaultHandler(recordingExceptionHandler(LogLevel.FATAL, map, exceptionsOnly, logToSlf4j));
        WARN.defaultHandler(recordingExceptionHandler(LogLevel.WARN, map, exceptionsOnly, logToSlf4j));
        DEBUG.defaultHandler(debug ? recordingExceptionHandler(LogLevel.DEBUG, map, exceptionsOnly, logToSlf4j) : logToSlf4j ? Slf4jExceptionHandler.DEBUG : NullExceptionHandler.NOTHING);
        return map;
    }

    private static ExceptionHandler recordingExceptionHandler(LogLevel logLevel, Map<ExceptionKey, Integer> map,
                                                              boolean exceptionsOnly, boolean logToSlf4j) {
        ExceptionHandler eh = new RecordingExceptionHandler(logLevel, map, exceptionsOnly);
        if (logToSlf4j)
            eh = new ChainedExceptionHandler(eh, Slf4jExceptionHandler.valueOf(logLevel));
        return eh;
    }

    public static boolean hasException(@NotNull Map<ExceptionKey, Integer> exceptions) {

        Iterator<ExceptionKey> iterator = exceptions.keySet().iterator();
        while (iterator.hasNext()) {
            ExceptionKey k = iterator.next();
            if ((k.throwable != null && !(k.throwable instanceof StackTrace)) && k.level != LogLevel.DEBUG)
                return true;
        }

        return false;
    }

    @Deprecated
    public static void setExceptionsHandlers(@Nullable ExceptionHandler fatal,
                                             @Nullable ExceptionHandler warn,
                                             @Nullable ExceptionHandler debug) {
        setExceptionHandlers(fatal, warn, debug);
    }

    public static void setExceptionHandlers(@Nullable ExceptionHandler fatal,
                                            @Nullable ExceptionHandler warn,
                                            @Nullable ExceptionHandler debug) {

        FATAL.defaultHandler(fatal);
        WARN.defaultHandler(warn);
        DEBUG.defaultHandler(debug);

    }

    public static void setExceptionHandlers(@Nullable ExceptionHandler fatal,
                                            @Nullable ExceptionHandler warn,
                                            @Nullable ExceptionHandler debug,
                                            @Nullable ExceptionHandler perf) {
        setExceptionHandlers(fatal, warn, debug);
        PERF.defaultHandler(perf);
    }

    public static void setThreadLocalExceptionHandlers(@Nullable ExceptionHandler fatal,
                                                       @Nullable ExceptionHandler warn,
                                                       @Nullable ExceptionHandler debug) {

        FATAL.threadLocalHandler(fatal);
        WARN.threadLocalHandler(warn);
        DEBUG.threadLocalHandler(debug);
    }

    public static void setThreadLocalExceptionHandlers(@Nullable ExceptionHandler fatal,
                                                       @Nullable ExceptionHandler warn,
                                                       @Nullable ExceptionHandler debug,
                                                       @Nullable ExceptionHandler perf) {

        setThreadLocalExceptionHandlers(fatal, warn, debug);
        PERF.threadLocalHandler(debug);
    }

    @NotNull
    public static ExceptionHandler fatal() {
        return FATAL;
    }

    @NotNull
    public static ExceptionHandler warn() {
        return WARN;
    }

    @NotNull
    public static ExceptionHandler perf() {
        return PERF;
    }

    @NotNull
    public static ExceptionHandler debug() {
        return DEBUG;
    }

    public static void dumpException(@NotNull Map<ExceptionKey, Integer> exceptions) {
        System.out.println("exceptions: " + exceptions.size());
        for (@NotNull Map.Entry<ExceptionKey, Integer> entry : exceptions.entrySet()) {
            ExceptionKey key = entry.getKey();
            System.err.println(key.level + " " + key.clazz.getSimpleName() + " " + key.message);
            if (key.throwable != null)
                key.throwable.printStackTrace();
            Integer value = entry.getValue();
            if (value > 1)
                System.err.println("Repeated " + value + " times");
        }
        resetExceptionHandlers();
    }

    public static boolean isDebugEnabled(Class aClass) {
        return DEBUG.isEnabled(aClass) || isDebug();
    }

    private static long maxDirectMemory0() {
        try {
            Class<?> clz;

            if (IS_JAVA_9_PLUS) {
                clz = Class.forName("jdk.internal.misc.VM");
            } else {
                clz = Class.forName("sun.misc.VM");
            }

            final Field f = clz.getDeclaredField("directMemory");
            long offset = UNSAFE.staticFieldOffset(f);
            Object base = UNSAFE.staticFieldBase(f);

            return UNSAFE.getLong(base, offset);
        } catch (Exception e) {
            // ignore
        }
        System.err.println(Jvm.class.getName() + ": Unable to determine max direct memory");
        return 0L;
    }

    private static int getMajorVersion0() {
        try {
            final Method method = Runtime.class.getDeclaredMethod("version");
            if (method != null) {
                final Object version = method.invoke(Runtime.getRuntime());
                final Class<?> clz = Class.forName("java.lang.Runtime$Version");
                return (Integer) clz.getDeclaredMethod("major").invoke(version);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            // ignore and fall back to pre-jdk9
        }
        return Integer.parseInt(Runtime.class.getPackage().getSpecificationVersion().split("\\.")[1]);
    }

    /**
     * Helper method for setting the default signals. Every signal handler you register with this method will be called.
     *
     * @param signalHandler to call on a signal
     */
    public static void signalHandler(SignalHandler signalHandler) {
        if (signalHandlerGlobal.handlers.isEmpty()) {
            if (!OS.isWindows()) // not available on windows.
                addSignalHandler("HUP", signalHandlerGlobal);
            addSignalHandler("INT", signalHandlerGlobal);
            addSignalHandler("TERM", signalHandlerGlobal);
        }
        SignalHandler signalHandler2 = signal -> {
            Jvm.warn().on(signalHandler.getClass(), "Signal " + signal.getName() + " triggered");
            signalHandler.handle(signal);
        };
        signalHandlerGlobal.handlers.add(signalHandler2);
    }

    private static void addSignalHandler(String sig, SignalHandler signalHandler) {
        try {
            Signal.handle(new Signal(sig), signalHandler);

        } catch (IllegalArgumentException e) {
            // When -Xrs is specified the user is responsible for
            // ensuring that shutdown hooks are run by calling
            // System.exit()
            Jvm.warn().on(signalHandler.getClass(), "Unable add a signal handler", e);
        }
    }

    public static void safepoint() {
        // no safepoint
        // System.identityHashCode("");
//        byte.class.getModifiers();

//        "".intern(); 50 ns
        if (!IS_JAVA_9_PLUS) {
            Compiler.enable(); // 5 ns on Java 8
        } else {
            Thread.holdsLock(""); // 100 ns on Java 11
        }
    }

    public static void optionalSafepoint() {
        if (SAFEPOINT_ENABLED)
            if (IS_JAVA_9_PLUS)
                Thread.holdsLock("");
            else
                Compiler.enable();
    }

    public static boolean areOptionalSafepointsEnabled() {
        return SAFEPOINT_ENABLED;
    }

    public static boolean stackTraceEndsWith(String endsWith, int n) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = n + 2; i < stackTrace.length; i++)
            if (stackTrace[i].getClassName().endsWith(endsWith))
                return true;
        return false;
    }

    public static boolean isArm() {
        return IS_ARM;
    }

    public static ClassMetrics classMetrics(Class c) throws IllegalArgumentException {
        return CLASS_METRICS_MAP.computeIfAbsent(c, Jvm::getClassMetrics);
    }

    private static ClassMetrics getClassMetrics(Class c) {
        Class superclass = c.getSuperclass();
        int start = Integer.MAX_VALUE, end = 0;
        for (Field f : c.getDeclaredFields()) {
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0)
                continue;
            if (!f.getType().isPrimitive())
                continue;
            int start0 = Math.toIntExact(UnsafeMemory.UNSAFE.objectFieldOffset(f));
            int size = PRIMITIVE_SIZE.get(f.getType());
            start = Math.min(start0, start);
            end = Math.max(start0 + size, end);
        }
        if (superclass != null && superclass != Object.class) {
            ClassMetrics cm0 = getClassMetrics(superclass);
            start = Math.min(cm0.offset(), start);
            end = Math.max(cm0.offset() + cm0.length(), end);
            validateClassMetrics(superclass, start, end);
        }

        validateClassMetrics(c, start, end);

        return new ClassMetrics(start, end - start);
    }

    private static void validateClassMetrics(Class c, int start, int end) {
        for (Field f : c.getDeclaredFields()) {
            if ((f.getModifiers() & Modifier.STATIC) != 0)
                continue;
            if (f.getType().isPrimitive())
                continue;
            int start0 = Math.toIntExact(UnsafeMemory.UNSAFE.objectFieldOffset(f));
            if (start <= start0 && start0 < end) {
                throw new IllegalArgumentException(c + " is not suitable for raw copies due to " + f);
            }
        }
    }

    public static String userHome() {
        return System.getProperty("user.home", ".");
    }

    public static boolean dontChain(Class tClass) {
        return tClass.getAnnotation(DontChain.class) != null || tClass.getName().startsWith("java");
    }

    public static boolean isResourceTracing() {
        return RESOURCE_TRACING;
    }

    /**
     * A more permissive boolean System property flag.
     * <code>-Dflag</code><code>-Dflag=true</code><code>-Dflag=yes</code>
     * are all accepted
     *
     * @param property name to lookup
     * @return if true or set
     */
    public static boolean getBoolean(String property) {
        return getBoolean(property, false);
    }

    /**
     * A more permissive boolean System property flag.
     * <code>-Dflag</code><code>-Dflag=true</code><code>-Dflag=yes</code>
     * are all accepted
     *
     * @param property     name to lookup
     * @param defaultValue value to be used if unknown
     * @return if true or set
     */
    public static boolean getBoolean(String property, boolean defaultValue) {
        String value = System.getProperty(property);
        if (value == null)
            return defaultValue;
        if (value.isEmpty())
            return true;
        return ObjectUtils.isTrue(value);
    }

    private static class ChainedSignalHandler implements SignalHandler {
        final List<SignalHandler> handlers = new CopyOnWriteArrayList<>();

        @Override
        public void handle(Signal signal) {
            for (SignalHandler handler : handlers) {
                try {
                    if (handler != null)
                        handler.handle(signal);
                } catch (Throwable t) {
                    Jvm.warn().on(this.getClass(), "Problem handling signal", t);
                }
            }
        }
    }
}
