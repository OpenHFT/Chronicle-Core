package net.openhft.chronicle.core.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class for handling annotation discovery on annotated elements, considering direct annotations,
 * nested annotations, and inherited annotations.
 */
public class AnnotationFinder {

    /**
     * Retrieve an annotation of the specified {@code annotationType} that is present on the given
     * {@code annotatedElement}, including considering nested annotations and method inheritance.
     * If the annotation isn't found, this method returns {@code null}.
     *
     * @param annotatedElement the element (e.g., class, method, field) to inspect for annotations.
     * @param annotationType   the desired annotation's type.
     * @param <A>              denotes the annotation type.
     * @return the found annotation of type {@code A} or null if not present.
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
     * A recursive method that attempts to find a specific annotation on a method considering
     * the given class, its superclass, and its interfaces.
     *
     * @param <A>            denotes the annotation type.
     * @param annoClass      annotation type to find.
     * @param aClass         class within which to search for the method and its annotation.
     * @param name           method's name.
     * @param parameterTypes method's parameter types.
     * @param visited        keeps track of annotations already checked to prevent infinite loops.
     * @return the found annotation of type {@code A} or null if not present.
     */
    private static <A extends Annotation> A findAnnotationForMethod(Class<A> annoClass, Class<?> aClass, String name, Class<?>[] parameterTypes, HashSet<Annotation> visited) {
        try {
            Method m = aClass.getMethod(name, parameterTypes);
            A annotationOnMethod = findAnnotationRecursively(m, annoClass, visited);
            if (annotationOnMethod != null) {
                return annotationOnMethod;
            }
        } catch (NoSuchMethodException e) {
            // Method not found in the current class, will proceed to check superclass/interfaces.
        }

        // Check superclass
        Class<?> superclass = aClass.getSuperclass();
        if (!(superclass == null || superclass == Object.class)) {
            A annotationInSuperclass = findAnnotationForMethod(annoClass, superclass, name, parameterTypes, visited);
            if (annotationInSuperclass != null) {
                return annotationInSuperclass;
            }
        }

        // Check interfaces
        for (Class<?> iClass : aClass.getInterfaces()) {
            A annotationInInterface = findAnnotationForMethod(annoClass, iClass, name, parameterTypes, visited);
            if (annotationInInterface != null) {
                return annotationInInterface;
            }
        }
        return null;
    }
}
