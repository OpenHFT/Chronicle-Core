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

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a method as "hot", indicating that it is performance-critical and frequently invoked.
 * It serves as a hint to the developers or tools, such as profilers and optimizers, to prioritize optimization
 * for the annotated method.
 *
 * <p>Methods marked with {@code @HotMethod} are often prime candidates for optimization, such as JIT compilation
 * or caching. The optional {@code value} can provide a custom description or categorization for the method.
 * </p>
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * @HotMethod("Frequently called during event processing")
 * public void processEvent(Event event) {
 *     // Critical logic that must be optimized for performance
 * }
 * }
 * </pre>
 *
 * <p><b>Note:</b> This annotation is primarily for documentation and developer communication purposes.
 * It can also be leveraged by certain profiling or monitoring tools to apply more focused performance tracking.
 * </p>
 *
 * @see org.jetbrains.annotations.NotNull
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HotMethod {

    /**
     * Optional description or categorization for the hot method. This can be used to clarify why the method
     * is considered hot or its importance in the overall application.
     *
     * @return A string description of why the method is marked as hot.
     */
    @NotNull String value() default "";
}
