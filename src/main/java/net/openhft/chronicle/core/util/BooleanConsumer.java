/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.openhft.chronicle.core.util;

/**
 * Created by peter.lawrey on 16/01/15.
 */

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents an operation that accepts a single {@code Boolean}-valued argument and returns no result.  This is the
 * primitive type specialization of {@link java.util.function.Consumer} for {@code Boolean}.  Unlike most other functional
 * interfaces, {@code BooleanConsumer} is expected to operate via side-effects.
 * <p>
 * <p>This is a <a href="package-summary.html">functional interface</a> whose functional method is
 * {@link #accept(Boolean)}.
 *
 * @see java.util.function.Consumer
 */
@FunctionalInterface
public interface BooleanConsumer {

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    void accept(Boolean value);

    /**
     * Returns a composed {@code IntConsumer} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed
     * operation.  If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code IntConsumer} that performs in sequence this operation followed by the {@code after}
     * operation
     * @throws NullPointerException if {@code after} is null
     */
    @NotNull
    default BooleanConsumer andThen(@NotNull BooleanConsumer after) {
        Objects.requireNonNull(after);
        return (Boolean t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
