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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@code @RequiredForClient} warns that the annotated class name is baked into
 * on-wire YAML/JSON or a class-alias pool. Its audience is library maintainers
 * reviewing potential breaking changes.
 *
 * <p><b>Retention and effect:</b> Kept in the source only. There is no runtime
 * effect unless build tooling checks for it.</p>
 *
 * <pre>
 * {@code @RequiredForClient("chronicle-queue-dump tool")}
 * public class ApiEntry { }
 * </pre>
 *
 * <p>Renaming or deleting such classes can break clients and should follow
 * semantic-versioning rules.</p>
 */
@Retention(RetentionPolicy.SOURCE)
public @interface RequiredForClient {

    /**
     * Specifies an optional comment to provide additional context or information
     * about where this class is referred to.
     *
     * @return the comment providing more details
     */
    String value() default "";
}
