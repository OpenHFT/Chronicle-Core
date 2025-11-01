/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        // Only meaningful when the real analytics dependency is on the classpath
        Assume.assumeTrue(ReflectionUtil.analyticsPresent());

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

            // If running against the test shim (RecordingFacade), verify via its in-memory store
            try {
                final java.lang.reflect.Method emitted = analyticsFacade.getClass().getMethod("emittedEvents");
                // Shim found; exercise shim path
                analyticsFacade.sendEvent("test");
                @SuppressWarnings("unchecked")
                final java.util.Map<String, java.util.Map<String, String>> events =
                        (java.util.Map<String, java.util.Map<String, String>>) emitted.invoke(analyticsFacade);
                assertTrue(events.containsKey("test"));
                return;
            } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException ignoreShim) {
                // Not the shim; fall through to real http path
            }

            analyticsFacade.sendEvent("test");

            long deadline = System.currentTimeMillis() + 9_000L;
            while (debugResponses.stream().noneMatch(TEST_RESPONSE::equals) && System.currentTimeMillis() < deadline) {
                Thread.yield();
            }

            assertTrue(errorResponses.isEmpty());
            assertTrue(debugResponses.stream().anyMatch(TEST_RESPONSE::equals));
        } finally {
            server.shutdown();
            new File(clientIdFileName).delete();
        }
    }
}
