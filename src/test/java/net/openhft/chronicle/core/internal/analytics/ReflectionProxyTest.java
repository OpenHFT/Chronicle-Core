/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ReflectionProxyTest {

    @Test
    void reflectiveProxyCanReturnProxyForFluent() {
        Delegate delegate = new Delegate();
        Fluent proxy = ReflectionUtil.reflectiveProxy(Fluent.class, delegate, true);
        Fluent chained = proxy.withA(7).withB("ok");
        assertSame(proxy, chained, "should return same instance (reference equality)");
        assertEquals("7:ok", proxy.build(), "proxy should build correct value from chained method calls");
    }

    interface Fluent {
        Fluent withA(Integer x);

        Fluent withB(String y);

        String build();
    }

    static class Delegate {
        Integer a;
        String b;

        public Delegate withA(Integer x) {
            a = x;
            return this;
        }

        public Delegate withB(String y) {
            b = y;
            return this;
        }

        public String build() {
            return a + ":" + b;
        }
    }

    // Only exercise the fluent (returnProxy=true) path which is the intended usage.
}
