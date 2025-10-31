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
/**
 * Cooperative single-thread event processing utilities.
 *
 * <p>An {@link net.openhft.chronicle.core.threads.EventLoop EventLoop} runs each
 * {@link net.openhft.chronicle.core.threads.EventHandler} on its dedicated core
 * thread. Successive calls to {@code action()} occur on the same thread and thus
 * form a happens-before relationship.
 *
 * <p>The {@link net.openhft.chronicle.core.threads.HandlerPriority} enum orders
 * handlers. Some values are aliases &ndash; for example {@code REPLICATION}
 * resolves to {@code MEDIUM}. Implementations use the value returned by
 * {@link net.openhft.chronicle.core.threads.HandlerPriority#alias()}.
 *
 * <p>Affinity can pin the core thread to a specific processor using the
 * Java&nbsp;Thread&nbsp;Affinity library. {@link net.openhft.chronicle.core.threads.CleaningThread}
 * resets the affinity to {@link net.openhft.affinity.AffinityLock#BASE_AFFINITY}
 * after running the task.
 *
 * <p>Parameters and return values are non-null unless annotated with
 * {@link org.jetbrains.annotations.Nullable}.
 *
 * <p> Handlers should perform small units of work and return promptly.
 * <p> Implementations must honour the alias mapping defined by
 * {@code HandlerPriority}.
 * <p> The default loop executes on a {@code CleaningThread} which also
 * clears thread-local state.
 *
 * <pre>{@code
 * EventLoop loop = ...;
 * loop.start();
 * Timer timer = new Timer(loop);
 * java.io.Closeable handle = timer.scheduleAtFixedRate(() -> {
 *     System.out.println("tick");
 *     return false;
 * }, 0, 100);
 * handle.close();
 * loop.stop();
 * loop.close();
 * }</pre>
 */
package net.openhft.chronicle.core.threads;
