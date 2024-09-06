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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Annotation to indicate that the annotated element (method, field, constructor, or class)
 * is accessed dynamically through reflection. This annotation serves as a marker to inform
 * developers that the annotated member, although not directly referenced in the source code,
 * should not be removed or modified, as it is used indirectly, typically by frameworks or tools.
 *
 * <p>This annotation is useful in cases where reflection is employed to access fields or methods
 * at runtime, or when public visibility is necessary for certain test frameworks.</p>
 *
 * <p><b>Retention Policy:</b> {@code RetentionPolicy.CLASS} ensures that this annotation is retained
 * in the bytecode but is not available at runtime. Its primary purpose is for documentation and to
 * prevent accidental refactoring.</p>
 *
 * <p><b>Use cases include:</b></p>
 * <ul>
 *     <li>Marking private fields or methods that are accessed by serialization libraries or frameworks like
 *     Jackson, Gson, or Hibernate.</li>
 *     <li>Highlighting methods or constructors that are accessed by test frameworks or reflection-based tools.</li>
 *     <li>Informing future developers that an element is dynamically utilized, even though it may not appear
 *     to be directly referenced in the source code.</li>
 * </ul>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * public class ReflectionExample {
 *
 *     @UsedViaReflection
 *     private String secretField;
 *
 *     @UsedViaReflection
 *     public ReflectionExample() {
 *         // Constructor accessed via reflection
 *     }
 *
 *     @UsedViaReflection
 *     public String getSecretField() {
 *         return secretField;
 *     }
 * }
 * }
 * </pre>
 */
@Documented
@Retention(CLASS)
@Target({METHOD, FIELD, CONSTRUCTOR, TYPE})
public @interface UsedViaReflection {
}
