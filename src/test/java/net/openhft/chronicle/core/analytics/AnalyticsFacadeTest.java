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

    @Test
    public void standardBuilderAppliesDefaultsAndDelegates() {
        System.clearProperty("chronicle.analytics.disable");

        AnalyticsFacade.Builder builder = AnalyticsFacade.standardBuilder("measurementId", "apiSecret", "1.2.3");
        builder.putEventParameter("extra-key", "extra-value");
        builder.putUserProperty("role", "test");

        AnalyticsFacade facade = builder
                .withClientIdFileName("client-id")
                .withUrl("https://example.test")
                .withReportDespiteJUnit()
                .build();

        Analytics.RecordingFacade recording = (Analytics.RecordingFacade) facade;
        assertEquals("measurementId", recording.measurementId());
        assertEquals("apiSecret", recording.apiSecret());
        assertEquals(4, recording.frequencyMessages());
        assertEquals(1L, recording.frequencyDuration());
        assertEquals(java.util.concurrent.TimeUnit.HOURS, recording.frequencyUnit());

        Map<String, String> userProps = recording.userProperties();
        assertTrue("Expected user properties to contain timezone", userProps.keySet().stream().anyMatch(k -> k.contains("timezone")));
        assertEquals("test", userProps.get("role"));

        facade.sendEvent("startup", Collections.singletonMap("foo", "bar"));
        Map<String, Map<String, String>> events = recording.emittedEvents();
        assertTrue(events.containsKey("startup"));
        assertEquals("1.2.3", events.get("startup").get("app_version"));
        assertEquals("extra-value", events.get("startup").get("extra-key"));
        assertEquals("bar", events.get("startup").get("foo"));
    }
}
