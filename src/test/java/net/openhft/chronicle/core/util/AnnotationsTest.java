package net.openhft.chronicle.core.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AnnotationsTest {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnnotation {
    }

    public interface TestInterface {
        @TestAnnotation
        void interfaceMethod();
    }

    public static class SuperClass {
        public void superMethod() {
        }
    }

    public static class TestClass extends SuperClass implements TestInterface {
        public void testMethod() {
        }

        @Override
        public void interfaceMethod() {
        }
    }

    @Test
    public void findAnnotation_OnMethod() {
        TestAnnotation annotation = Annotations.findAnnotation(TestAnnotation.class, TestClass.class, "interfaceMethod", new Class<?>[0]);

        assertNotNull(annotation);
    }

    @Test
    public void findAnnotation_NotPresent() {
        TestAnnotation annotation = Annotations.findAnnotation(TestAnnotation.class, TestClass.class, "testMethod", new Class<?>[0]);

        assertNull(annotation);
    }

    @Test
    public void getAnnotation_DirectlyOnMethod() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("interfaceMethod");
        TestAnnotation annotation = Annotations.getAnnotation(method, TestAnnotation.class);

        assertNotNull(annotation);
    }

    @Test
    public void getAnnotation_NotPresent() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        TestAnnotation annotation = Annotations.getAnnotation(method, TestAnnotation.class);

        assertNull(annotation);
    }
}
