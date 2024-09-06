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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Indicates that the annotated element (method, field, constructor, or class)
 * has intentionally been given package-local visibility, meaning it is only
 * accessible within its own package.
 *
 * <p>This annotation is primarily used for documentation purposes to inform
 * developers that the package-local visibility is a deliberate design choice
 * to restrict access, rather than an oversight. By marking elements with this
 * annotation, it signals that no accessor methods or wider visibility (e.g.,
 * public or protected) are required or intended for this element.
 *
 * <p><b>Retention Policy:</b> {@code RetentionPolicy.SOURCE} ensures that this
 * annotation is only retained in the source code and is not included in the
 * compiled bytecode. It is used purely for documentation and tooling purposes.</p>
 *
 * <p><b>Usage Example:</b>
 * <pre>
 * {@code
 * @PackageLocal
 * class InternalClass {
 *     @PackageLocal
 *     void packageLocalMethod() {
 *         // Method intentionally restricted to package scope
 *     }
 * }
 * }
 * </pre>
 *
 * <p>Note: This annotation does not alter the actual visibility of the element.
 * It merely serves to document the intent behind the chosen visibility.</p>
 */
@Documented
@Retention(SOURCE)
@Target({METHOD, FIELD, CONSTRUCTOR, TYPE})
public @interface PackageLocal {
}
