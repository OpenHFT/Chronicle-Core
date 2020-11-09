package net.openhft.chronicle.core.analytics;

import org.junit.Test;

import java.io.IOException;

public class AnalyticsTest {

    @Test
    public void analytics() throws IOException {
        final Analytics analytics = Analytics.acquire("map", "3.20.84-SNAPSHOT");
        analytics.onStart();

        System.out.println("Press enter to stop");
        System.in.read();

    }

}