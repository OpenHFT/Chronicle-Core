//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
