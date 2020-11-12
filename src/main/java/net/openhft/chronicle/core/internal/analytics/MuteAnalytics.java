package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.Analytics;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

enum MuteAnalytics implements Analytics {

    INSTANCE;

    @Override
    public void onFeature(@NotNull String id, @NotNull Map<String, String> eventParameters) {}
}