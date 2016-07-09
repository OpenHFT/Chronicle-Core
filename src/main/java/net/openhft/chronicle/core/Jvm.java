/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.onoes.*;
import sun.misc.VM;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;

/**
 * Utility class to access information in the JVM.
 */
public enum Jvm {
    ;

    private static final boolean IS_DEBUG = getRuntimeMXBean().getInputArguments().toString().contains("jdwp") || Boolean.getBoolean("debug");
    // e.g-verbose:gc  -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=dumponexit=true,filename=myrecording.jfr,settings=profile -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints

    private static final boolean IS_FLIGHT_RECORDER = (" " + getRuntimeMXBean().getInputArguments()).contains(" -XX:+FlightRecorder") || Boolean.getBoolean("jfr");
    private static final Class bitsClass;
    private static final Field reservedMemory;
    private static final AtomicLong reservedMemoryAtomicLong;
    private static final DirectMemoryInspector DIRECT_MEMORY_INSPECTOR;
    private static final boolean IS_64BIT = is64bit0();
    private static ExceptionHandler FATAL = Slf4jExceptionHandler.FATAL;
    private static ExceptionHandler WARN = Slf4jExceptionHandler.WARN;
    private static ExceptionHandler DEBUG = Slf4jExceptionHandler.DEBUG;

    static {
        try {
            bitsClass = Class.forName("java.nio.Bits");
            reservedMemory = bitsClass.getDeclaredField("reservedMemory");
            reservedMemory.setAccessible(true);
            if (reservedMemory.getType() == AtomicLong.class) {
                reservedMemoryAtomicLong = (AtomicLong) reservedMemory.get(null);
                DIRECT_MEMORY_INSPECTOR = DirectMemoryInspector.AtomicLong;
            } else {
                reservedMemoryAtomicLong = null;
                DIRECT_MEMORY_INSPECTOR = DirectMemoryInspector.Reflect;
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
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

    /**
     * Cast a CheckedException as an unchecked one.
     *
     * @param throwable to cast
     * @param <T>       the type of the Throwable
     * @return this method will never return a Throwable instance, it will just throw it.
     * @throws T the throwable as an unchecked throwable
     */
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
    public static void trimStackTrace(StringBuilder sb, StackTraceElement... stes) {
        int first = trimFirst(stes);
        int last = trimLast(first, stes);
        for (int i = first; i <= last; i++)
            sb.append("\n\tat ").append(stes[i]);
    }

    static int trimFirst(StackTraceElement[] stes) {
        int first = 0;
        for (; first < stes.length; first++)
            if (!isInternal(stes[first].getClassName()))
                break;
        return Math.max(0, first - 2);
    }

    public static int trimLast(int first, StackTraceElement[] stes) {
        int last = stes.length - 1;
        for (; first < last; last--)
            if (!isInternal(stes[last].getClassName()))
                break;
        if (last < stes.length - 1) last++;
        return last;
    }

    static boolean isInternal(String className) {
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
        long timeNanos = millis * 1000000;
        if (timeNanos > 10e6) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            LockSupport.parkNanos(timeNanos);
        }
    }

    /**
     * This method is designed tobe used when the time to be
     * waited is very small, typically under a millisecond.
     *
     * @param micros Time in micros
     */
    public static void busyWaitMicros(long micros) {
        long waitUntil = System.nanoTime() + (micros * 1_000);
        while (waitUntil > System.nanoTime()) {
        }
    }

    /**
     * Get the Field for a class by name.
     *
     * @param clazz to get the field for
     * @param name  of the field
     * @return the Field.
     */
    public static Field getField(Class clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
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

    public static <V> V getValue(Object obj, String name) {
        for (String n : name.split("/")) {
            Field f = getField(obj.getClass(), n);
            try {
                obj = f.get(obj);
                if (obj == null)
                    return null;
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
        return (V) obj;
    }

    /**
     * Log the stack trace of the thread holding a lock.
     *
     * @param lock to log
     * @return the lock.toString plus a stack trace.
     */
    public static String lockWithStack(ReentrantLock lock) {
        Thread t = getValue(lock, "sync/exclusiveOwnerThread");
        if (t == null) {
            return lock.toString();
        }
        StringBuilder ret = new StringBuilder();
        ret.append(lock).append(" running at");
        trimStackTrace(ret, t.getStackTrace());
        return ret.toString();
    }

    /**
     * @return The size of memory used by direct ByteBuffers i.e. ByteBuffer.allocateDirect()
     */
    public static long usedDirectMemory() {
        return DIRECT_MEMORY_INSPECTOR.usedDirectMemory();
    }

    /**
     * @return The size of memory used by UnsafeMemory.allocate()
     */
    public static long usedNativeMemory() {
        return UnsafeMemory.INSTANCE.nativeMemoryUsed();
    }

    public static long maxDirectMemory() {
        return VM.maxDirectMemory();
    }

    public static boolean is64bit() {
        return IS_64BIT;
    }

    public static void resetExceptionHandlers() {
        FATAL = Slf4jExceptionHandler.FATAL;
        WARN = Slf4jExceptionHandler.WARN;
        DEBUG = Slf4jExceptionHandler.DEBUG;
    }

    public static Map<ExceptionKey, Integer> recordExceptions() {
        return recordExceptions(false);
    }

    public static Map<ExceptionKey, Integer> recordExceptions(boolean debug) {
        Map<ExceptionKey, Integer> map = new LinkedHashMap<>();
        FATAL = new RecordingExceptionHandler(LogLevel.FATAL, map);
        WARN = new RecordingExceptionHandler(LogLevel.WARN, map);
        DEBUG = debug ? new RecordingExceptionHandler(LogLevel.DEBUG, map) : NullExceptionHandler.NOTHING;
        return map;
    }

    public static void setExceptionsHandlers(ExceptionHandler fatal, ExceptionHandler warn, ExceptionHandler debug) {
        FATAL = fatal;
        WARN = warn;
        DEBUG = debug;
    }

    public static void disableDebugHandler() {
        DEBUG = NullExceptionHandler.NOTHING;
    }

    public static ExceptionHandler fatal() {
        return FATAL;
    }

    public static ExceptionHandler warn() {
        return WARN;
    }

    public static ExceptionHandler debug() {
        return DEBUG;
    }

    public static void dumpException(Map<ExceptionKey, Integer> exceptions) {
        for (Map.Entry<ExceptionKey, Integer> entry : exceptions.entrySet()) {
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
        return DEBUG.isEnabled(aClass);
    }

    enum DirectMemoryInspector {
        Reflect {
            @Override
            public long usedDirectMemory() {
                try {
                    synchronized (bitsClass) {
                        return reservedMemory.getLong(null);
                    }
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
        },
        AtomicLong {
            @Override
            public long usedDirectMemory() {
                return reservedMemoryAtomicLong.get();
            }
        };

        public abstract long usedDirectMemory();
    }
}
