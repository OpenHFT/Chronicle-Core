/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JvmFlagsTest {

    @AfterEach
    void clearProps() {
        System.clearProperty("foo.bar");
        System.clearProperty("foo.baz");
    }

    @DisplayName("getBooleanRespectsDefaultsAndSystemProperty behaviour under expected input and output conditions")
    @Test
    void getBooleanRespectsDefaultsAndSystemProperty() {
        assertFalse(Jvm.getBoolean("foo.bar"), "getBoolean should return false when property is not set and no default is provided");
        assertTrue(Jvm.getBoolean("foo.baz", true), "getBoolean should return the default value when property is not set");

        System.setProperty("foo.bar", "true");
        System.setProperty("foo.baz", "false");
        assertTrue(Jvm.getBoolean("foo.bar"), "getBoolean should return true when property is set to true");
        assertFalse(Jvm.getBoolean("foo.baz", true), "getBoolean should return system property value when set, ignoring default");
    }

    @DisplayName("majorVersionIsSaneAndPausesDoNotThrow behaviour under expected input and output conditions")
    @Test
    void majorVersionIsSaneAndPausesDoNotThrow() {
        int majorVersion = Jvm.majorVersion();
        assertTrue(majorVersion >= 8, "JVM major version should be >= 8 but was " + majorVersion);
        assertDoesNotThrow(Jvm::nanoPause, "nanoPause should not throw for default duration");
        assertDoesNotThrow(() -> Jvm.pause(0), "pause should not throw for zero duration");
        assertDoesNotThrow(() -> Jvm.pause(1), "pause should not throw for small duration");
        assertDoesNotThrow(() -> Jvm.busyWaitMicros(10), "busyWaitMicros should not throw for small duration");
    }
}
