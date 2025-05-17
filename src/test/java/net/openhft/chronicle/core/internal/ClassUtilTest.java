package net.openhft.chronicle.core.internal;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ClassUtilTest {

    static class Parent {
        protected void inheritedMethod() {
        }
    }

    static class Child extends Parent {
    }

    static class WithPrivate {
        private void privateMethod() {
        }
    }

    @Test
    public void getMethod0ShouldReturnInheritedMethod() {
        Method method = ClassUtil.getMethod0(Child.class, "inheritedMethod", new Class[0], true);
        assertNotNull(method);
        assertTrue(method.isAccessible());
    }

    @Test
    public void getMethod0ShouldReturnPrivateMethod() {
        Method method = ClassUtil.getMethod0(WithPrivate.class, "privateMethod", new Class[0], true);
        assertNotNull(method);
        assertTrue(method.isAccessible());
    }
}
