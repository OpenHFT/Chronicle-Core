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

package net.openhft.chronicle.core;

/**
 * A specialized {@link Runnable} interface that provides a default method to perform an action after the
 * static initialization of the {@link Jvm} class.
 *
 * This interface is intended for use with classes that need to run some initialization code after the
 * {@code Jvm} class has completed its static initialization. Implementing classes can override the
 * {@link #postInit()} method to provide custom post-initialization logic.
 */
public interface ChronicleInitRunnable extends Runnable {
    /**
     * This method is called once at the end of the static initialization of the {@link Jvm} class.
     *
     * By default, this method does nothing (no-op). Implementing classes can override this method
     * to execute any necessary post-initialization tasks after the {@code Jvm} class has been initialized.
     */
    default void postInit() {
        // No-op (No operation by default)
    }
}
