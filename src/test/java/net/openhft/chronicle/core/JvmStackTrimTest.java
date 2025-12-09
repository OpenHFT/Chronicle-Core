/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JvmStackTrimTest {

    @Test
    void isInternalClassNameClassification() {
        assertTrue(Jvm.isInternal("java.lang.String"));
        assertTrue(Jvm.isInternal("sun.nio.fs.UnixFileSystem"));
        assertTrue(Jvm.isInternal("jdk.internal.module.ModuleBootstrap"));
        assertFalse(Jvm.isInternal("net.openhft.chronicle.core.Jvm"));
    }

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
        assertEquals(0, first);
        assertTrue(last >= first && last <= st.length);
    }
}
