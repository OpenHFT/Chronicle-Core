//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @ForceInline} requests that a method or constructor be inlined where
 * possible. It targets library maintainers controlling performance
 * characteristics. The HotSpot JVM ignores custom annotations unless the
 * Chronicle Warmup agent processes the class.
 *
 * <p><b>Retention and effect:</b> Retained at runtime but has no direct effect
 * unless recognised by Chronicle warm-up tooling.</p>
 *
 * <pre>
 * {@code @ForceInline}
 * static long xor(long a, long b) { return a ^ b; }
 * </pre>
 *
 * <p>Large methods or those containing loops may still not inline.</p>
 *
 * @see HotMethod
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface ForceInline {
}
