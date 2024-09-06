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
 * Annotation to represent a specific feature in the Chronicle software stack.
 *
 * <p>This annotation can be applied to types (classes or interfaces) to associate them with a
 * particular feature by specifying a feature code using the {@code value} attribute.
 *
 * <p>The retention policy is set to {@link RetentionPolicy#CLASS}, meaning the annotation will be
 * stored in the class file but not available at runtime.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * @ChronicleFeature(1)
 * public class SomeClass {
 *     // Implementation details
 * }
 * }
 * </pre>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface ChronicleFeature {

    /**
     * Represents the unique feature code associated with the annotated type.
     *
     * @return The feature code as an integer.
     */
    int value();
}
