package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public enum Annotations {
    ;

    public static <A extends Annotation> A findAnnotation(Class<A> annoClass, Class<?> aClass, String name, Class<?>[] parameterTypes) {
        A methodId;
        try {
            Method m = aClass.getMethod(name, parameterTypes);
            methodId = m.getAnnotation(annoClass);
            if (methodId != null)
                return methodId;
        } catch (NoSuchMethodException e) {
            // ignored
        }
        Class<?> superclass = aClass.getSuperclass();
        if (!(superclass == null || superclass == Object.class)) {
            methodId = findAnnotation(annoClass, superclass, name, parameterTypes);
            if (methodId != null)
                return methodId;
        }
        for (Class<?> iClass : aClass.getInterfaces()) {
            methodId = findAnnotation(annoClass, iClass, name, parameterTypes);
            if (methodId != null)
                return methodId;
        }
        return null;
    }

    @Nullable
    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationClass) {
        A methodId = method.getAnnotation(annotationClass);
        if (methodId == null) {
            methodId = findAnnotation(annotationClass, method.getDeclaringClass(), method.getName(), method.getParameterTypes());
            if (methodId == null)
                return null;
        }
        return methodId;
    }
}
