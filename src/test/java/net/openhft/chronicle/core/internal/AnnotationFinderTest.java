package net.openhft.chronicle.core.internal;

import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;

public class AnnotationFinderTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Marker {
    }

    interface ParentInterface {
        @Marker
        void fromInterface();
    }

    static class ParentClass {
        @Marker
        public void fromParent() {
        }
    }

    static class ChildClass extends ParentClass implements ParentInterface {
        @Override
        public void fromInterface() {
        }

        @Override
        public void fromParent() {
            super.fromParent();
        }
    }

    @Test
    public void findAnnotationOnInheritedMethod() throws NoSuchMethodException {
        Method method = ChildClass.class.getMethod("fromParent");
        Marker marker = AnnotationFinder.findAnnotation(method, Marker.class);
        assertNotNull(marker);
    }

    @Test
    public void findAnnotationOnInterfaceMethod() throws NoSuchMethodException {
        Method method = ChildClass.class.getMethod("fromInterface");
        Marker marker = AnnotationFinder.findAnnotation(method, Marker.class);
        assertNotNull(marker);
    }
}
