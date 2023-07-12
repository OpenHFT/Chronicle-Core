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

import net.openhft.chronicle.core.ClassLocal;
import net.openhft.chronicle.core.internal.ClassUtil;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.io.InvalidMarshallableException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
 * Abstract base class for invocation handlers, which provides a mechanism for dynamically
 * dispatching method calls to arbitrary targets.
 * <p>
 * Subclasses should override the {@link #doInvoke(Object, Method, Object[])} method to provide custom
 * handling for method invocations.
 */
public abstract class AbstractInvocationHandler implements InvocationHandler {
    /**
     * Lookup which allows access to default methods in another package.
     */
    private static final ClassLocal<MethodHandles.Lookup> PRIVATE_LOOKUP = ClassLocal.withInitial(AbstractInvocationHandler::acquireLookup);

    /**
     * Constant for representing no arguments.
     */
    private static final Object[] NO_ARGS = {};

    /**
     * The type for which this invocation handler is defined.
     */
    private final Type definedClass;

    /**
     * Closeable to be invoked when close() is called.
     */
    private Closeable closeable;

    /**
     * Constructs a new invocation handler for the specified type.
     *
     * @param definedClass The type for which the invocation handler is defined.
     */
    protected AbstractInvocationHandler(Type definedClass) {
        this.definedClass = definedClass;
    }

    /**
     * Acquires a MethodHandles.Lookup instance for the specified class.
     *
     * @param c The class to get a MethodHandles.Lookup instance for.
     * @return MethodHandles.Lookup instance.
     */
    private static MethodHandles.Lookup acquireLookup(Class<?> c) {
        try {
            // try to create one using a constructor
            Constructor<MethodHandles.Lookup> lookupConstructor =
                    MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
            if (!lookupConstructor.isAccessible()) {
                ClassUtil.setAccessible(lookupConstructor);
            }
            return lookupConstructor.newInstance(c, MethodHandles.Lookup.PRIVATE);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException ignored) {
            // Do nothing. Continue below to recover.
        }
        try {
            // Try to grab an internal one,
            final Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            ClassUtil.setAccessible(field);
            return (MethodHandles.Lookup) field.get(null);
        } catch (Exception e) {
            // use the default to produce an error message.
            return MethodHandles.lookup();
        }
    }

    /**
     * Handles method invocations on proxy instances.
     *
     * @param proxy  The proxy instance.
     * @param method The method being called.
     * @param args   The arguments for the method call.
     * @return The result from the method call.
     * @throws Throwable if an error occurs during method invocation.
     */
    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass == Object.class) {
            return method.invoke(this, args);

        } else if (declaringClass == Closeable.class && method.getName().equals("close")) {
            Closeable.closeQuietly(closeable);
            return null;
        }

        if (args == null)
            args = NO_ARGS;

        Object o = doInvoke(proxy, method, args);
        if (o == null) {
            final Type returnType0 = GenericReflection.getReturnType(method, definedClass);
            Class returnType = returnType0 instanceof Class ? (Class) returnType0 : method.getReturnType();
            if (returnType.isInstance(proxy))
                return proxy; // assume it's a chained method.
            return ObjectUtils.defaultValue(method.getReturnType());
        }
        return o;
    }

    /**
     * Handles the actual invocation of the method. This method should be overridden by subclasses
     * to implement custom invocation behavior.
     *
     * @param proxy  The proxy instance.
     * @param method The method being called.
     * @param args   The arguments for the method call.
     * @return The result of the method invocation.
     * @throws InvocationTargetException    if the method being called throws an exception.
     * @throws IllegalAccessException       if this {@code InvocationHandler} does not have access to the method.
     * @throws IllegalStateException        if the proxy is in an inappropriate state for the method call.
     * @throws BufferOverflowException      if the buffer overflows during method invocation.
     * @throws BufferUnderflowException     if the buffer underflows during method invocation.
     * @throws IllegalArgumentException     if the method receives illegal arguments.
     * @throws ArithmeticException          if an arithmetic exception occurs during method invocation.
     * @throws InvalidMarshallableException if the object being marshaled is invalid.
     */
    protected abstract Object doInvoke(Object proxy, Method method, Object[] args)
            throws InvocationTargetException, IllegalAccessException, IllegalStateException, BufferOverflowException, BufferUnderflowException, IllegalArgumentException, ArithmeticException, InvalidMarshallableException;

    /**
     * Retrieves the MethodHandle for the specified method on the proxy.
     *
     * @param proxy The proxy instance.
     * @param m     The method to get the handle for.
     * @return The MethodHandle for the specified method on the proxy.
     */
    @SuppressWarnings("WeakerAccess")
    MethodHandle methodHandleForProxy(Object proxy, Method m) {
        try {
            Class<?> declaringClass = m.getDeclaringClass();
            final MethodHandles.Lookup lookup = PRIVATE_LOOKUP.get(declaringClass);
            return lookup
                    .in(declaringClass)
                    .unreflectSpecial(m, declaringClass)
                    .bindTo(proxy);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Sets a Closeable that should be called when close() is called on this invocation handler.
     *
     * @param closeable The Closeable to be invoked when close() is called.
     */
    public void onClose(Closeable closeable) {
        this.closeable = closeable;
    }
}
