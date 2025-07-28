/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GenericReflectionTest extends CoreTestCommon {

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
    public void getGenericClassesSuperclassesAndInterfaces() {
        final Type returnString = new TypeOf<Returns<String>>() {
        }.type();
        final Object[] genericInterfaces = GenericReflection.getGenericClassesSuperclassesAndInterfaces(ReturnsString.class).toArray();
        assertArrayEquals(genericInterfaces,
                GenericReflection.getGenericClassesSuperclassesAndInterfaces(returnString).toArray());
    }

    @Test
    public void getParameterTypes() throws NoSuchMethodException {
        final Method method = GenericMethod.class.getDeclaredMethod("method", Object.class, Object.class);
        final String expected = "[class java.lang.Byte, class java.lang.Short]";
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, ExtendsGenericMethod.class)));
        final Method omethod = OverridesGenericMethod.class.getDeclaredMethod("method", Byte.class, Short.class);
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(omethod, OverridesGenericMethod.class)));
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, OverridesGenericMethod.class)));
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, NestedExtendsGenericMethod.class)));
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, OverlyNestedExtendsGenericMethod.class)));
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, MassivelyNestedExtendsGenericMethod.class)));

        final Method method0 = OverlyNestedExtendsGenericMethod.class.getDeclaredMethod("method0", Object.class);
        assertEquals("[class java.lang.Long]",
                Arrays.toString(GenericReflection.getParameterTypes(method0, MassivelyNestedExtendsGenericMethod.class)));
    }

    @Test
    public void getParameterTypesExtends() {
        Method method = null;
        for (Method m : GenericMethodExtends.class.getMethods()) {
            if (m.getName().equals("method")) {
                method = m;
                break;
            }
        }
        assertNotNull(method);
        final String expected = "[" + Number.class + ", " + CharSequence.class + "]";
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, GenericMethodExtends.class)));
    }

    interface Returns<A> {
        A ret();
    }

    interface ReturnsReturnsAString {
        Returns<String> retRS();
    }

    interface ReturnsString extends Returns<String> {
    }

    interface GenericMethodExtends<A extends Number, B extends CharSequence> {
        void method(A a, B b);
    }

    interface GenericMethod<A, B> {
        @SuppressWarnings("EmptyMethod")
        void method(A a, B b);
    }

    interface ExtendsGenericMethod extends GenericMethod<Byte, Short> {

    }

    interface OverridesGenericMethod extends GenericMethod<Byte, Short> {
        void method(Byte b, Short s);
    }

    interface NestedExtendsGenericMethod extends ExtendsGenericMethod {

    }

    interface OverlyNestedExtendsGenericMethod<A> extends NestedExtendsGenericMethod {
        void method0(A a);
    }

    interface MassivelyNestedExtendsGenericMethod extends OverlyNestedExtendsGenericMethod<Long>, OverridesGenericMethod {
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
