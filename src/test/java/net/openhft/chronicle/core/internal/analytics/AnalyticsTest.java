package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.analytics.Analytics;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsTest {

    @Test
    public void analytics() throws IOException {
        // final Analytics analytics = Analytics.acquire("map", "3.20.84-SNAPSHOT");
        final Analytics analytics = new VanillaAnalytics("map", "3.20.84-SNAPSHOT");

        final Map<String, String> eventParameters = new HashMap<>();
        eventParameters.put("test_parameter", "a");

        analytics.onStart(eventParameters);

/*        System.out.println("Press enter to stop");
        System.in.read();*/

        // Give the http thread time to complete
        Jvm.pause(2000);

    }

}