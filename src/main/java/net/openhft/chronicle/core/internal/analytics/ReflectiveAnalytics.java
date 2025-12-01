/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * {@link AnalyticsFacade} implementation that delegates to the optional analytics module via reflection.
 * <p>
 * Avoids a hard dependency on {@code chronicle-analytics} while still allowing events to be sent
 * when the module is present on the classpath.
 */
final class ReflectiveAnalytics implements AnalyticsFacade {

    private static final String CLASS_NAME = "net.openhft.chronicle.analytics.Analytics";

    private final Object delegate;

    /**
     * Wrap a concrete analytics delegate discovered at runtime.
     *
     * @param delegate instance of {@code chronicle-analytics} facade
     */
    public ReflectiveAnalytics(@NotNull final Object delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public void sendEvent(@NotNull final String name, @NotNull final Map<String, String> additionalEventParameters) {
        requireNonNull(name);
        requireNonNull(additionalEventParameters);
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "sendEvent", String.class, Map.class);
        ReflectionUtil.invokeOrThrow(m, delegate, name, additionalEventParameters);
    }
}
