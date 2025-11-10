//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.values;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation is used to specify the maximum size, in encoded bytes,
 * that a variable-length data type can occupy. The unit of measurement
 * may be either in elements or bytes.
 *
 * <p>The annotation can be applied to method parameters that accept variable-length data
 * types (e.g. arrays, collections, strings) to enforce constraints on the amount of
 * memory they can use.
 *
 * <p>This can be particularly useful in scenarios where memory usage needs to be
 * tightly controlled or when interacting with external systems that impose
 * data size limits.
 */
@Target(PARAMETER)
@Retention(RUNTIME)
@Documented
public @interface MaxBytes {

    /**
     * Specifies the maximum number of encoded bytes allowed for the variable-length data type.
     *
     * <p>By default, the value is set to 64.
     *
     * @return the maximum number of encoded bytes
     */
    int value() default 64;
}
