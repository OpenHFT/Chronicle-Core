package net.openhft.chronicle.core.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
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
        return findAnnotationRecursively(annotatedElement, annotationType, new HashSet<>());
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
            A annotation = findDirectAnnotation(annotatedElement, annotationType);
            if (annotation != null) {
                return annotation;
            }

            // Check for nested direct annotation
            annotation = findNestedDirectAnnotation(annotatedElement, annotationType, visited);
            if (annotation != null) {
                return annotation;
            }

            // Check for any inherited annotations
            annotation = annotatedElement.getAnnotation(annotationType);
            if (annotation != null) {
                return annotation;
            }

            // Check for nested inherited annotations
            annotation = findNestedAnnotation(annotatedElement, annotationType, visited);
            if (annotation != null) {
                return annotation;
            }

        } catch (Exception ex) {
            return null; // Opt to return null on any exception during retrieval.
        }

        // If we're working with a class, search its hierarchy
        return (annotatedElement instanceof Class) ? findAnnotationInHierarchy((Class<?>) annotatedElement, annotationType, visited) : null;
    }

    /**
     * Retrieve an annotation of the specified {@code annotationType} that is directly present on the given
     * {@code annotatedElement}, without checking nested annotations.
     *
     * @param annotatedElement the annotated element to inspect.
     * @param annotationType   the type of the annotation to retrieve.
     * @param <A>              the type of the annotation.
     * @return the annotation, or null if not found.
     */
    private static <A extends Annotation> A findDirectAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        for (Annotation ann : annotatedElement.getDeclaredAnnotations()) {
            if (ann.annotationType() == annotationType) {
                return (A) ann;
            }
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
     * Explore all annotations (including inherited ones) to see if they have the desired annotation nested within them.
     *
     * @param annotatedElement the annotated element to inspect.
     * @param annotationType   the type of the annotation to retrieve.
     * @param visited          set of already checked annotations to avoid infinite loops.
     * @param <A>              the type of the annotation.
     * @return the annotation, or null if not found.
     */
    private static <A extends Annotation> A findNestedAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType, Set<Annotation> visited) {
        for (Annotation ann : annotatedElement.getAnnotations()) {
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
     * Search through the hierarchy of the given class for the desired annotation.
     *
     * @param clazz          the class whose hierarchy should be searched.
     * @param annotationType the type of the annotation to retrieve.
     * @param visited        set of already checked annotations to avoid infinite loops.
     * @param <A>            the type of the annotation.
     * @return the annotation, or null if not found.
     */
    private static <A extends Annotation> A findAnnotationInHierarchy(Class<?> clazz, Class<A> annotationType, Set<Annotation> visited) {
        for (Class<?> ifc : clazz.getInterfaces()) {
            A annotation = findAnnotationRecursively(ifc, annotationType, visited);
            if (annotation != null) {
                return annotation;
            }
        }

        Class<?> superclass = clazz.getSuperclass();
        return (superclass == null || Object.class == superclass)
                ? null
                : findAnnotationRecursively(superclass, annotationType, visited);
    }
}