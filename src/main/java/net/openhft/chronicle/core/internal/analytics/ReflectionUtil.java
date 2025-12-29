/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.stream.Stream;

import static net.openhft.chronicle.core.Jvm.uncheckedCast;
import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * Reflection utilities for integrating with the optional {@code chronicle-analytics} module and avoiding linkage failures.
 *
 * <p>Locates analytics types by name, returns builder instances, and exposes helpers to look up
 * and invoke methods while wrapping reflective failures in Chronicle's unchecked exception
 * handling.
 */
public final class ReflectionUtil {

    private static final String ANALYTICS_NAME = "net.openhft.chronicle.analytics.Analytics";

    // Suppresses default constructor, ensuring non-instantiability.
    private ReflectionUtil() {
    }

    /**
     * Detect whether the optional {@code chronicle-analytics} module is on the classpath.
     *
     * @return true if analytics classes can be loaded
     */
    public static boolean analyticsPresent() {
        try {
            Class.forName(ANALYTICS_NAME);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * Construct an analytics builder via reflection using the provided credentials.
     */
    @NotNull
    public static Object analyticsBuilder(@NotNull final String measurementId, @NotNull final String apiSecret) {
        requireNonNull(measurementId);
        requireNonNull(apiSecret);
        try {
            final Method method = methodOrThrow(ANALYTICS_NAME, "builder", String.class, String.class);
            return method.invoke(null, measurementId, apiSecret);
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw Jvm.rethrow(e);
        }
    }

    /**
     * Look up a method by name or throw a runtime exception if it cannot be resolved.
     */
    @NotNull
    public static Method methodOrThrow(@NotNull final String className,
                                       @NotNull final String methodName,
                                       final Class<?>... parameterTypes) {
        requireNonNull(className);
        requireNonNull(methodName);
        try {
            final Class<?> analyticsClass = Class.forName(className);
            return analyticsClass.getMethod(methodName, parameterTypes);
        } catch (ReflectiveOperationException e) {
            throw Jvm.rethrow(e);
        }
    }

    /**
     * Invoke a method or rethrow reflection errors using {@link Jvm#rethrow(Throwable)}.
     */
    public static Object invokeOrThrow(@NotNull final Method method,
                                       @NotNull final Object target,
                                       Object... params) {
        requireNonNull(method);
        requireNonNull(target);

        try {
            return method.invoke(target, params);
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw Jvm.rethrow(e);
        }
    }

    /**
     * Create a proxy implementing {@code interf} that forwards calls to {@code delegate}.
     */
    @NotNull
    @Deprecated(/* to be removed in 2027, only used in tests */)
    public static <T> T reflectiveProxy(@NotNull final Class<T> interf, @NotNull final Object delegate) throws IllegalArgumentException {
        requireNonNull(interf);
        requireNonNull(delegate);
        Class<?>[] interfaces = {interf};
        return uncheckedCast(
                Proxy.newProxyInstance(
                        delegate.getClass().getClassLoader(),
                        interfaces,
                        new ReflectiveInvocationHandler(delegate, false)));
    }

    /**
     * Create a forwarding proxy with the option to return the proxy itself for fluent APIs.
     */
    @NotNull
    @Deprecated(/* to be removed in 2027, only used in tests */)
    public static <T> T reflectiveProxy(@NotNull final Class<T> interf,
                                        @NotNull final Object delegate,
                                        final boolean returnProxy) throws IllegalArgumentException {
        requireNonNull(interf);
        requireNonNull(delegate);
        Class<?>[] interfaces = {interf};
        return uncheckedCast(
                Proxy.newProxyInstance(
                        delegate.getClass().getClassLoader(),
                        interfaces,
                        new ReflectiveInvocationHandler(delegate, returnProxy)));
    }

    /**
     * Invocation handler that forwards calls to a concrete delegate, optionally returning the proxy
     * for fluent usage when {@link #returnProxy} is true.
     */
    private static final class ReflectiveInvocationHandler implements InvocationHandler {

        private final Object delegate;
        private final boolean returnProxy;

        /**
         * @param delegate    target object that receives method calls
         * @param returnProxy when true, non-build methods return the proxy itself to enable fluent APIs
         */
        public ReflectiveInvocationHandler(@NotNull final Object delegate, final boolean returnProxy) {
            this.delegate = requireNonNull(delegate);
            this.returnProxy = requireNonNull(returnProxy);
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

            final Class<?>[] parameterTypes;

            if (args == null)
                parameterTypes = null;
            else
                parameterTypes = Stream.of(args)
                        .map(Object::getClass)
                        .toArray(Class[]::new);

            final Method delegateMethod = delegate.getClass().getMethod(method.getName(), parameterTypes);
            final Object result = delegateMethod.invoke(delegate, args);
            if (returnProxy && !"build".equals(method.getName())) {
                return proxy;
            } else {
                return result;
            }
        }
    }
}
