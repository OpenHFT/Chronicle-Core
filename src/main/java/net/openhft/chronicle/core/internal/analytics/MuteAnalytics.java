package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.Analytics;
import org.jetbrains.annotations.NotNull;

enum MuteAnalytics implements Analytics {

    INSTANCE;

    @Override
    public void onStart() {}

    @Override
    public void onFeature(@NotNull String id) {}

}