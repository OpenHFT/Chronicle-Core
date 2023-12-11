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

package net.openhft.chronicle.core.annotation;

import java.lang.annotation.*;

/**
 * Indicates that the value consumer, value mapper, structure etc. should not make the object available outside
 * the annotated scope. The meaning of "the object" should also be construed to include any and all descendant
 * non-immutable Object properties recursively dereferenced from the original object.
 * <p>
 * <em>
 * In other words, object contents would be purposely overwritten after they are passed to the receiver and
 * consequently cannot be kept after the invocation.
 * </em>
 * <p>
 * This annotation can be used for entities that are dealing with reused-objects.
 * The term make available also includes making the object or any of its descendant objects available to another Thread.
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
