/*
 * Copyright 2016-2020 chronicle.software
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
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
/**
 * Utility class for working with annotations on methods and classes.
 * <p>
 * Provides helper methods to fetch annotations from a method or class, including its superclasses and interfaces.
 * <p>
 * Note: This class is deprecated and will be removed in version x.26. Use {@link Jvm#findAnnotation(AnnotatedElement, Class)} instead.
 */
@Deprecated(/* to be removed in x.26, replaced by Jvm.findAnnotation */)
public final class Annotations {
    private Annotations() { }

    /**
     * Finds the specified annotation on a method of a given class, its superclass, or interfaces.
     *
     * @param <A> the type of the annotation to be fetched
     * @param annoClass the Class object corresponding to the annotation type
     * @param aClass the Class object in which to find the method
     * @param name the name of the method
     * @param parameterTypes the parameter types of the method
     * @return the annotation of type {@code A} if present, or {@code null} otherwise
     */
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

    /**
     * Fetches the specified annotation from a method. If the method does not have the annotation,
     * it attempts to find it in the declaring class, its superclass, or interfaces.
     *
     * @param <A> the type of the annotation to be fetched
     * @param method the method from which to fetch the annotation
     * @param annotationClass the Class object corresponding to the annotation type
     * @return the annotation of type {@code A} if present, or {@code null} otherwise
     */
    @Nullable
    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationClass) {
        A methodId = method.getAnnotation(annotationClass);
        if (methodId == null) {
            methodId = findAnnotation(annotationClass, method.getDeclaringClass(), method.getName(), method.getParameterTypes());
        }
        return methodId;
    }
}
