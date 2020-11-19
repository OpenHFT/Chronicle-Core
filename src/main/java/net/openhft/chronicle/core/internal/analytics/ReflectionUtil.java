package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.stream.Stream;

public final class ReflectionUtil {

    private static final String ANALYTICS_NAME = "net.openhft.chronicle.analytics.Analytics";

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
        try {
            final Method method = methodOrThrow(ANALYTICS_NAME, "builder", String.class, String.class);
            return method.invoke(null, measurementId, apiSecret);
        } catch (ReflectiveOperationException e) {
            Jvm.rethrow(e);
            return null;
        }
    }

    @NotNull
    public static Method methodOrThrow(@NotNull final String className,
                                       @NotNull final String methodName,
                                       final Class<?>... parameterTypes) {
        try {
            final Class<?> analyticsClass = Class.forName(className);
            final Method method = analyticsClass.getMethod(methodName, parameterTypes);
            return method;
        } catch (ReflectiveOperationException e) {
            Jvm.rethrow(e);
            return null;
        }
    }

    public static Object invokeOrThrow(@NotNull final Method m,
                                       @NotNull final Object target,
                                       Object... params) {
        try {
            return m.invoke(target, params);
        } catch (ReflectiveOperationException e) {
            Jvm.rethrow(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> T reflectiveProxy(@NotNull final Class<T> interf, @NotNull final Object delegate) {
        return (T) Proxy.newProxyInstance(delegate.getClass().getClassLoader(), new Class[]{interf}, new ReflectiveInvocationHandler(delegate, false));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> T reflectiveProxy(@NotNull final Class<T> interf,
                                        @NotNull final Object delegate,
                                        final boolean returnProxy) {
        return (T) Proxy.newProxyInstance(delegate.getClass().getClassLoader(), new Class[]{interf}, new ReflectiveInvocationHandler(delegate, returnProxy));
    }

    private static final class ReflectiveInvocationHandler implements InvocationHandler {

        private final Object delegate;
        private final boolean returnProxy;

        public ReflectiveInvocationHandler(@NotNull final Object delegate, final boolean returnProxy) {
            this.delegate = delegate;
            this.returnProxy = returnProxy;
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
            if (delegateMethod == null) {
                throw new RuntimeException(String.format("Class %s does not have a method %s(%s)", delegate.getClass(), method.getName(), Arrays.toString(parameterTypes)));
            }
            final Object result = delegateMethod.invoke(delegate, args);
            if (returnProxy && !"build".equals(method.getName())) {
                return proxy;
            } else {
                return result;
            }
        }
    }
}