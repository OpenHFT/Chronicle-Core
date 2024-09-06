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

/**
 * Provides classes and interfaces for handling shutdown procedures in a controlled manner.
 *
 * <p>This package includes classes for registering shutdown hooks with specific priorities, allowing for
 * an orderly shutdown of resources. This is particularly useful in scenarios where resources need to
 * be released or cleaned up in a specific order during the JVM shutdown phase.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 *     // Register a hook to be executed at priority 50.
 *     PriorityHook.add(50, () -> {
 *         // Code to execute during shutdown.
 *     });
 * }
 * </pre>
 *
 * @see net.openhft.chronicle.core.shutdown.Hooklet
 * @see net.openhft.chronicle.core.shutdown.PriorityHook
 */
package net.openhft.chronicle.core.shutdown;
