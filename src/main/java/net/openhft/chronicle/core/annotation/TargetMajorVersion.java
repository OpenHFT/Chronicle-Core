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
 * Annotation to specify the target major version for the annotated type.
 * This annotation allows you to define the intended major version compatibility
 * of a class or component, including optional support for older and newer versions.
 *
 * <p>When applying this annotation, you can specify:</p>
 * <ul>
 *     <li>A specific target major version to indicate the primary version the code is compatible with.</li>
 *     <li>Whether the code should also be compatible with versions older or newer than the target version.</li>
 *     <li>Setting the version to {@code ANY_VERSION} allows compatibility across all versions.</li>
 * </ul>
 *
 * <p>Use cases include ensuring that a class or interface is only loaded or used
 * when the JVM is running a compatible version, or for enforcing version-specific
 * features in libraries or applications.</p>
 *
 * <p><b>Retention Policy:</b> {@code RetentionPolicy.RUNTIME} ensures that this
 * annotation is available at runtime for reflective inspection.</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * @TargetMajorVersion(majorVersion = 11, includeNewer = true)
 * public class Java11CompatibleClass {
 *     // Implementation for Java 11 and later
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TargetMajorVersion {

    /**
     * Constant representing compatibility with any major version.
     * <p>Set this value to indicate that the annotated class or interface
     * is compatible with any JVM major version.</p>
     */
    int ANY_VERSION = 0;

    /**
     * Specifies the target major version that the annotated type is designed for.
     * <p>By default, this is set to {@code ANY_VERSION}, indicating compatibility
     * with all versions. Set this to a specific value to target a particular version.</p>
     *
     * @return the target major version number (e.g., 8 for Java 8, 11 for Java 11)
     */
    int majorVersion() default ANY_VERSION;

    /**
     * Indicates if the annotated type is compatible with versions older than the specified
     * {@link #majorVersion()}.
     * <p>If set to {@code true}, the type will be compatible with versions older than the
     * specified version, allowing backward compatibility.</p>
     *
     * @return {@code true} if compatibility with older versions is desired; {@code false} otherwise
     */
    boolean includeOlder() default false;

    /**
     * Indicates if the annotated type is compatible with versions newer than the specified
     * {@link #majorVersion()}.
     * <p>If set to {@code true}, the type will be compatible with versions newer than the
     * specified version, allowing forward compatibility.</p>
     *
     * @return {@code true} if compatibility with newer versions is desired; {@code false} otherwise
     */
    boolean includeNewer() default false;
}
