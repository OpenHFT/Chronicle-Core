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
        StackTraceElement[] st = new StackTraceElement[] {
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
