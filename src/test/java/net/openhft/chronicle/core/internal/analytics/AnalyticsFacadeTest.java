/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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

    @Test(timeout = 10_000L)
    public void analyticsWithRealWebServer() throws IOException {

        final String clientIdFileName = "client_id_file_name.txt";

        // this makes sure the client id file is pre-created
        final AnalyticsFacade dummyFacade = AnalyticsFacade.builder("measurementId", "apiSecret")
                .withClientIdFileName(clientIdFileName)
                .build();

        final List<String> debugResponses = new CopyOnWriteArrayList<>();
        final List<String> errorResponses = new CopyOnWriteArrayList<>();

        final MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(TEST_RESPONSE));

        server.start();
        try {
            final HttpUrl url = server.url("mp/collect");

            final AnalyticsFacade.Builder builder = AnalyticsFacade.builder("measurementId", "apiSecret")
                    .putEventParameter("e", "1")
                    .putUserProperty("u", "2")
                    .withClientIdFileName(clientIdFileName)
                    .withDebugLogger(debugResponses::add)
                    .withErrorLogger(errorResponses::add)
                    .withUrl(url.url().toString())
                    .withReportDespiteJUnit() // Run the real thing even though we are in test mode.
                    .withFrequencyLimit(1, 1, TimeUnit.SECONDS);

            final AnalyticsFacade analyticsFacade = builder.build();

            analyticsFacade.sendEvent("test");

            while (debugResponses.stream().noneMatch(TEST_RESPONSE::equals)) {
                // Await reporting thread
            }

            assertTrue(errorResponses.isEmpty());
            assertTrue(debugResponses.stream().anyMatch(TEST_RESPONSE::equals));
        } finally {
            server.shutdown();
            new File(clientIdFileName).delete();
        }
    }
}