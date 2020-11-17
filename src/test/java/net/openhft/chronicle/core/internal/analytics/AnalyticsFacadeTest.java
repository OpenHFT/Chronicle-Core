package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnalyticsFacadeTest {

    private static final String TEST_RESPONSE = "A";

    @Test
    public void analytics() {
        final AnalyticsFacade.Builder builder = AnalyticsFacade.builder("measurementId", "apiSecret")
                .putEventParameter("e", "1")
                .putUserProperty("u","2")
                .withClientIdFileName("file_name")
                .withDebugLogger(System.out::println)
                .withErrorLogger(System.err::println)
                .withUrl("url")
                // .withReportDespiteJUnit() Not using this will produce a mute logger in test contexts
                .withFrequencyLimit(1, TimeUnit.SECONDS);

        final AnalyticsFacade analyticsFacade = builder.build();

        analyticsFacade.sendEvent("test");
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
                    .putUserProperty("u","2")
                    .withClientIdFileName(clientIdFileName)
                    .withDebugLogger(debugResponses::add)
                    .withErrorLogger(errorResponses::add)
                    .withUrl(url.url().toString())
                    .withReportDespiteJUnit() // Run the real thing even though we are in test mode.
                    .withFrequencyLimit(1, TimeUnit.SECONDS);

            final AnalyticsFacade analyticsFacade = builder.build();

            analyticsFacade.sendEvent("test");

            while (debugResponses.stream().noneMatch(TEST_RESPONSE::equals)) {
                // Await reporting thread
            }

            System.out.println("debugResponses = " + debugResponses);

            assertTrue(errorResponses.isEmpty());
            assertTrue(debugResponses.stream().anyMatch(TEST_RESPONSE::equals));
        } finally {
            server.shutdown();
            new File(clientIdFileName).delete();
        }
    }
}