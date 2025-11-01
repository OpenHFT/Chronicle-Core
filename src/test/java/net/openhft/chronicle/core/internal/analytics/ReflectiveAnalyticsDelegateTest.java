/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ReflectiveAnalyticsDelegateTest {

    private static final class FakeAnalytics {
        final Map<String, Map<String, String>> recorded = new LinkedHashMap<>();

        public void sendEvent(String name, Map<String, String> additional) {
            recorded.put(name, new LinkedHashMap<>(additional));
        }
    }

    @Test
    public void delegatesSendEventToUnderlyingTarget() {
        FakeAnalytics fake = new FakeAnalytics();
        AnalyticsFacade facade = new ReflectiveAnalytics(fake);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("alpha", "1");
        params.put("beta", "2");

        facade.sendEvent("startup", params);

        assertTrue(fake.recorded.containsKey("startup"));
        assertEquals("1", fake.recorded.get("startup").get("alpha"));
        assertEquals("2", fake.recorded.get("startup").get("beta"));
    }
}

