//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.util;

import java.util.function.Consumer;

/**
 * Represents an operation that alters an object.
 *
 * <p>Similar to {@link Consumer}, but unlike Consumer, Updater is explicitly
 * expected to perform modifications on its argument.
 * <p>
 * Example usage:
 * <pre>
 * Updater&lt;List&lt;String&gt;&gt; appender = list -&gt; list.add("newElement");
 * List&lt;String&gt; myList = new ArrayList&lt;&gt;();
 * appender.update(myList);
 * </pre>
 *
 * @param <T> the type of the input to the operation
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
     * <p>It is provided to ensure compatibility with the {@link Consumer} interface.
     *
     * @param t the input argument
     */
    @Override
    default void accept(T t) {
        update(t);
    }
}
