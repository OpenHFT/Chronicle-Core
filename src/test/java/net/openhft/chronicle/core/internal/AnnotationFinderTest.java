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
    @DisplayName("findAnnotation returns DirectlyAnnotated for class level annotation")
    void findDirectAnnotationOnClass() {
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(DirectlyAnnotatedClass.class, DirectlyAnnotated.class);
        assertNotNull(annotation, "DirectlyAnnotatedClass should include DirectlyAnnotated class annotation");
        assertEquals("class-level", annotation.value(),
                "DirectlyAnnotated class annotation value should be class-level");
    }

    @Test
    @DisplayName("findAnnotation returns null when DirectlyAnnotated marker annotation is absent")
    void findAnnotationReturnsNullWhenNotPresent() {
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(UnannotatedClass.class, DirectlyAnnotated.class);
        assertNull(annotation, "UnannotatedClass should have no DirectlyAnnotated class annotation");
    }

    @Test
    @DisplayName("findAnnotation finds DirectlyAnnotated via nested meta-annotation")
    void findNestedMetaAnnotation() {
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(MetaAnnotatedClass.class, DirectlyAnnotated.class);
        assertNotNull(annotation, "MetaAnnotatedClass should include DirectlyAnnotated meta-annotation");
        assertEquals("meta", annotation.value(),
                "MetaAnnotatedClass meta-annotation value should be meta");
    }

    @Test
    @DisplayName("findAnnotation lookup returns DirectlyAnnotated marker for method annotation")
    void findDirectAnnotationOnMethod() throws NoSuchMethodException {
        Method method = DirectlyAnnotatedClass.class.getMethod("annotatedMethod");
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(method, DirectlyAnnotated.class);
        assertNotNull(annotation, "annotatedMethod should include DirectlyAnnotated method annotation");
        assertEquals("method-level", annotation.value(),
                "annotatedMethod annotation value should be method-level");
    }

    @Test
    @DisplayName("findAnnotation finds DirectlyAnnotated from superclass method")
    void findAnnotationFromSuperclassMethod() throws NoSuchMethodException {
        Method method = ChildClass.class.getMethod("inheritedMethod");
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(method, DirectlyAnnotated.class);
        assertNotNull(annotation, "inheritedMethod should include DirectlyAnnotated from superclass");
        assertEquals("inherited", annotation.value(),
                "inheritedMethod annotation value should be inherited");
    }

    @Test
    @DisplayName("findAnnotation finds DirectlyAnnotated from interface method")
    void findAnnotationFromInterfaceMethod() throws NoSuchMethodException {
        Method method = InterfaceImpl.class.getMethod("interfaceMethod");
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(method, DirectlyAnnotated.class);
        assertNotNull(annotation, "interfaceMethod should include DirectlyAnnotated from interface");
        assertEquals("interface", annotation.value(),
                "interfaceMethod annotation value should be interface");
    }

    @Test
    @DisplayName("findAnnotation returns null when DirectlyAnnotated marker on method is absent")
    void findAnnotationReturnsNullForUnannotatedMethod() throws NoSuchMethodException {
        Method method = UnannotatedClass.class.getMethod("unannotatedMethod");
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(method, DirectlyAnnotated.class);
        assertNull(annotation, "unannotatedMethod should have no DirectlyAnnotated method annotation");
    }

    @Test
    @DisplayName("findAnnotation handles method parameters and returns annotation")
    void findAnnotationOnMethodWithParameters() throws NoSuchMethodException {
        Method method = DirectlyAnnotatedClass.class.getMethod("methodWithParams", String.class, int.class);
        DirectlyAnnotated annotation = AnnotationFinder.findAnnotation(method, DirectlyAnnotated.class);
        assertNotNull(annotation, "methodWithParams should include DirectlyAnnotated method annotation");
        assertEquals("with-params", annotation.value(),
                "methodWithParams annotation value should be with-params");
    }

    @Test
    @DisplayName("findAnnotation avoids infinite loop with cyclic meta-annotations")
    void findAnnotationHandlesCyclicMetaAnnotations() {
        assertDoesNotThrow(
                () -> AnnotationFinder.findAnnotation(CyclicAnnotatedClass.class, DirectlyAnnotated.class),
                "AnnotationFinder should handle cyclic meta-annotations without looping");
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
