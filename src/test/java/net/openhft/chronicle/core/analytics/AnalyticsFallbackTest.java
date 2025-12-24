/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.analytics;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsFallbackTest {

    @Test
    void builderFallsBackWhenLibraryAbsentOrDisabled() throws Exception {
        System.setProperty("chronicle.analytics.disable", "true");
        AnalyticsFacade analytics = AnalyticsFacade.builder("mid", "sec")
                .withFrequencyLimit(1, 1, java.util.concurrent.TimeUnit.HOURS)
                .withReportDespiteJUnit()
                .build();
        assertNotNull(analytics, "required object should not be null");
        // Invocation increments internal counter on mute analytics
        analytics.sendEvent("x", Collections.emptyMap());
        Class<?> muteCls = Class.forName("net.openhft.chronicle.core.internal.analytics.MuteAnalytics");
        Field f = muteCls.getDeclaredField("INSTANCE");
        f.setAccessible(true);
        Object inst = f.get(null);
        Field cnt = muteCls.getDeclaredField("invocationCounter");
        cnt.setAccessible(true);
        int v = (int) cnt.get(inst);
        assertTrue(v >= 1, "invocation count should be >= 1, was " + v);
    }
}
