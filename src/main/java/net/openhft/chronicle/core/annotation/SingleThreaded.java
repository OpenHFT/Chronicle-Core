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
 * Annotation to indicate that a class is designed to be used in a single-threaded context.
 * <p>
 * Classes or components marked with this annotation are not thread-safe and should only
 * be accessed by a single thread at any given time. Attempting to access such a class
 * from multiple threads concurrently may result in undefined behavior, data races, or
 * inconsistencies.
 * </p>
 *
 * <p>By marking a class as {@code @SingleThreaded}, developers are clearly conveying
 * the expectation that the class's methods and state are intended to be managed
 * within a single thread. As such, any effort to synchronize access or use the class
 * in multi-threaded scenarios should be avoided.</p>
 *
 * <p><b>Retention Policy:</b> {@code RetentionPolicy.RUNTIME} ensures that this
 * annotation is retained at runtime and can be accessed via reflection.</p>
 *
 * <p><b>Use Cases:</b></p>
 * <ul>
 *     <li>Low-latency systems where thread-safety mechanisms (like locks) are deliberately avoided to maximize performance.</li>
 *     <li>Components in real-time or embedded systems where multi-threaded behavior is not expected or required.</li>
 *     <li>Development environments where components are known to be single-threaded but could be mistakenly used in concurrent scenarios.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @SingleThreaded
 * public class SingleThreadOnlyResource {
 *     // Class implementation
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SingleThreaded {
}
