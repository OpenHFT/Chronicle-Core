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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker annotation used for methods and constructors in the JSR 292 implementation.
 * <p>
 * Applying this annotation indicates that the method or constructor is a candidate for inlining by the JIT (Just-In-Time) compiler.
 * This may help optimize performance by suggesting the compiler should inline the method where possible, avoiding the overhead of a method call.
 * <p>
 * This annotation is primarily utilized in specific modules such as the Chronicle Enterprise Warmup module, which is part of Chronicle's
 * performance optimization framework.
 * <p>
 * <b>Note:</b> The effectiveness of this annotation depends on the JIT compiler and JVM optimization settings. It serves as a hint and does not guarantee inlining.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * @ForceInline
 * public void criticalMethod() {
 *     // Code that is performance-critical and should be inlined if possible
 * }
 * }
 * </pre>
 *
 * @see Chronicle Enterprise Warmup module for detailed usage and application.
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface ForceInline {
}
