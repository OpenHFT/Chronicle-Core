/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JvmStackTrimTest {

    @DisplayName("isInternal class name classification returns expected internal results")
    @Test
    void isInternalClassNameClassification() {
        assertTrue(Jvm.isInternal("java.lang.String"), "isInternal should classify java.lang classes as internal");
        assertTrue(Jvm.isInternal("sun.nio.fs.UnixFileSystem"), "isInternal should classify sun.* classes as internal");
        assertTrue(Jvm.isInternal("jdk.internal.module.ModuleBootstrap"), "isInternal should classify jdk.internal classes as internal");
        assertFalse(Jvm.isInternal("net.openhft.chronicle.core.Jvm"), "isInternal should classify user application classes as non-internal");
    }

    @DisplayName("trimFirst and trimLast return valid indices")
    @Test
    void trimFirstAndLastIndices() {
        StackTraceElement[] st = {
                new StackTraceElement("java.lang.Object", "m", "Object.java", 1),
                new StackTraceElement("sun.misc.Unsafe", "n", "Unsafe.java", 1),
                new StackTraceElement("net.openhft.User", "x", "User.java", 10),
                new StackTraceElement("jdk.internal.Foo", "y", "Foo.java", 2)
        };
        int first = Jvm.trimFirst(st);
        int last = Jvm.trimLast(first, st);
        // trimFirst returns up to 2 frames of context; here it returns 0
        assertEquals(0, first, "index should be within valid range");
        assertTrue(last >= first && last <= st.length, "range indices should be valid");
    }
}
