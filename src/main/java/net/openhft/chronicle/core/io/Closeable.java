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

package net.openhft.chronicle.core.io;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

@FunctionalInterface
public interface Closeable extends java.io.Closeable {
    static void closeQuietly(Object o) {
        if (o instanceof java.io.Closeable) {
            try {
                ((java.io.Closeable) o).close();
            } catch (ClosedChannelException ignore) {
            } catch (IOException ignored) {
            }

        }
    }

    /**
     * Doesn't throw a checked exception.
     */
    void close();

    default void notifyClosing() {
        // take an action before everything else closes.
    }
}
