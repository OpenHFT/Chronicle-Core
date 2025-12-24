/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.analytics;

import net.openhft.chronicle.core.internal.analytics.MuteBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsFacadeTest {

    private String originalDisableProperty;

    @BeforeEach
    public void captureProperty() {
        originalDisableProperty = System.getProperty("chronicle.analytics.disable");
    }

    @AfterEach
    public void restoreProperty() {
        if (originalDisableProperty == null) {
            System.clearProperty("chronicle.analytics.disable");
        } else {
            System.setProperty("chronicle.analytics.disable", originalDisableProperty);
        }
    }

    @Test
    void enabledWhenAnalyticsPresent() {
        System.clearProperty("chronicle.analytics.disable");
        assertTrue(AnalyticsFacade.isEnabled(), "Analytics should be enabled when dependency is available");

        AnalyticsFacade.Builder builder = AnalyticsFacade.builder("measurement", "secret");
        assertEquals("net.openhft.chronicle.core.internal.analytics.ReflectiveBuilder", builder.getClass().getName(), "builder should be reflective implementation when analytics enabled");
    }

    @Test
    void disabledWhenSystemPropertyExplicit() {
        System.setProperty("chronicle.analytics.disable", "true");
        assertFalse(AnalyticsFacade.isEnabled(), "analytics should be disabled when system property set");

        AnalyticsFacade.Builder builder = AnalyticsFacade.builder("measurement", "secret");
        assertSame(MuteBuilder.INSTANCE, builder, "builder should return same instance (reference equality)");
        assertSame(MuteBuilder.INSTANCE.build(), builder.build(), "built facade should be same mute instance");
    }
}
