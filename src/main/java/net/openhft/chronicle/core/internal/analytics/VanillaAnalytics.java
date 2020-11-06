package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.Analytics;
import net.openhft.chronicle.core.internal.analytics.ga.GoogleAnalytics;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Adds filtering of unique events per JVM/class-loader
 */
final class VanillaAnalytics implements Analytics {

    private final Analytics delegate;
    private final Set<String> reportedFeatures = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final AtomicBoolean started = new AtomicBoolean();

    public VanillaAnalytics(@NotNull final String libraryName, @NotNull final String version) {
        delegate = new GoogleAnalytics(libraryName, version);
    }

    @Override
    public void onStart() {
        if (started.compareAndSet(false, true)) {
            delegate.onStart();
        }
    }

    @Override
    public void onFeature(@NotNull final String id) {
        if (reportedFeatures.add(id)) {
            delegate.onFeature(id);
        }
    }

}
