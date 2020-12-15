package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

enum MuteAnalytics implements AnalyticsFacade {

    INSTANCE;

    // Used for testing only
    int invocationCounter;

    @Override
    public void sendEvent(@NotNull String name, @NotNull Map<String, String> additionalEventParameters) {
        requireNonNull(name);
        requireNonNull(additionalEventParameters);

        // Ignore the call as this instance is mute
        invocationCounter++;
    }
}