/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JvmStackTrimTest {

    @Test
    void isInternalClassNameClassification() {
        assertTrue(Jvm.isInternal("java.lang.String"), "isInternalClassNameClassification: L14");
        assertTrue(Jvm.isInternal("sun.nio.fs.UnixFileSystem"), "isInternalClassNameClassification: L15");
        assertTrue(Jvm.isInternal("jdk.internal.module.ModuleBootstrap"), "isInternalClassNameClassification: L16");
        assertFalse(Jvm.isInternal("net.openhft.chronicle.core.Jvm"), "isInternalClassNameClassification: L17");
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
        assertEquals(0, first, "trimFirstAndLastIndices: L31");
        assertTrue(last >= first && last <= st.length, "trimFirstAndLastIndices: L32");
    }
}
