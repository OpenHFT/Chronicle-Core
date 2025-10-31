/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.internal;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilExtraTest {

    private static class Parent {
        @SuppressWarnings("unused")
        private int hidden = 42;
        @SuppressWarnings("unused")
        private String greet() { return "hi"; }
    }

    private static class Child extends Parent { }

    @Test
    void getField0FindsPrivateFieldInHierarchy() {
        Field f = ClassUtil.getField0(Child.class, "hidden", true, true);
        assertNotNull(f);
        assertEquals("hidden", f.getName());
    }

    @Test
    void getField0ReturnsNullWhenMissingAndErrorFalse() {
        Field f = ClassUtil.getField0(Child.class, "nope", false, true);
        assertNull(f);
    }

    @Test
    void getField0ThrowsWhenMissingAndErrorTrue() {
        assertThrows(AssertionError.class, () -> ClassUtil.getField0(Child.class, "nope", true, true));
    }

    @Test
    void getMethod0FindsPrivateMethodInHierarchy() {
        Method m = ClassUtil.getMethod0(Child.class, "greet", new Class<?>[0], true);
        assertNotNull(m);
        assertEquals("greet", m.getName());
    }
}

