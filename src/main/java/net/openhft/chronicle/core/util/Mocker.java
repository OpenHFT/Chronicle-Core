/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.openhft.chronicle.core.Jvm.uncheckedCast;

/**
 * The Mocker class provides utility methods for creating mocked instances of interfaces.
 */
public final class Mocker {

    private static final Class<?>[] NO_CLASSES = {};

    // Suppresses default constructor, ensuring non-instantiability.
    private Mocker() {
    }

    /**
     * Creates a mocked instance of the specified interface that logs method invocations to the provided PrintStream.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param description   the description of the mocking behavior
     * @param out           the PrintStream to log the method invocations
     * @return the mocked instance of the class
     */
    @NotNull
    public static <T> T logging(@NotNull Class<T> interfaceType, String description, @NotNull PrintStream out) {
        return intercepting(interfaceType, description, out::println);
    }

    /**
     * Creates a mocked instance of the specified interface that logs method invocations to the provided PrintWriter.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param description   the description of the mocking behavior
     * @param out           the PrintWriter to log the method invocations
     * @return the mocked instance of the class
     */
    @NotNull
    public static <T> T logging(@NotNull Class<T> interfaceType, String description, @NotNull PrintWriter out) {
        return intercepting(interfaceType, description, out::println);
    }

    /**
     * Creates a mocked instance of the specified interface that logs method invocations to the provided StringWriter.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param description   the description of the mocking behavior
     * @param out           the StringWriter to log the method invocations
     * @return the mocked instance of the class
     */
    @NotNull
    public static <T> T logging(@NotNull Class<T> interfaceType, String description, @NotNull StringWriter out) {
        return logging(interfaceType, description, new PrintWriter(out));
    }

    /**
     * Creates a mocked instance of the specified interface that enqueues method invocations to the provided BlockingQueue.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param description   the description of the mocking behavior
     * @param queue         the BlockingQueue to enqueue the method invocations
     * @return the mocked instance of the class
     */
    @NotNull
    public static <T> T queuing(@NotNull Class<T> interfaceType, String description, @NotNull BlockingQueue<String> queue) {
        return intercepting(interfaceType, description, queue::add);
    }

    /**
     * Creates a mocked instance of the specified interface that intercepts method invocations using the provided consumer.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param description   the description of the mocking behavior
     * @param consumer      the consumer to intercept and handle the method invocations
     * @return the mocked instance of the class
     */
    @NotNull
    public static <T> T intercepting(@NotNull Class<T> interfaceType, String description, @NotNull Consumer<String> consumer) {
        return intercepting(interfaceType, description, consumer, null);
    }

    /**
     * Creates a mocked instance of the specified interface that intercepts method invocations
     * using the provided consumer,
     * and optionally delegates the intercepted invocations to the provided object.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param description   the description of the mocking behavior
     * @param consumer      the consumer to intercept and handle the method invocations
     * @param t             the object to delegate the intercepted invocations, or null if no delegation is needed
     * @return the mocked instance of the class
     */
    @NotNull
    public static <T> T intercepting(@NotNull Class<T> interfaceType, @NotNull final String description, @NotNull Consumer<String> consumer, T t) {
        return intercepting(interfaceType,
                (name, args) -> consumer.accept(description + name + (args == null ? "()" : Arrays.toString(args))),
                t);
    }

    /**
     * Creates a mocked instance of the specified interface that intercepts method invocations using the provided
     * bi-consumer, and optionally delegates the intercepted invocations to the provided object.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param consumer      the bi-consumer to intercept and handle the method invocations
     * @param t             the object to delegate the intercepted invocations, or null if no delegation is needed
     * @return the mocked instance of the class
     */
    @NotNull
    public static <T> T intercepting(@NotNull Class<T> interfaceType, @NotNull BiConsumer<String, Object[]> consumer, T t) {
        final Set<Class<?>> classes = new LinkedHashSet<>();
        addInterface(classes, interfaceType);
        return uncheckedCast(
                newProxyInstance(interfaceType.getClassLoader(), classes.toArray(NO_CLASSES), new AbstractInvocationHandler(interfaceType) {
                    @Override
                    protected Object doInvoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
                        consumer.accept(method.getName(), args);
                        if (t != null)
                            return method.invoke(t, args);
                        return null;
                    }
                }));
    }

    private static <T> void addInterface(Set<Class<?>> interfaceType, Class<T> tClass) {
        if (Jvm.dontChain(tClass))
            return;
        if (interfaceType.contains(tClass))
            return;
        interfaceType.add(tClass);
        for (Method method : tClass.getMethods()) {
            final Type returnType0 = GenericReflection.getReturnType(method, tClass);
            if (returnType0 instanceof Class) {
                Class<?> returnType = (Class<?>) returnType0;
                if (returnType.isInterface())
                    addInterface(interfaceType, returnType);
            }
        }
    }

    /**
     * Creates a mocked instance of the specified interface that ignores all method invocations.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param additional    additional classes to add to the mocked instance
     * @return the mocked instance of the class
     */
    @NotNull
    public static <T> T ignored(@NotNull Class<T> interfaceType, Class<?>... additional) {
        final Set<Class<?>> classes = new LinkedHashSet<>();
        addInterface(classes, interfaceType);
        classes.add(interfaceType);
        for (Class<?> aClass : additional)
            addInterface(classes, aClass);
        classes.add(IgnoresEverything.class);
        ClassLoader tClassLoader = interfaceType.getClassLoader();
        return uncheckedCast(
                newProxyInstance(tClassLoader != null ? tClassLoader : Mocker.class.getClassLoader(),
                        classes.toArray(NO_CLASSES), new AbstractInvocationHandler(interfaceType) {
                            @Override
                            protected Object doInvoke(Object proxy, Method method, Object[] args) {
                                return null;
                            }
                        }));
    }

    /**
     * Creates a new proxy instance using the specified class loader, interfaces, and invocation handler.
     *
     * @param classLoader the class loader to define the proxy class
     * @param classes     the interfaces implemented by the proxy class
     * @param handler     the invocation handler to handle method invocations on the proxy instance
     * @return the new proxy instance
     */
    private static Object newProxyInstance(ClassLoader classLoader, Class<?>[] classes, AbstractInvocationHandler handler) {
        try {
            // for exec-maven
            return Proxy.newProxyInstance(classLoader, classes, handler);
        } catch (IllegalArgumentException e) {
            // For Java 11+
            return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), classes, handler);
        }
    }
}
