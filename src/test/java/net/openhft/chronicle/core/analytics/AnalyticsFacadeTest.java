/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.analytics;

import net.openhft.chronicle.analytics.Analytics;
import net.openhft.chronicle.core.internal.analytics.MuteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class AnalyticsFacadeTest {

    private String originalDisableProperty;

    @Before
    public void captureProperty() {
        originalDisableProperty = System.getProperty("chronicle.analytics.disable");
    }

    @After
    public void restoreProperty() {
        if (originalDisableProperty == null) {
            System.clearProperty("chronicle.analytics.disable");
        } else {
            System.setProperty("chronicle.analytics.disable", originalDisableProperty);
        }
    }

    @Test
    public void enabledWhenAnalyticsPresent() {
        System.clearProperty("chronicle.analytics.disable");
        assertTrue("Analytics should be enabled when dependency is available", AnalyticsFacade.isEnabled());

        AnalyticsFacade.Builder builder = AnalyticsFacade.builder("measurement", "secret");
        assertEquals("net.openhft.chronicle.core.internal.analytics.ReflectiveBuilder", builder.getClass().getName());
    }

    @Test
    public void disabledWhenSystemPropertyExplicit() {
        System.setProperty("chronicle.analytics.disable", "true");
        assertFalse(AnalyticsFacade.isEnabled());

        AnalyticsFacade.Builder builder = AnalyticsFacade.builder("measurement", "secret");
        assertSame(MuteBuilder.INSTANCE, builder);
        assertSame(MuteBuilder.INSTANCE.build(), builder.build());
    }
}
