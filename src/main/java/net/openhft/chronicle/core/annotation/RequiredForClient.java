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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that modifications to the annotated class should be made with caution
 * as it is known to be used by a client. This annotation serves as a warning to
 * developers, reminding them of the implications of changes to the annotated element.
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
