/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */

package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.util.IgnoresEverything;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Null-Object implementation of {@link ExceptionHandler} that ignores every event.
 * <p>{@code isEnabled} always returns {@code false}.</p>
 * @see ExceptionHandler
 */
public enum NullExceptionHandler implements ExceptionHandler, IgnoresEverything {
    /**
     * A no-op handler that is always disabled.
     */
    NOTHING {
        /**
         * Ignores the supplied event.
         *
         * @param logger  ignored
         * @param message ignored
         * @param thrown  ignored
         */
        @Override
        public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
            // ignored
        }

        /**
         * Always returns {@code false}.
         *
         * @param aClass the class being checked
         * @return {@code false}
         */
        @Override
        public boolean isEnabled(@NotNull Class<?> aClass) {
            return false;
        }
    }
}
