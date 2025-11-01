/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @DontChain} indicates that an interface should be ignored by the
 * {@code chronicle-wire} {@code MethodReader} and {@code MethodWriter}
 * utilities. It is intended for library maintainers when excluding ancillary
 * APIs from tooling. Chaining increases reflection cost and can break
 * polymorphic dispatch.
 *
 * <p><b>Retention and effect:</b> Retained at runtime but only acted upon by
 * Chronicle tooling. It has no direct runtime behaviour.</p>
 *
 * <pre>
 * // Chained interface
 * interface Service extends A, B { }
 *
 * // Not chained
 * {@code @DontChain}
 * interface Helper extends A, B { }
 * </pre>

 * <p>See {@link net.openhft.chronicle.core.Jvm#dontChain(Class)} for the
 * programmatic equivalent.</p>
 *
 * @see UsedViaReflection
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DontChain {
}
