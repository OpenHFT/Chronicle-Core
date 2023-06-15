package net.openhft.chronicle.core.internal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;

public final class ClassUtil {
    public static final MethodHandle setAccessible0_Method = getSetAccessible0Method();

    private ClassUtil() {
    }

    private static MethodHandle getSetAccessible0Method() {
        if (!Bootstrap.isJava9Plus()) {
            return null;
        }
        final MethodType signature = MethodType.methodType(boolean.class, boolean.class);
        try {
            // Access privateLookupIn() reflectively to support compilation with JDK 8
            Method privateLookupIn = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) privateLookupIn.invoke(null, AccessibleObject.class, MethodHandles.lookup());
            return lookup.findVirtual(AccessibleObject.class, "setAccessible0", signature);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 IllegalArgumentException e) {
            Logger logger = LoggerFactory.getLogger(ClassUtil.class);
            logger.error("Chronicle products require command line arguments to be provided for Java 11 and above. See https://chronicle.software/chronicle-support-java-17");
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Field getField0(@NotNull final Class<?> clazz,
                                  @NotNull final String name,
                                  final boolean error) {
        try {
            final Field field = clazz.getDeclaredField(name);
            setAccessible(field);
            return field;

        } catch (NoSuchFieldException e) {
            final Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                final Field field = getField0(superclass, name, false);
                if (field != null)
                    return field;
            }
            if (error)
                throw new AssertionError(e);
            return null;
        }
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
        if (Bootstrap.isJava9Plus())
            try {
                boolean newFlag = (boolean) setAccessible0_Method.invokeExact(accessibleObject, true);
                assert newFlag;
            } catch (Throwable throwable) {
                throw new AssertionError(throwable);
            }
        else
            accessibleObject.setAccessible(true);
    }

    public static Method getMethod0(@NotNull final Class<?> clazz,
                                    @NotNull final String name,
                                    final Class[] args,
                                    final boolean first) {
        try {
            final Method method = clazz.getDeclaredMethod(name, args);
            if (!Modifier.isPublic(method.getModifiers()) ||
                    !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
                setAccessible(method);
            return method;

        } catch (NoSuchMethodException e) {
            final Class<?> superclass = clazz.getSuperclass();
            if (superclass != null)
                try {
                    final Method m = getMethod0(superclass, name, args, false);
                    if (m != null)
                        return m;
                } catch (Exception ignored) {
                    // Ignore
                }
            if (first)
                throw new AssertionError(e);
            return null;
        }
    }
}
