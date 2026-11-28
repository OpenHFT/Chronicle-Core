/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.alloc;

import net.openhft.chronicle.core.UnsafeMemory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AllocationListenerTest {

    @AfterEach
    void resetListener() {
        UnsafeMemory.setAllocationListener(null);
    }

    @Test
    void defaultListenerIsNoOp() {
        assertSame(NoOpAllocationListener.INSTANCE, UnsafeMemory.getAllocationListener());
    }

    @Test
    void setListenerInstallsAndRemoves() {
        AllocationListener custom = new RecordingListener();
        UnsafeMemory.setAllocationListener(custom);
        assertSame(custom, UnsafeMemory.getAllocationListener());

        UnsafeMemory.setAllocationListener(null);
        assertSame(NoOpAllocationListener.INSTANCE, UnsafeMemory.getAllocationListener());
    }

    @Test
    void onAllocateCalledWithCorrectParameters() {
        RecordingListener listener = new RecordingListener();
        UnsafeMemory.setAllocationListener(listener);

        long beforeMs = System.currentTimeMillis();
        long address = UnsafeMemory.INSTANCE.allocate(128);
        long afterMs = System.currentTimeMillis();

        try {
            assertEquals(1, listener.allocations.size());
            RecordedEvent event = listener.allocations.get(0);
            assertTrue(event.timestampMs >= beforeMs && event.timestampMs <= afterMs,
                    "timestampMs should be between before and after");
            assertEquals(address, event.address);
            assertEquals(128, event.size);
            assertNotNull(event.traceElements);
            assertTrue(event.traceDepth > 0, "trace depth should be > 0");
        } finally {
            UnsafeMemory.INSTANCE.freeMemory(address, 128);
        }
    }

    @Test
    void onFreeCalledWithCorrectParameters() {
        RecordingListener listener = new RecordingListener();
        long address = UnsafeMemory.INSTANCE.allocate(256);

        // Install listener after allocate so we only capture the free
        UnsafeMemory.setAllocationListener(listener);
        long beforeMs = System.currentTimeMillis();
        UnsafeMemory.INSTANCE.freeMemory(address, 256);
        long afterMs = System.currentTimeMillis();

        assertEquals(1, listener.frees.size());
        RecordedEvent event = listener.frees.get(0);
        assertTrue(event.timestampMs >= beforeMs && event.timestampMs <= afterMs,
                "timestampMs should be between before and after");
        assertEquals(address, event.address);
        assertEquals(256, event.size);
        assertNotNull(event.traceElements);
        assertTrue(event.traceDepth > 0, "trace depth should be > 0");
    }

    @Test
    void onFreeNotCalledWhenAddressIsZero() {
        RecordingListener listener = new RecordingListener();
        UnsafeMemory.setAllocationListener(listener);

        UnsafeMemory.INSTANCE.freeMemory(0, 64);

        assertTrue(listener.frees.isEmpty(), "onFree should not be called for address 0");
    }

    @Test
    void listenerExceptionIsCaughtAndAllocationSucceeds() {
        AllocationListener throwing = new AllocationListener() {
            @Override
            public void onAllocate(long timestampMs, long address, long capacity, AllocationTrace trace) {
                throw new RuntimeException("test exception");
            }

            @Override
            public void onFree(long timestampMs, long address, long size, AllocationTrace trace) {
                throw new RuntimeException("test exception");
            }
        };
        UnsafeMemory.setAllocationListener(throwing);

        // Should not throw — exception is caught and logged
        long address = UnsafeMemory.INSTANCE.allocate(64);
        assertTrue(address != 0, "allocation should succeed despite listener exception");

        // Free should also not throw
        assertDoesNotThrow(() -> UnsafeMemory.INSTANCE.freeMemory(address, 64));
    }

    @Test
    void allocationTraceIsReusedAcrossCallsOnSameThread() {
        List<AllocationTrace> traces = new ArrayList<>();
        AllocationListener capturing = new AllocationListener() {
            @Override
            public void onAllocate(long timestampMs, long address, long capacity, AllocationTrace trace) {
                traces.add(trace);
            }

            @Override
            public void onFree(long timestampMs, long address, long size, AllocationTrace trace) {
                traces.add(trace);
            }
        };
        UnsafeMemory.setAllocationListener(capturing);

        long addr1 = UnsafeMemory.INSTANCE.allocate(32);
        long addr2 = UnsafeMemory.INSTANCE.allocate(32);
        UnsafeMemory.INSTANCE.freeMemory(addr1, 32);
        UnsafeMemory.INSTANCE.freeMemory(addr2, 32);

        assertEquals(4, traces.size());
        // All traces should be the same object (ThreadLocal reuse)
        AllocationTrace first = traces.get(0);
        for (AllocationTrace t : traces) {
            assertSame(first, t, "AllocationTrace should be reused on the same thread");
        }
    }

    // ---- helpers ----

    private static class RecordedEvent {
        final long timestampMs;
        final long address;
        final long size;
        final StackTraceElement[] traceElements;
        final int traceDepth;

        RecordedEvent(long timestampMs, long address, long size, AllocationTrace trace) {
            this.timestampMs = timestampMs;
            this.address = address;
            this.size = size;
            // Copy since trace is recycled
            this.traceDepth = trace.depth();
            this.traceElements = new StackTraceElement[trace.depth()];
            System.arraycopy(trace.elements(), 0, this.traceElements, 0, trace.depth());
        }
    }

    private static class RecordingListener implements AllocationListener {
        final List<RecordedEvent> allocations = new ArrayList<>();
        final List<RecordedEvent> frees = new ArrayList<>();

        @Override
        public void onAllocate(long timestampMs, long address, long capacity, AllocationTrace trace) {
            allocations.add(new RecordedEvent(timestampMs, address, capacity, trace));
        }

        @Override
        public void onFree(long timestampMs, long address, long size, AllocationTrace trace) {
            frees.add(new RecordedEvent(timestampMs, address, size, trace));
        }
    }
}
