/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class AnalyticsFacadeTest extends CoreTestCommon {

    private static final String TEST_RESPONSE = "A";

    @Before
    public void setSystemProp() {
        System.clearProperty("chronicle.analytics.disable");
    }

    @Test
    public void systemProp() {
        System.setProperty("chronicle.analytics.disable", "true");
        final AnalyticsFacade facade = AnalyticsFacade.builder("measurementId", "apiSecret")
                .withReportDespiteJUnit()
                .build();

        assertTrue(facade instanceof MuteAnalytics);

    }

    @Test
    public void analytics() {
        final AnalyticsFacade.Builder builder = AnalyticsFacade.builder("measurementId", "apiSecret")
                .putEventParameter("e", "1")
                .putUserProperty("u", "2")
                .withClientIdFileName("file_name")
                .withDebugLogger(System.out::println)
                .withErrorLogger(System.err::println)
                .withUrl("url")
                // .withReportDespiteJUnit() Not using this will produce a mute logger in test contexts
                .withFrequencyLimit(1, 1, TimeUnit.SECONDS);

        final AnalyticsFacade analyticsFacade = builder.build();

        // Must be a real one
        assertFalse(analyticsFacade instanceof MuteAnalytics);
    }
}
