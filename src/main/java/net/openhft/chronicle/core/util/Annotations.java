/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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
