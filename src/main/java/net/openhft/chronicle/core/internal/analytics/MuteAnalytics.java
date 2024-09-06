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

import java.util.Map;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * A mute implementation of the {@link AnalyticsFacade} interface that ignores all analytics events.
 * <p>
 * This singleton enum is used in scenarios where analytics tracking is disabled, and thus
 * all event submissions are no-ops. The class is primarily useful for testing and ensuring that
 * analytics calls do not perform any actions when analytics are muted.
 * </p>
 * <p>
 * The class also tracks the number of times the {@link #sendEvent(String, Map)} method is invoked,
 * which can be useful for testing purposes.
 * </p>
 */
enum MuteAnalytics implements AnalyticsFacade {

    INSTANCE;  // Singleton instance for mute analytics

    // Counter to track how many times the sendEvent method has been called (for testing purposes).
    int invocationCounter;

    /**
     * Mute implementation of the sendEvent method. This method ignores the provided event name
     * and parameters, and only increments the invocation counter for testing purposes.
     *
     * @param name                    The name of the event being sent (ignored).
     * @param additionalEventParameters A map of additional parameters for the event (ignored).
     */
    @Override
    public void sendEvent(@NotNull String name, @NotNull Map<String, String> additionalEventParameters) {
        requireNonNull(name);
        requireNonNull(additionalEventParameters);

        // Ignore the event, but increment the invocation counter for testing purposes
        invocationCounter++;
    }
}
