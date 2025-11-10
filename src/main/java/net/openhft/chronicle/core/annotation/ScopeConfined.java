//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.*;

/**
 * {@code @ScopeConfined} signals that an object must not escape the annotated
 * scope. It is aimed at tooling and library maintainers dealing with
 * reused objects.
 *
 * <p><b>Retention and effect:</b> Retained at runtime but only meaningful if
 * Chronicle tooling enforces the confinement.</p>
 *
 * <pre>
 * {@code @ScopeConfined}
 * void accept(Buffer b) { }
 * </pre>
 *
 * <p>Object contents will be scrubbed after hand-off. When used with off-heap
 * resources such as {@code Bytes}, the Cleaner may reclaim memory once the
 * callback completes.</p>
 *
 * <pre>
 * void onMessage(@ScopeConfined Bytes&lt;?&gt; b) { ringBuffer.write(b); }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
public @interface ScopeConfined {

    /**
     * Returns a comment as to why this value must meet the condition.
     *
     * @return a comment as to why this value must meet the condition
     */
    String value() default "";
}
