/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
        assertFalse(Jvm.getBoolean("foo.bar"), "getBoolean should return false when property is not set and no default is provided");
        assertTrue(Jvm.getBoolean("foo.baz", true), "getBoolean should return the default value when property is not set");

        System.setProperty("foo.bar", "true");
        System.setProperty("foo.baz", "false");
        assertTrue(Jvm.getBoolean("foo.bar"), "getBoolean should return true when property is set to true");
        assertFalse(Jvm.getBoolean("foo.baz", true), "getBoolean should return system property value when set, ignoring default");
    }

    @Test
    void majorVersionIsSaneAndPausesDoNotThrow() {
        assertTrue(Jvm.majorVersion() >= 8, "JVM major version should be at least 8");
        assertDoesNotThrow(Jvm::nanoPause);
        assertDoesNotThrow(() -> Jvm.pause(0));
        assertDoesNotThrow(() -> Jvm.pause(1));
        assertDoesNotThrow(() -> Jvm.busyWaitMicros(10));
    }
}

