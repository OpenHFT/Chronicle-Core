/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class NotNullIntrumentationTargetTest extends CoreTestCommon {

    @Test
    public void notNull() {
        test("a");
        assertTrue(true);
    }

    @Test(expected = NullPointerException.class)
    @Ignore("Awaiting https://github.com/osundblad/intellij-annotations-instrumenter-maven-plugin/issues/53. " +
            "When compiled by IntelliJ this class is instrumented with null checks but it will throw an IllegalArgumentExceptioj not NPE!")
    public void Null() {
        test(null);
    }

    @SuppressWarnings("EmptyMethod")
    private static void test(@NotNull String nn) {
        // This should throw an NPE if called with a null argument
    }
}
