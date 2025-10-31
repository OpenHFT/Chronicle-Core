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
        assertNotNull(analytics);
        // Invocation increments internal counter on mute analytics
        analytics.sendEvent("x", Collections.emptyMap());
        Class<?> muteCls = Class.forName("net.openhft.chronicle.core.internal.analytics.MuteAnalytics");
        Field f = muteCls.getDeclaredField("INSTANCE");
        f.setAccessible(true);
        Object inst = f.get(null);
        Field cnt = muteCls.getDeclaredField("invocationCounter");
        cnt.setAccessible(true);
        int v = (int) cnt.get(inst);
        assertTrue(v >= 1);
    }
}
