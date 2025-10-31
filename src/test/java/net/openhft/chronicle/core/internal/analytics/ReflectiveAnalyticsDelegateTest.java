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

