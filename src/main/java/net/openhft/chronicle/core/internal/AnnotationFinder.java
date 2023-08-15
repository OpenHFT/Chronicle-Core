package net.openhft.chronicle.core.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class for handling annotation discovery on annotated elements.
 */
public class AnnotationFinder {

    /**
     * Retrieve an annotation of the specified {@code annotationType} that is present on the given
     * {@code annotatedElement}, or null if not found.
     * 
     * @param annotatedElement the annotated element to inspect.
     * @param annotationType   the type of the annotation to retrieve.
     * @param <A>              the type of the annotation.
     * @return the annotation, or null if not found.
     */
    public static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        HashSet<Annotation> visited = new HashSet<>();
        A annotationRecursively = findAnnotationRecursively(annotatedElement, annotationType, visited);
        if (annotationRecursively == null && annotatedElement instanceof Method) {
            Method method = (Method) annotatedElement;
            return findAnnotationForMethod(annotationType, method.getDeclaringClass(), method.getName(), method.getParameterTypes(), visited);
        }
        return annotationRecursively;
    }

    /**
     * Recursive utility method for annotation retrieval. Explores direct annotations, nested annotations, and
     * annotations in the class hierarchy.
     *
     * @param annotatedElement the annotated element to inspect.
     * @param annotationType   the type of the annotation to retrieve.
     * @param visited          set of already checked annotations to avoid infinite loops.
     * @param <A>              the type of the annotation.
     * @return the annotation, or null if not found.
     */
    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A findAnnotationRecursively(AnnotatedElement annotatedElement, Class<A> annotationType, Set<Annotation> visited) {
        try {
            // Check for direct annotation
            A annotation = annotatedElement.getDeclaredAnnotation(annotationType);
            if (annotation != null) {
                return annotation;
            }

            // Check for nested direct annotation
            annotation = findNestedDirectAnnotation(annotatedElement, annotationType, visited);
            if (annotation != null) {
                return annotation;
            }

        } catch (Exception ex) {
            return null; // Opt to return null on any exception during retrieval.
        }
        return null;
    }

    /**
     * Check directly declared annotations to see if they have the desired annotation nested within them.
     *
     * @param annotatedElement the annotated element to inspect.
     * @param annotationType   the type of the annotation to retrieve.
     * @param visited          set of already checked annotations to avoid infinite loops.
     * @param <A>              the type of the annotation.
     * @return the annotation, or null if not found.
     */
    private static <A extends Annotation> A findNestedDirectAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType, Set<Annotation> visited) {
        for (Annotation ann : annotatedElement.getDeclaredAnnotations()) {
            if (visited.add(ann)) {
                A annotation = findAnnotationRecursively(ann.annotationType(), annotationType, visited);
                if (annotation != null) {
                    return annotation;
                }
            }
        }
        return null;
    }

    /**
     * Finds the specified annotation on a method of a given class, its superclass, or interfaces.
     *
     * @param <A>            the type of the annotation to be fetched
     * @param annoClass      the Class object corresponding to the annotation type
     * @param aClass         the Class object in which to find the method
     * @param name           the name of the method
     * @param parameterTypes the parameter types of the method
     * @param visited          set of already checked annotations to avoid infinite loops.
     * @return the annotation of type {@code A} if present, or {@code null} otherwise
     */
    private static <A extends Annotation> A findAnnotationForMethod(Class<A> annoClass, Class<?> aClass, String name, Class<?>[] parameterTypes, HashSet<Annotation> visited) {
        try {
            Method m = aClass.getMethod(name, parameterTypes);
            A methodId = findAnnotationRecursively(m, annoClass, visited);
            if (methodId != null)
                return methodId;
        } catch (NoSuchMethodException e) {
            // ignored
        }
        Class<?> superclass = aClass.getSuperclass();
        if (!(superclass == null || superclass == Object.class)) {
            A methodId = findAnnotationForMethod(annoClass, superclass, name, parameterTypes, visited);
            if (methodId != null)
                return methodId;
        }
        for (Class<?> iClass : aClass.getInterfaces()) {
            A methodId = findAnnotationForMethod(annoClass, iClass, name, parameterTypes, visited);
            if (methodId != null)
                return methodId;
        }
        return null;
    }
}