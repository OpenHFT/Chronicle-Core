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

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.io.Closeable;

import java.lang.ref.WeakReference;
import java.util.List;

public interface ReferenceCounted extends Closeable {
    static void releaseAll(List<WeakReference<ReferenceCounted>> refCounts) {
        for (WeakReference<? extends ReferenceCounted> refCountRef : refCounts) {
            if (refCountRef == null)
                continue;
            ReferenceCounted refCounted = refCountRef.get();
            if (refCounted != null) {
                refCounted.release();
            }
        }
    }

    void reserve() throws IllegalStateException;

    void release() throws IllegalStateException;

    static void release(Object o) throws IllegalStateException {
        if (o instanceof ReferenceCounted) {
            ReferenceCounted rc = (ReferenceCounted) o;
            rc.release();
        }
    }

    default void close() {
        release();
    }

    long refCount();

    default boolean tryReserve() {
        try {
            if (refCount() > 0) {
                reserve();
                return true;
            }
        } catch (IllegalStateException ignored) {
        }
        return false;
    }
}