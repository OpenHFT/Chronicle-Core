//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
