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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @Java9} marks a method that is only used when running on Java 9 or
 * later. It is primarily for tooling authors and library maintainers. IDEs may
 * safely delete imports that appear unused when this annotation is present.
 *
 * <p><b>Retention and effect:</b> Present only in the source and discarded by
 * the compiler. It has no runtime effect unless recognised by build tooling.</p>
 *
 * {@code @Java9}
 * void newApiCall();
 *
 * <p>Use {@code Jvm.isJava9Plus()} to guard such calls at runtime.</p>
 *
 * @see TargetMajorVersion
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Java9 {
}
