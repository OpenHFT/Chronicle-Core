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

public abstract class AbstractInvocationHandler implements InvocationHandler {
    // Lookup which allows access to default methods in another package.
    private static final ClassLocal<MethodHandles.Lookup> PRIVATE_LOOKUP = ClassLocal.withInitial(AbstractInvocationHandler::acquireLookup);
    private static final Object[] NO_ARGS = {};
    private final Type definedClass;
    // called when close() is called.
    private Closeable closeable;

    protected AbstractInvocationHandler(Type definedClass) {
        this.definedClass = definedClass;
    }

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
     * Default handler for method call.
     */
    protected abstract Object doInvoke(Object proxy, Method method, Object[] args)
            throws InvocationTargetException, IllegalAccessException, IllegalStateException, BufferOverflowException, BufferUnderflowException, IllegalArgumentException, ArithmeticException, InvalidMarshallableException;

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

    public void onClose(Closeable closeable) {
        this.closeable = closeable;
    }
}
