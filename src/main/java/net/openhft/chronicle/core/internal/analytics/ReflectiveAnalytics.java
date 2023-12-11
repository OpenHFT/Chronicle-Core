/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

final class ReflectiveAnalytics implements AnalyticsFacade {

    private static final String CLASS_NAME = "net.openhft.chronicle.analytics.Analytics";

    private final Object delegate;

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
