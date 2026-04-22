/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @Address} declares that the annotated value represents a native
 * memory address or base pointer. It improves API semantics by distinguishing
 * raw addresses from sizes, indexes, and object-relative offsets.
 *
 * <p><b>Retention and effect:</b> Stored in the class file but ignored by the
 * runtime unless Chronicle tooling checks it.</p>
 *
 * <p>This annotation is semantic only. Use it alongside explicit range guards
 * such as {@code MemoryAegis.assertAddressRange(...)} when a concrete access
 * width or slice length is known.</p>
 *
 * <pre>{@code
 * @Address long address;
 *
 * int readInt(@Address long address) {
 *     assert SKIP_ASSERTIONS || MemoryAegis.assertAddressRange(address, Integer.BYTES);
 *     return ...;
 * }
 * }</pre>
 *
 * @see NonNegative
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
public @interface Address {

    /**
     * Specifies an optional comment to provide additional context or rationale
     * for why the annotated element represents a native address.
     *
     * @return the comment explaining why this address annotation is necessary
     */
    String value() default "";
}
