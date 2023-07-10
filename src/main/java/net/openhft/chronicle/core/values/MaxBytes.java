/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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
