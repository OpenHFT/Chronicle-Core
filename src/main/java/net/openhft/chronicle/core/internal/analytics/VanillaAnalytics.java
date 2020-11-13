package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.Analytics;
import net.openhft.chronicle.core.internal.analytics.google.GoogleAnalytics;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adds filtering of events per JVM/class-loader
 */
final class VanillaAnalytics implements Analytics {

    private static final boolean DISABLE_FILTERING = true;

    private final Analytics delegate;
    private final Set<Long> reportedFeatures = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public VanillaAnalytics(@NotNull final String libraryName, @NotNull final String version) {
        delegate = new GoogleAnalytics(libraryName, version);
    }

    @Override
    public void onFeature(@NotNull final String id, @NotNull final Map<String, String> eventParameters) {
        final long hash = 31L * id.hashCode() + eventParameters.hashCode();
        if (DISABLE_FILTERING || reportedFeatures.add(hash)) {
            delegate.onFeature(id, eventParameters);
        }
    }

}