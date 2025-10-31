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
package net.openhft.chronicle.core.internal.analytics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionProxyTest {

    interface Fluent {
        Fluent withA(Integer x);
        Fluent withB(String y);
        String build();
    }

    static class Delegate {
        Integer a; String b;
        public Delegate withA(Integer x) { a = x; return this; }
        public Delegate withB(String y) { b = y; return this; }
        public String build() { return a + ":" + b; }
    }

    @Test
    void reflectiveProxyCanReturnProxyForFluent() {
        Delegate delegate = new Delegate();
        Fluent proxy = ReflectionUtil.reflectiveProxy(Fluent.class, delegate, true);
        Fluent chained = proxy.withA(7).withB("ok");
        assertSame(proxy, chained);
        assertEquals("7:ok", proxy.build());
    }

    // Only exercise the fluent (returnProxy=true) path which is the intended usage.
}
