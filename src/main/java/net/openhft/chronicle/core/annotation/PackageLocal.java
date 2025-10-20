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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * {@code @PackageLocal} marks an element as intentionally package scoped. The
 * audience is library maintainers documenting visibility choices.
 *
 * <p><b>Retention and effect:</b> Retained only in the source and discarded by
 * the compiler. There is no runtime effect unless tooling inspects the source.</p>
 *
 * <pre>
 * {@code @PackageLocal} class Helper { }
 * </pre>
 *
 * <p>Package-private access avoids {@code SecurityManager} checks and can
 * enable sealed hierarchies. Use this annotation to silence IDE warnings such
 * as "access can be private".</p>
 *
 * @see UsedViaReflection
 */
@Documented
@Retention(SOURCE)
@Target({METHOD, FIELD, CONSTRUCTOR, TYPE})
public @interface PackageLocal {
}
