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

/**
 * Represents an operation that accepts a an object-valued and {@code short}-valued argument, and returns no result.  This is the
 * {@code (reference, long)} specialization of {@link java.util.function.BiConsumer} for {@code short}.  Unlike most other functional
 * interfaces, {@code ObjShortConsumer} is expected to operate via side-effects.
 * <p>
 * <p>This is a <a href="package-summary.html">functional interface</a> whose functional method is
 * {@link #accept(Object, short)}.
 *
 * @see java.util.function.BiConsumer
 */
@FunctionalInterface
public interface ObjShortConsumer<T> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t     the first input argument
     * @param value the second input argument
     */
    void accept(T t, short value);
}
