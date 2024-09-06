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
 * Annotation to signal that the annotated class is critical for a client,
 * meaning modifications to this class should be made with extra caution.
 * This annotation serves as a warning to developers, reminding them of the
 * potential downstream impact of changes to this element, as it is known
 * to be relied upon by external systems or clients.
 *
 * <p>By marking a class with this annotation, developers are alerted that
 * the class is part of a client-facing API or functionality. As such, changes
 * like method signatures, behavior, or even performance optimizations could
 * affect external clients, requiring careful consideration, backward compatibility,
 * or additional communication with the client before proceeding with changes.</p>
 *
 * <p><b>Retention Policy:</b> {@code RetentionPolicy.SOURCE} ensures that this
 * annotation is only available in the source code and not retained in the compiled
 * bytecode. It is used for documentation and development purposes.</p>
 *
 * <p><b>Optional Comment:</b> The {@code value} attribute can be used to provide
 * additional context or details regarding where this class is being used or why it
 * is critical for the client.</p>
 *
 * <p>Example:</p>
 * <pre>
 * {@code
 * @RequiredForClient("Used by critical client integration.")
 * public class ImportantClientClass {
 *     // Be cautious with changes in this class
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.SOURCE)
public @interface RequiredForClient {

    /**
     * Provides an optional comment that offers more details or context about
     * why this class is required for the client or where it is referred to.
     *
     * @return the comment offering additional details
     */
    String value() default "";
}
