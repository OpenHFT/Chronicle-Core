package net.openhft.chronicle.core.analytics;

import org.junit.Test;

public class AnalyticsTest {

    @Test
    public void analytics() {
        final Analytics analytics = Analytics.acquire("map", "3.20.84-SNAPSHOT");
        analytics.onStart();
    }

}