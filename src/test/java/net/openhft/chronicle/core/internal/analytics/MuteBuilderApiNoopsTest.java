//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class MuteBuilderApiNoopsTest {

    private String prev;

    @Before
    public void disableAnalytics() {
        prev = System.getProperty("chronicle.analytics.disable");
        System.setProperty("chronicle.analytics.disable", "true");
    }

    @After
    public void restoreProperty() {
        if (prev == null) System.clearProperty("chronicle.analytics.disable");
        else System.setProperty("chronicle.analytics.disable", prev);
    }

    @Test
    public void builderMethodsAreNoOpsAndBuildsMuteAnalytics() {
        AtomicBoolean called = new AtomicBoolean(false);
        AnalyticsFacade.Builder b = AnalyticsFacade.builder("mid", "sec")
                .putUserProperty("k1", "v1")
                .putEventParameter("e1", "v2")
                .withFrequencyLimit(1, 1, TimeUnit.SECONDS)
                .withClientIdFileName("cid")
                .withUrl("http://example.invalid")
                .withErrorLogger(s -> called.set(true))
                .withDebugLogger(s -> {})
                .withReportDespiteJUnit();

        AnalyticsFacade facade = b.build();
        assertTrue(facade instanceof MuteAnalytics);

        // Should not throw
        facade.sendEvent("startup");
        // Error logger not called in the mute path
        assertFalse(called.get());
    }
}

