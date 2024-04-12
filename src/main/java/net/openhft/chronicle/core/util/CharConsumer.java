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
package net.openhft.chronicle.core.util;

/**
 * Represents an operation that accepts a single {@code char}-valued argument and returns no result.  This is the
 * primitive type specialization of {@link java.util.function.Consumer} for {@code char}.  Unlike most other functional
 * interfaces, {@code CharConsumer} is expected to operate via side effects.
 * <p>This is a <a href="package-summary.html">functional interface</a> whose functional method is {@link
 * #accept(char)}.
 *
 * @see java.util.function.Consumer
 * @since 1.8
 */
@FunctionalInterface
public interface CharConsumer {

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    void accept(char value);

}
