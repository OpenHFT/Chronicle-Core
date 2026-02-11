/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.alloc;

/**
 * Pooled, reusable stack-trace container. One instance per thread via ThreadLocal.
 * <p>
 * Listeners must read or copy data before returning — the object is recycled
 * on the next allocation from the same thread.
 * <p>
 * Plain class with no Wire dependency; Chronicle Wire serialises it via
 * reflection-based field access.
 */
public class AllocationTrace {
    private StackTraceElement[] elements;
    private int depth;

    /**
     * Captures the current stack trace, reusing the backing array where possible.
     */
    public void capture() {
        Throwable t = new Throwable();
        StackTraceElement[] raw = t.getStackTrace();
        if (elements == null || elements.length < raw.length) {
            elements = new StackTraceElement[raw.length];
        }
        System.arraycopy(raw, 0, elements, 0, raw.length);
        depth = raw.length;
    }

    public StackTraceElement[] elements() { return elements; }

    public int depth() { return depth; }
}
