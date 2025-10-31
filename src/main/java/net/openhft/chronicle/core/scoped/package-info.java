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
 * Provides classes for short-lived, stack-based object pooling.
 *
 * <p>The main entry point is {@link net.openhft.chronicle.core.scoped.ScopedThreadLocal}
 * which maintains a per-thread stack of objects. When {@link net.openhft.chronicle.core.scoped.ScopedThreadLocal#get()}
 * is called a {@link net.openhft.chronicle.core.scoped.ScopedResource} is returned
 * that must be closed. The resource is then pushed back on to the owning thread's
 * stack. If the stack is full the newest object is discarded.
 *
 * <p>Resources acquired from a pool belong to the thread that obtained them and
 * must not be passed to another thread while in scope.
 *
 * <p>Typical usage leverages the try-with-resources statement:
 * <pre>{@code
 * ScopedThreadLocal<StringBuilder> pool = new ScopedThreadLocal<>(StringBuilder::new, 4);
 *
 * try (ScopedResource<StringBuilder> handle = pool.get()) {
 *     StringBuilder sb = handle.get();
 *     // use the StringBuilder
 * }
 * }</pre>
 */
package net.openhft.chronicle.core.scoped;
