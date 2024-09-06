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

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * Utility class for handling reflection-based operations, such as method invocations
 * and proxy creation. This class simplifies reflective operations for the Analytics module
 * and related components.
 * <p>
 * It checks the presence of the analytics class, dynamically invokes methods, and creates
 * proxies using Java Reflection API. It also supports returning proxies for chaining methods.
 * </p>
 */
public final class ReflectionUtil {

    private static final String ANALYTICS_NAME = "net.openhft.chronicle.analytics.Analytics";

    // Suppresses default constructor, ensuring non-instantiability.
    private ReflectionUtil() {
    }

    /**
     * Checks whether the Analytics class is present in the classpath.
     *
     * @return true if the Analytics class is present, false otherwise.
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
     * Dynamically builds an analytics object using the provided measurement ID and API secret.
     *
     * @param measurementId The measurement ID for analytics.
     * @param apiSecret     The API secret for analytics.
     * @return The analytics builder object.
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
     * Retrieves a method from the specified class by name and parameter types.
     *
     * @param className    The fully qualified name of the class.
     * @param methodName   The name of the method to retrieve.
     * @param parameterTypes The parameter types of the method.
     * @return The Method object representing the specified method.
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
     * Invokes the specified method on the target object, passing in the provided parameters.
     *
     * @param method The method to invoke.
     * @param target The target object to invoke the method on.
     * @param params The parameters to pass to the method.
     * @return The result of the method invocation.
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
     * Creates a dynamic proxy that forwards method calls to the provided delegate object.
     *
     * @param interf   The interface class the proxy should implement.
     * @param delegate The delegate object to forward method calls to.
     * @param <T>      The type of the interface.
     * @return A proxy instance implementing the specified interface.
     * @throws IllegalArgumentException If the proxy cannot be created.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> T reflectiveProxy(@NotNull final Class<T> interf, @NotNull final Object delegate) throws IllegalArgumentException {
        requireNonNull(interf);
        requireNonNull(delegate);
        return (T) Proxy.newProxyInstance(delegate.getClass().getClassLoader(), new Class[]{interf}, new ReflectiveInvocationHandler(delegate, false));
    }

    /**
     * Creates a dynamic proxy that forwards method calls to the provided delegate object,
     * with the option to return the proxy instance itself for chaining methods.
     *
     * @param interf      The interface class the proxy should implement.
     * @param delegate    The delegate object to forward method calls to.
     * @param returnProxy If true, the proxy instance is returned for method chaining.
     * @param <T>         The type of the interface.
     * @return A proxy instance implementing the specified interface.
     * @throws IllegalArgumentException If the proxy cannot be created.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> T reflectiveProxy(@NotNull final Class<T> interf,
                                        @NotNull final Object delegate,
                                        final boolean returnProxy) throws IllegalArgumentException {
        requireNonNull(interf);
        requireNonNull(delegate);
        return (T) Proxy.newProxyInstance(delegate.getClass().getClassLoader(), new Class[]{interf}, new ReflectiveInvocationHandler(delegate, returnProxy));
    }

    /**
     * An internal invocation handler that dynamically dispatches method calls to the delegate object.
     */
    private static final class ReflectiveInvocationHandler implements InvocationHandler {

        private final Object delegate;
        private final boolean returnProxy;

        /**
         * Constructs a new ReflectiveInvocationHandler.
         *
         * @param delegate    The object that method calls are forwarded to.
         * @param returnProxy If true, the proxy itself is returned for method chaining.
         */
        public ReflectiveInvocationHandler(@NotNull final Object delegate, final boolean returnProxy) {
            this.delegate = requireNonNull(delegate);
            this.returnProxy = requireNonNull(returnProxy);
        }

        /**
         * Handles method invocation on the proxy instance by dispatching it to the delegate.
         *
         * @param proxy  The proxy instance.
         * @param method The method being called.
         * @param args   The arguments for the method call.
         * @return The result of the method call, or the proxy itself if method chaining is enabled.
         * @throws Throwable If an exception occurs during method invocation.
         */
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
