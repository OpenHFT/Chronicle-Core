/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * {@link AnalyticsFacade} implementation that discards all events.
 *
 * <p>Used when analytics reporting is disabled so that call sites can still invoke the
 * {@link AnalyticsFacade} API without incurring network or disk traffic. The
 * {@link #invocationCounter} is incremented on each call to {@link #sendEvent(String, Map)}
 * to support testing and diagnostics.
 */
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
