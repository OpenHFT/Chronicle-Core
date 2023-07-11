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
 * Annotation to specify the target major version for the annotated type. It allows
 * defining a range of major versions including, excluding, or specific to the provided
 * version.
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
