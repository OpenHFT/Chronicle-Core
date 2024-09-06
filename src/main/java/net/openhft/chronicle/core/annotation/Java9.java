/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
 * Marker annotation to label methods that are intended for use in Java 9 and later environments.
 *
 * <p>This annotation helps in distinguishing methods that are part of code paths specific to Java 9+,
 * which can be useful when maintaining codebases that support both older and newer Java versions.
 *
 * <p>It serves as a hint to developers and tools that these methods may utilize APIs introduced in Java 9,
 * or rely on behavior only available in those versions or later.</p>
 *
 * <p><b>Retention Policy:</b> {@code RetentionPolicy.SOURCE} indicates that this annotation is for
 * documentation and analysis purposes only, and will not be retained in the compiled class files.</p>
 *
 * <p><b>Usage Example:</b>
 * <pre>
 * {@code
 * @Java9
 * public void java9SpecificMethod() {
 *     // Code relying on Java 9+ APIs
 * }
 * }
 * </pre>
 *
 * <p>This annotation does not enforce any checks but serves as a documentation tool within the codebase.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Java9 {
}
