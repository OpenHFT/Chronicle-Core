/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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

package net.openhft.chronicle.core.annotation;

import java.lang.annotation.*;

/**
 * Indicates that the value, value consumer, value mapper etc. should not divulge the value outside
 * the annotated scope. The meaning value should also be construed to include any and all descendant Object properties
 * recursively from the original value.
 * <p>
 * This annotation can be used for entities that are dealing with reused-objects.
 * The term divulge obviously includes making the value or any of its descendant objects available to another Thread.
 *
 * @see ThreadConfined
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
public @interface ScopeConfined {

    /**
     * Returns a comment as to why this value must meet the condition.
     *
     * @return a comment as to why this value must meet the condition
     */
    String value() default "";
}