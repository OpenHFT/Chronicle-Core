/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;

public class IgnoresEverythingTest extends CoreTestCommon {
    @Test
    public void test() {
        assertTrue(Mocker.ignored(Consumer.class) instanceof IgnoresEverything);
    }

    @Test
    public void returnsIgnored() {
        assertTrue(Mocker.ignored(Chained.class).method1() instanceof IgnoresEverything);
    }

    interface Chained {
        Chained2 method1();
    }

    interface Chained2 {
        @SuppressWarnings("unused")
        Object method2();
    }
}
