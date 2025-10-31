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

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilSetAccessibleTest {

    // package-private class with a private method to trigger setAccessible branch
    static class PkgClass {
        private String greet() { return "ok"; }
    }

    @Test
    void getMethod0OnNonPublicClassMakesMethodUsable() throws Exception {
        Method m = ClassUtil.getMethod0(PkgClass.class, "greet", new Class<?>[0], true);
        assertNotNull(m);
        // The method should now be invokable despite being private
        String s = (String) m.invoke(new PkgClass());
        assertEquals("ok", s);
    }
}

