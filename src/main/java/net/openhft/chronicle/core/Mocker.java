/*
 * Copyright 2016-2020 Chronicle Software
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
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public enum Mocker {
    ;

    private static final Class[] NO_CLASSES = new Class[0];

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
        Set<Class> classes = new LinkedHashSet<>();
        addInterface(classes, tClass);
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), classes.toArray(NO_CLASSES), new AbstractInvocationHandler(ConcurrentHashMap::new) {
            @Override
            protected Object doInvoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
                consumer.accept(method.getName(), args);
                if (t != null)
                    return method.invoke(t, args);
                return null;
            }
        });
    }

    private static <T> void addInterface(Set<Class> classes, Class<T> tClass) {
        if (Jvm.dontChain(tClass))
            return;
        if (classes.contains(tClass))
            return;
        classes.add(tClass);
        for (Method method : tClass.getMethods()) {
            Class<?> returnType = method.getReturnType();
            if (returnType.isInterface())
                addInterface(classes, returnType);
        }
    }

    @NotNull
    public static <T> T ignored(@NotNull Class<T> tClass) {
        Set<Class> classes = new LinkedHashSet<>();
        addInterface(classes, tClass);
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), classes.toArray(NO_CLASSES), new AbstractInvocationHandler(ConcurrentHashMap::new) {
            @Override
            protected Object doInvoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
                return null;
            }
        });
    }
}
