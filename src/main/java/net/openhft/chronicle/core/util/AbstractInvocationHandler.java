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

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.ClassLocal;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractInvocationHandler implements InvocationHandler {
    // Lookup which allows access to default methods in another package.
    private static final ClassLocal<MethodHandles.Lookup> PRIVATE_LOOKUP = ClassLocal.withInitial(AbstractInvocationHandler::acquireLookup);
    private static final Object[] NO_ARGS = {};
    // called when close() is called.
    private Closeable closeable;

    /**
     * @param mapSupplier ConcurrentHashMap::new for thread safe, HashMap::new for single thread, Collections::emptyMap to turn off.
     */
    protected AbstractInvocationHandler(Supplier<Map> mapSupplier) {
        //noinspection unchecked
        Map<Object, Function<Method, MethodHandle>> proxyToLambda = mapSupplier.get();
        //noinspection unchecked
        Map<Method, MethodHandle> defaultMethod = mapSupplier.get();
    }

    private static MethodHandles.Lookup acquireLookup(Class<?> c) {
        try {
            // try to create one using a constructor
            Constructor<MethodHandles.Lookup> lookupConstructor =
                    MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
            if (!lookupConstructor.isAccessible()) {
                Jvm.setAccessible(lookupConstructor);
            }
            return lookupConstructor.newInstance(c, MethodHandles.Lookup.PRIVATE);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
        }
        try {
            // Try to grab an internal one,
            final Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Jvm.setAccessible(field);
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
        if (o == null && method.getReturnType().isInstance(proxy))
            return proxy; // assume it's a chained method.

        return o == null ? ObjectUtils.defaultValue(method.getReturnType()) : o;
    }

    /**
     * Default handler for method call.
     */
    protected abstract Object doInvoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException;

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
