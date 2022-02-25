/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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

import net.openhft.chronicle.core.util.AbstractInvocationHandler;
import net.openhft.chronicle.core.util.GenericReflection;
import net.openhft.chronicle.core.util.IgnoresEverything;
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

public final class Mocker {

    private static final Class<?>[] NO_CLASSES = new Class[0];

    // Suppresses default constructor, ensuring non-instantiability.
    private Mocker() {
    }

    @NotNull
    public static <T> T logging(@NotNull Class<T> tClass, String description, @NotNull PrintStream out) {
        return intercepting(tClass, description, out::println);
    }

    @NotNull
    public static <T> T logging(@NotNull Class<T> tClass, String description, @NotNull PrintWriter out) {
        return intercepting(tClass, description, out::println);
    }

    @NotNull
    public static <T> T logging(@NotNull Class<T> tClass, String description, @NotNull StringWriter out) {
        return logging(tClass, description, new PrintWriter(out));
    }

    @NotNull
    public static <T> T queuing(@NotNull Class<T> tClass, String description, @NotNull BlockingQueue<String> queue) {
        return intercepting(tClass, description, queue::add);
    }

    @NotNull
    public static <T> T intercepting(@NotNull Class<T> tClass, String description, @NotNull Consumer<String> consumer) {
        return intercepting(tClass, description, consumer, null);
    }

    @NotNull
    public static <T> T intercepting(@NotNull Class<T> tClass, @NotNull final String description, @NotNull Consumer<String> consumer, T t) {
        return intercepting(tClass,
                (name, args) -> consumer.accept(description + name + (args == null ? "()" : Arrays.toString(args))),
                t);
    }

    @NotNull
    public static <T> T intercepting(@NotNull Class<T> tClass, @NotNull BiConsumer<String, Object[]> consumer, T t) {
        final Set<Class<?>> classes = new LinkedHashSet<>();
        addInterface(classes, tClass);
        //noinspection unchecked
        try {
            return (T) Proxy.newProxyInstance(tClass.getClassLoader(), classes.toArray(NO_CLASSES), new AbstractInvocationHandler(tClass) {
                @Override
                protected Object doInvoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
                    consumer.accept(method.getName(), args);
                    try {
                        if (t != null)
                            return method.invoke(t, args);
                        return null;
                    } catch (IllegalArgumentException e) {
                        throw new AssertionError(e);
                    }
                }
            });
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        }
    }

    private static <T> void addInterface(Set<Class<?>> classes, Class<T> tClass) {
        if (Jvm.dontChain(tClass))
            return;
        if (classes.contains(tClass))
            return;
        classes.add(tClass);
        for (Method method : tClass.getMethods()) {
            final Type returnType0 = GenericReflection.getReturnType(method, tClass);
            if (returnType0 instanceof Class) {
                Class<?> returnType = (Class<?>) returnType0;
                if (returnType.isInterface())
                    addInterface(classes, returnType);
            }
        }
    }

    @NotNull
    public static <T> T ignored(@NotNull Class<T> tClass, Class<?>... additional) {
        final Set<Class<?>> classes = new LinkedHashSet<>();
        addInterface(classes, tClass);
        classes.add(tClass);
        for (Class<?> aClass : additional)
            addInterface(classes, aClass);
        classes.add(IgnoresEverything.class);
        try {
            //noinspection unchecked
            final T t = (T) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), classes.toArray(NO_CLASSES), new AbstractInvocationHandler(tClass) {
                @Override
                protected Object doInvoke(Object proxy, Method method, Object[] args) {
                    return null;
                }
            });
            return t;
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        }
    }
}
