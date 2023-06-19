/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.util.IgnoresEverything;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * NullExceptionHandler is an enumeration implementing the ExceptionHandler and IgnoresEverything interfaces.
 * It serves as a null object for the ExceptionHandler, providing default behavior that essentially does nothing
 * when an exception occurs. This is often used as a safer alternative to null.
 *
 * <ul>
 *     <li>{@link #NOTHING} - An ExceptionHandler that does nothing when an exception occurs and is not enabled for any class.</li>
 * </ul>
 */
public enum NullExceptionHandler implements ExceptionHandler, IgnoresEverything {
    /**
     * The NOTHING instance of this enumeration represents a no-op implementation of the ExceptionHandler.
     */
    NOTHING {
        /**
         * This implementation of {@link ExceptionHandler#on(Logger, String, Throwable)} does nothing.
         *
         * @param logger  the logger instance. Must not be null.
         * @param message a custom message detailing the error, or null.
         * @param thrown  the throwable instance representing the error, or null.
         */
        @Override
        public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
            // Do nothing
        }

        /**
         * This implementation of {@link ExceptionHandler#isEnabled(Class)} always returns false,
         * indicating that this handler is not enabled for any class.
         *
         * @param aClass the class to check if the exception handler is enabled for. Must not be null.
         * @return false, as this handler is not enabled for any class.
         */
        @Override
        public boolean isEnabled(@NotNull Class<?> aClass) {
            return false;
        }
    }
}

