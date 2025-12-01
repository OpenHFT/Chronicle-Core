/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @SingleThreaded} documents that a class is not thread-safe and should
 * be used by one thread only. Library maintainers can employ it to flag
 * non-concurrent designs.
 *
 * <p><b>Retention and effect:</b> Retained at runtime but imposes no automatic
 * restrictions unless Chronicle tooling validates it.</p>
 *
 * <pre>
 * {@code @SingleThreaded}
 * final class IdGenerator { }
 * </pre>
 *
 * <p>Chronicle runtime checks this contract unless
 * <p>
 * is enabled. Call {@code singleThreadedCheckReset()} before handing the
 * instance to another thread. Violations may lead to subtle data races.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SingleThreaded {
}
