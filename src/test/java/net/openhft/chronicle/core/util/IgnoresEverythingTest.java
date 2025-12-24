/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IgnoresEverythingTest extends CoreTestCommon {
    @Test
    void test() {
        assertInstanceOf(IgnoresEverything.class, Mocker.ignored(Consumer.class), "Mocker.ignored should return an IgnoresEverything instance for Consumer interface");
    }

    @Test
    void returnsIgnored() {
        assertInstanceOf(IgnoresEverything.class, Mocker.ignored(Chained.class).method1(), "chained method call should return IgnoresEverything instance when invoked on ignored mock");
    }

    interface Chained {
        Chained2 method1();
    }

    interface Chained2 {
        @SuppressWarnings("unused")
        Object method2();
    }
}
