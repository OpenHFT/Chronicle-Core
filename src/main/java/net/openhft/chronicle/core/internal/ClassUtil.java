/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;

public final class ClassUtil {
    static class SetAccessibleHolder {
        static final MethodHandle setAccessible0_Method = getSetAccessible0Method();

        private static MethodHandle getSetAccessible0Method() {
            if (!Bootstrap.isJava9Plus()) {
                return null;
            }
            final MethodType signature = MethodType.methodType(boolean.class, boolean.class);
            try {
                // Access privateLookupIn() reflectively to support compilation with JDK 8
                // CSReflectiveMethodLookup keep this privateLookupIn lookup here because ClassUtil probes once for the JDK 9+ setAccessible0 path and reuses the result for all later access checks.
                Method privateLookupIn = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
                MethodHandles.Lookup lookup = (MethodHandles.Lookup) privateLookupIn.invoke(null, AccessibleObject.class, MethodHandles.lookup());
                return lookup.findVirtual(AccessibleObject.class, "setAccessible0", signature);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                     IllegalArgumentException e) {
                Logger logger = LoggerFactory.getLogger(ClassUtil.class);
                logger.error("Chronicle products may require command line arguments to be provided for Java 11 and above. See https://chronicle.software/chronicle-support-java-17");
                // Fallback to null here when the JDK 9+ setAccessible0 path is unavailable and let callers use the older accessibility path instead.
                return null;
            }
        }
    }
    private ClassUtil() {
    }

    @Nullable
    public static Field getField0(@NotNull final Class<?> clazz,
                                  @NotNull final String name,
                                  final boolean error,
                                  final boolean setAccessible) {
        try {
            final Field field = clazz.getDeclaredField(name);
            if (setAccessible)
                // CSSetAccessibleEscalation keep setAccessible(field) here because this helper intentionally exposes declared-field lookup that can bypass normal Java access checks.
                setAccessible(field);
            return field;

            // CSWarnAndContinue catch (IllegalAccessError e) because this reflective lookup reports the access failure and then falls back to treating the field as unavailable.
        } catch (IllegalAccessError e) {
            if (error)
                Jvm.warn().on(clazz, "Unable to access " + name + " " + e.getMessage());
            return null;
        } catch (NoSuchFieldException e) {
            final Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                final Field field = getField0(superclass, name, false, setAccessible);
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
     * @throws SecurityException - if the request is denied.
     * @see SecurityManager#checkPermission
     * @see RuntimePermission
     */
    @SuppressWarnings({"java:S3011", "CSSetAccessibleEscalation"})
    // Justification: centralised, audited accessibility control for Chronicle internals.
    public static void setAccessible(@NotNull final AccessibleObject accessibleObject) {
        if (Bootstrap.isJava9Plus())
            try {
                if (SetAccessibleHolder.setAccessible0_Method == null)
                    return;
                boolean newFlag = (boolean) SetAccessibleHolder.setAccessible0_Method.invokeExact(accessibleObject, true);
                assert newFlag;
            } catch (Throwable throwable) {
                throw new AssertionError(throwable);
            }
        else
            accessibleObject.setAccessible(true);
    }

    @Nullable
    @SuppressWarnings({"CSReflectiveMethodLookup", "CSSetAccessibleEscalation:silent"})
    public static Method getMethod0(@NotNull final Class<?> clazz,
                                    @NotNull final String name,
                                    final Class<?>[] args,
                                    final boolean first) {
        try {
            final Method method = clazz.getDeclaredMethod(name, args);
            if (!Modifier.isPublic(method.getModifiers()) ||
                    !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
                setAccessible(method);
            return method;

        } catch (NoSuchMethodException e) {
            final Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                final Method m = getMethod0(superclass, name, args, false);
                if (m != null)
                    return m;
            }
            if (first)
                throw new AssertionError(e);
            return null;
        }
    }
}
