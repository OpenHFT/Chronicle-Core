/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

public abstract class AbstractCloseable implements Closeable, ReferenceOwner {
    private static final long CLOSED_OFFSET;
    static Map<AbstractCloseable, StackTrace> CLOSEABLE_STACK_TRACE_MAP = Collections.synchronizedMap(new WeakHashMap<>());

    static {
        CLOSED_OFFSET = UNSAFE.objectFieldOffset(Jvm.getField(AbstractCloseable.class, "closed"));
    }

    private transient volatile int closed = 0;
    private transient volatile StackTrace createdHere;
    private transient volatile StackTrace closedHere;

    protected AbstractCloseable() {
        createdHere = Jvm.isResourceTracing() ? new StackTrace("Created Here") : null;

        Map<AbstractCloseable, StackTrace> map = CLOSEABLE_STACK_TRACE_MAP;
        if (map != null)
            map.put(this, new StackTrace("Created here"));
    }

    public static void enableCloseableTracing() {
        CLOSEABLE_STACK_TRACE_MAP = Collections.synchronizedMap(new WeakHashMap<>());
    }

    public static void disableCloseableTracing() {
        CLOSEABLE_STACK_TRACE_MAP = null;
    }

    public static void assertCloseablesClosed() {
        Map<AbstractCloseable, StackTrace> traceMap = CLOSEABLE_STACK_TRACE_MAP;
        if (traceMap == null) {
            Jvm.warn().on(AbstractCloseable.class, "closable tracing disabled");
            return;
        }
        AssertionError openFiles = new AssertionError("Closeables still open");
        synchronized (traceMap) {
            for (Map.Entry<AbstractCloseable, StackTrace> entry : traceMap.entrySet()) {
                if (!entry.getKey().isClosed())
                    openFiles.addSuppressed(entry.getValue());
                Closeable.closeQuietly(entry.getKey());
            }
        }
        if (openFiles.getSuppressed().length > 0)
            throw openFiles;
    }

    /**
     * Close a resource so it cannot be used again.
     */
    @Override
    public final void close() {
        if (UNSAFE.getAndSetInt(this, CLOSED_OFFSET, 1) != 0) {
            return;
        }
        closedHere = Jvm.isResourceTracing() ? new StackTrace("Closed here") : null;
        performClose();
    }

    /**
     * Called when a resources needs to be open to use it.
     *
     * @throws IllegalStateException if closed
     */
    public void throwExceptionIfClosed() throws IllegalStateException {
        if (isClosed())
            throw new IllegalStateException("Closed", closedHere);
    }

    /**
     * Called from finalise() implementations.
     */
    protected void warnIfNotClosed() {
        if (!isClosed()) {
            if (Jvm.isResourceTracing())
                Jvm.warn().on(getClass(), "Discarded without closing", new IllegalStateException(createdHere));
            close();
        }
    }

    /**
     * Call close() to ensure this is called exactly once.
     */
    protected abstract void performClose();

    @Override
    public boolean isClosed() {
        return closed != 0;
    }
}
