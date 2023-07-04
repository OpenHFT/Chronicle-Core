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
package net.openhft.chronicle.core.util;

import java.util.function.Consumer;

/**
 * Represents an operation that alters an object.
 *
 * <p>Similar to {@link Consumer}, but unlike Consumer, Updater is explicitly
 * expected to perform modifications on its argument.</p>
 *
 * @param <T> the type of the input to the operation
 *
 * <p>Example usage:
 * <pre>
 * Updater&lt;List&lt;String&gt;&gt; appender = list -&gt; list.add("newElement");
 * List&lt;String&gt; myList = new ArrayList&lt;&gt;();
 * appender.update(myList);
 * </pre>
 * </p>
 */
@FunctionalInterface
public interface Updater<T> extends Consumer<T> {

    /**
     * Performs this operation on the given argument, altering it in some way.
     *
     * @param t the input argument
     */
    void update(T t);

    /**
     * This default implementation simply calls the {@code update} method.
     *
     * <p>It is provided to ensure compatibility with the {@link Consumer} interface.</p>
     *
     * @param t the input argument
     */
    @Override
    default void accept(T t) {
        update(t);
    }
}

