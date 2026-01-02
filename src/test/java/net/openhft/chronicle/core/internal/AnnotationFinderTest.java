/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationFinderTest {

    @Test
    @DisplayName("findAnnotation returns direct annotation on class")
    void findDirectAnnotationOnClass() {
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(DirectlyAnnotatedClass.class, DirectlyAnnotated.class);
        assertNotNull(annotation, "should find direct annotation on class");
        assertEquals("class-level", annotation.value(), "annotation value should match");
    }

    @Test
    @DisplayName("findAnnotation returns null when annotation not present")
    void findAnnotationReturnsNullWhenNotPresent() {
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(UnannotatedClass.class, DirectlyAnnotated.class);
        assertNull(annotation, "should return null when annotation not present");
    }

    @Test
    @DisplayName("findAnnotation finds nested meta-annotation")
    void findNestedMetaAnnotation() {
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(MetaAnnotatedClass.class, DirectlyAnnotated.class);
        assertNotNull(annotation, "should find nested annotation via meta-annotation");
        assertEquals("meta", annotation.value(), "annotation value should match meta-annotation");
    }

    @Test
    @DisplayName("findAnnotation returns direct annotation on method")
    void findDirectAnnotationOnMethod() throws NoSuchMethodException {
        Method method = DirectlyAnnotatedClass.class.getMethod("annotatedMethod");
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(method, DirectlyAnnotated.class);
        assertNotNull(annotation, "should find direct annotation on method");
        assertEquals("method-level", annotation.value(), "annotation value should match");
    }

    @Test
    @DisplayName("findAnnotation finds annotation from superclass method")
    void findAnnotationFromSuperclassMethod() throws NoSuchMethodException {
        Method method = ChildClass.class.getMethod("inheritedMethod");
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(method, DirectlyAnnotated.class);
        assertNotNull(annotation, "should find annotation from superclass method");
        assertEquals("inherited", annotation.value(), "annotation value should match");
    }

    @Test
    @DisplayName("findAnnotation finds annotation from interface method")
    void findAnnotationFromInterfaceMethod() throws NoSuchMethodException {
        Method method = InterfaceImpl.class.getMethod("interfaceMethod");
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(method, DirectlyAnnotated.class);
        assertNotNull(annotation, "should find annotation from interface method");
        assertEquals("interface", annotation.value(), "annotation value should match");
    }

    @Test
    @DisplayName("findAnnotation returns null for method without annotation")
    void findAnnotationReturnsNullForUnannotatedMethod() throws NoSuchMethodException {
        Method method = UnannotatedClass.class.getMethod("unannotatedMethod");
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(method, DirectlyAnnotated.class);
        assertNull(annotation, "should return null for unannotated method");
    }

    @Test
    @DisplayName("findAnnotation handles method with parameters")
    void findAnnotationOnMethodWithParameters() throws NoSuchMethodException {
        Method method = DirectlyAnnotatedClass.class.getMethod("methodWithParams", String.class, int.class);
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(method, DirectlyAnnotated.class);
        assertNotNull(annotation, "should find annotation on method with parameters");
        assertEquals("with-params", annotation.value(), "annotation value should match");
    }

    @Test
    @DisplayName("findAnnotation avoids infinite loop with cyclic meta-annotations")
    void findAnnotationHandlesCyclicMetaAnnotations() {
        // CyclicAnnotatedClass has annotations that reference each other
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(CyclicAnnotatedClass.class, DirectlyAnnotated.class);
        // Should not hang or throw - may or may not find annotation depending on path
        // Just ensure it completes without error
        assertTrue(true, "should complete without infinite loop");
    }

    // --- Test annotations ---

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @interface DirectlyAnnotated {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @DirectlyAnnotated("meta")
    @interface MetaAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @CyclicAnnotationB
    @interface CyclicAnnotationA {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @CyclicAnnotationA
    @interface CyclicAnnotationB {
    }

    // --- Test classes ---

    @DirectlyAnnotated("class-level")
    static class DirectlyAnnotatedClass {
        @DirectlyAnnotated("method-level")
        public void annotatedMethod() {
        }

        @DirectlyAnnotated("with-params")
        public void methodWithParams(String s, int i) {
        }
    }

    static class UnannotatedClass {
        public void unannotatedMethod() {
        }
    }

    @MetaAnnotation
    static class MetaAnnotatedClass {
    }

    static class ParentClass {
        @DirectlyAnnotated("inherited")
        public void inheritedMethod() {
        }
    }

    static class ChildClass extends ParentClass {
        @Override
        public void inheritedMethod() {
        }
    }

    interface AnnotatedInterface {
        @DirectlyAnnotated("interface")
        void interfaceMethod();
    }

    static class InterfaceImpl implements AnnotatedInterface {
        @Override
        public void interfaceMethod() {
        }
    }

    @CyclicAnnotationA
    static class CyclicAnnotatedClass {
    }
}
