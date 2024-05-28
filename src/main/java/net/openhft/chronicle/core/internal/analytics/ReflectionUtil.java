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

package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.stream.Stream;

import static net.openhft.chronicle.core.Jvm.uncheckedCast;
import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

public final class ReflectionUtil {

    private static final String ANALYTICS_NAME = "net.openhft.chronicle.analytics.Analytics";

    // Suppresses default constructor, ensuring non-instantiability.
    private ReflectionUtil() {
    }

    public static boolean analyticsPresent() {
        try {
            Class.forName(ANALYTICS_NAME);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

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

    @NotNull
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

    @NotNull
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

    private static final class ReflectiveInvocationHandler implements InvocationHandler {

        private final Object delegate;
        private final boolean returnProxy;

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
