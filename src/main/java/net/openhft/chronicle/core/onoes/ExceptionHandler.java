/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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

@FunctionalInterface
public interface ExceptionHandler {
    default void on(Class clazz, Throwable thrown) {
        try {
            on(clazz, "", thrown);
        } catch (Throwable t) {
            try {
                Slf4jExceptionHandler.FATAL.on(clazz, "unable to handle the exception so logging to SLF, ", t);
            } catch (Throwable t0) {
                t0.printStackTrace();
            }
        }
    }

    default void on(Class clazz, String message) {
        on(clazz, message, null);
    }

    /**
     * A method to call when an exception occurs. It assumes there is a different handler for different levels.
     *
     * @param clazz   the error is associated with, e.g. the one in which it was caught
     * @param message any message associated with the error, or empty String.
     * @param thrown  any Throwable caught, or null if there was no exception.
     */
    void on(Class clazz, String message, Throwable thrown);

    default boolean isEnabled(Class aClass) {
        return true;
    }
}
