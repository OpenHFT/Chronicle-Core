/*
 * Copyright 2016-2025 chronicle.software
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
 * {@code @UsedViaReflection} notes that an element is accessed through
 * reflection or tests. It warns library maintainers against removing or hiding
 * the member.
 *
 * <p><b>Retention and effect:</b> Retained in the class file and ignored at
 * runtime unless tooling looks for it.</p>
 *
 * <pre>
 * {@code @UsedViaReflection}
 * public void setAccessible() { }
 * </pre>
 *
 * @see PackageLocal
 */
@Documented
@Retention(CLASS)
@Target({METHOD, FIELD, CONSTRUCTOR, TYPE})
public @interface UsedViaReflection {
}
