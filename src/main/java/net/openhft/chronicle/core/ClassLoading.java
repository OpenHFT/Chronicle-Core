package net.openhft.chronicle.core;

/**
 * Created by peter.lawrey on 03/03/2015.
 */
public enum ClassLoading {
    ;

    public static Class defineClass(String className, byte[] bytes) {
        return defineClass(Thread.currentThread().getContextClassLoader(), className, bytes);
    }

    public static Class defineClass(ClassLoader classLoader, String className, byte[] bytes) {
        return UnsafeMemory.UNSAFE.defineClass(className, bytes, 0, bytes.length, classLoader, null);
    }
}
