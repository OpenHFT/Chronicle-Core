package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GenericReflectionTest {

    @Test
    void getReturnType() {
        assertEquals(String.class,
                GenericReflection.getReturnType(Returns.class.getMethods()[0], ReturnsString.class));

        assertEquals(Integer.class,
                GenericReflection.getReturnType(Returns.class.getMethods()[0], ReturnsInteger.class));

        assertEquals(Double.class,
                GenericReflection.getReturnType(Returns2.class.getMethods()[0], Returns2Double.class));

        Type t = GenericReflection.getReturnType(ReturnsReturnsAString.class.getMethods()[0], ReturnsReturnsAString.class);
        assertEquals(new TypeOf<Returns<String>>() {
        }.type(), t);
    }

    @Test
    public void getMethodReturnTypes() {
        final Type returnString = new TypeOf<Returns<String>>() {
        }.type();
        final Set<Type> methodReturnTypes = GenericReflection.getMethodReturnTypes(ReturnsString.class);
        assertEquals(methodReturnTypes,
                GenericReflection.getMethodReturnTypes(returnString));
    }

    @Test
    public void getGenericInterfaces() {
        final Type returnString = new TypeOf<Returns<String>>() {
        }.type();
        final Type[] genericInterfaces = GenericReflection.getGenericInterfaces(ReturnsString.class);
        assertArrayEquals(genericInterfaces,
                GenericReflection.getGenericInterfaces(returnString));
    }

    @Test
    public void getGenericSuperclass() {
        final Type returnString = new TypeOf<Returns<String>>() {
        }.type();
        final Type genericSuperclass = GenericReflection.getGenericSuperclass(ReturnsString.class);
        assertEquals(genericSuperclass,
                GenericReflection.getGenericSuperclass(returnString));
    }

    interface Returns<A> {
        A ret();
    }

    interface ReturnsReturnsAString {
        Returns<String> retRS();
    }

    interface ReturnsString extends Returns<String> {
    }

    class ReturnsInteger implements Returns<Integer> {
        @Override
        public Integer ret() {
            return null;
        }
    }

    class Returns2<A> {
        public A ret() {
            return null;
        }
    }

    class Returns2Double extends Returns2<Double> {
        @Override
        public Double ret() {
            return 1.0;
        }
    }
}