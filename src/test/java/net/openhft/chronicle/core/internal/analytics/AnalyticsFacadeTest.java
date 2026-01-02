/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsFacadeTest extends CoreTestCommon {

    private static final String TEST_RESPONSE = "A";

    @BeforeEach
    public void setSystemProp() {
        System.clearProperty("chronicle.analytics.disable");
    }

    @Test
    @DisplayName("System property disables analytics facade builder")
    void systemProp() {
        System.setProperty("chronicle.analytics.disable", "true");
        final AnalyticsFacade facade = AnalyticsFacade.builder("measurementId", "apiSecret")
                .withReportDespiteJUnit()
                .build();

        assertInstanceOf(MuteAnalytics.class, facade, "analytics facade should be mute when analytics is disabled");

    }

    @Test
    @DisplayName("Analytics facade builder creates real implementation")
    void analytics() {
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
        assertFalse(analyticsFacade instanceof MuteAnalytics, "object should not be of specified type");
    }
}
