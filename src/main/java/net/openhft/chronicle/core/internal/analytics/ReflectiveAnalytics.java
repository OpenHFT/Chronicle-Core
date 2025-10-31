/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
