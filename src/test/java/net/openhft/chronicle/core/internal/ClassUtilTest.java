package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ClassUtilTest {

    private static class Parent {
        private final String parentField = "parent";
    }

    private static class Child extends Parent {
        private final int childField = 42;
    }

    @Test
    public void getField0ShouldAccessPrivateFieldInSuperclass() throws IllegalAccessException {
        Field field = ClassUtil.getField0(Child.class, "parentField", true);
        Child child = new Child();
        assertEquals("parent", field.get(child));
    }

    @Test
    public void getField0ShouldAccessPrivateFieldInClass() throws IllegalAccessException {
        Field field = ClassUtil.getField0(Child.class, "childField", true);
        Child child = new Child();
        assertEquals(42, field.get(child));
    }

    @Test
    public void setAccessibleShouldGrantAccessJava8() throws Exception {
        assumeFalse(Jvm.isJava9Plus());
        Field field = Parent.class.getDeclaredField("parentField");
        ClassUtil.setAccessible(field);
        Parent parent = new Parent();
        assertEquals("parent", field.get(parent));
    }

    @Test
    public void setAccessibleShouldGrantAccessJava9Plus() throws Exception {
        assumeTrue(Jvm.isJava9Plus());
        Field field = Parent.class.getDeclaredField("parentField");
        ClassUtil.setAccessible(field);
        Parent parent = new Parent();
        assertEquals("parent", field.get(parent));
    }
}
