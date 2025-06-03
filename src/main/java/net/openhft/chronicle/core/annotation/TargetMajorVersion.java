/*
 * Copyright 2016-2025 chronicle.software
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
 * {@code @TargetMajorVersion} specifies which Java major version a type targets.
 * It is useful for library maintainers managing backwards compatibility.
 *
 * <p><b>Retention and effect:</b> Retained at runtime but has no behaviour
 * unless tooling inspects it.</p>
 *
 * <pre>
 * {@code @TargetMajorVersion(11)}
 * class ModernOnly { }
 * </pre>
 *
 * <p>Major version {@code 0} means any version. Common values are 8, 11, 17
 * and 21.</p>
 *
 * <pre>
 * Class&lt;?&gt; impl = ImplementationPicker.pick(TargetInterface.class);
 * </pre>
 *
 * <p>When multiple classes match, {@code includeOlder} and
 * {@code includeNewer} determine precedence.</p>
 *
 * @see Java9
 * @see ChronicleFeature
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TargetMajorVersion {

    /**
     * Constant to represent compatibility with any version.
     */
    int ANY_VERSION = 0;

    /**
     * Specifies the major version number that the annotated type targets.
     *
     * @return the target major version number
     */
    int majorVersion() default ANY_VERSION;

    /**
     * If set to true, indicates that the annotated type should be compatible
     * with versions older than the specified major version.
     *
     * @return true if compatibility with older versions is intended
     */
    boolean includeOlder() default false;

    /**
     * If set to true, indicates that the annotated type should be compatible
     * with versions newer than the specified major version.
     *
     * @return true if compatibility with newer versions is intended
     */
    boolean includeNewer() default false;
}
