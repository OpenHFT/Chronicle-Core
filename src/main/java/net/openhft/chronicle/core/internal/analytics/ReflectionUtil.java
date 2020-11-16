package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

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

    public static Object analyticsBuilder(@NotNull final String measurementId, @NotNull final String apiSecret) {
        try {
            final Method method = methodOrThrow(ANALYTICS_NAME, "builder", String.class, String.class);
            return method.invoke(null, measurementId, apiSecret);
        } catch (ReflectiveOperationException e) {
            Jvm.rethrow(e);
            return null;
        }
    }

    public static Method methodOrThrow(String className, String methodName, Class<?>... parameterTypes) {
        try {
            final Class<?> analyticsClass = Class.forName(className);
            final Method method = analyticsClass.getMethod(methodName, parameterTypes);
            return method;
        } catch (ReflectiveOperationException e) {
            Jvm.rethrow(e);
            return null;
        }
    }

    public static Object invokeOrThrow(Method m, Object target, Object... params) {
        try {
            return m.invoke(target, params);
        } catch (ReflectiveOperationException e) {
            Jvm.rethrow(e);
            return null;
        }
    }
}