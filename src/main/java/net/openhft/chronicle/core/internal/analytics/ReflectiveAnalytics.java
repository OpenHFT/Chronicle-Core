/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import net.openhft.chronicle.core.internal.ClassUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

final class ReflectiveAnalytics implements AnalyticsFacade {

    private final Object delegate;

    public ReflectiveAnalytics(@NotNull final Object delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public void sendEvent(@NotNull final String name, @NotNull final Map<String, String> additionalEventParameters) {
        requireNonNull(name);
        requireNonNull(additionalEventParameters);
        try {
            final Method m = delegate.getClass().getMethod("sendEvent", String.class, Map.class);
            // Some runtime analytics implementations live in non-exported/internal packages.
            // Ensure accessibility so invocation succeeds across JDKs.
            ClassUtil.setAccessible(m);
            m.invoke(delegate, name, additionalEventParameters);
        } catch (ReflectiveOperationException e) {
            throw net.openhft.chronicle.core.Jvm.rethrow(e);
        }
    }
}
