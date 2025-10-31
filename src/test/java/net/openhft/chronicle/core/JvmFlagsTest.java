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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JvmFlagsTest {

    @AfterEach
    void clearProps() {
        System.clearProperty("foo.bar");
        System.clearProperty("foo.baz");
    }

    @Test
    void getBooleanRespectsDefaultsAndSystemProperty() {
        assertFalse(Jvm.getBoolean("foo.bar"));
        assertTrue(Jvm.getBoolean("foo.baz", true));

        System.setProperty("foo.bar", "true");
        System.setProperty("foo.baz", "false");
        assertTrue(Jvm.getBoolean("foo.bar"));
        assertFalse(Jvm.getBoolean("foo.baz", true));
    }

    @Test
    void majorVersionIsSaneAndPausesDoNotThrow() {
        assertTrue(Jvm.majorVersion() >= 8);
        assertDoesNotThrow(Jvm::nanoPause);
        assertDoesNotThrow(() -> Jvm.pause(0));
        assertDoesNotThrow(() -> Jvm.pause(1));
        assertDoesNotThrow(() -> Jvm.busyWaitMicros(10));
    }
}

