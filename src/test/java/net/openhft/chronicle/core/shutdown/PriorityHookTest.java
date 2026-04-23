/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.shutdown;

import net.openhft.chronicle.core.test.RecordingRunnable;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriorityHookTest {

    @AfterEach
    void tearDown() {
        PriorityHook.clear();
    }

    @Test
    void testAddHook() {
        RecordingRunnable hook1 = new RecordingRunnable();
        boolean added1 = PriorityHook.add(1, hook1);
        assertFalse(added1);

        boolean addedAgain = PriorityHook.add(1, hook1);
        assertFalse(addedAgain);
    }

    @Test
    void testHookExecutionOrder() {
        List<String> calls = new ArrayList<>();
        Runnable hook1 = () -> calls.add("hook1");
        Runnable hook2 = () -> calls.add("hook2");
        PriorityHook.add(1, hook1);
        PriorityHook.add(2, hook2);

        PriorityHook.getRegisteredHook().onShutdown();

        assertEquals(List.of("hook1", "hook2"), calls);
    }

    @Test
    void testClearHooks() {
        RecordingRunnable hook = new RecordingRunnable();
        PriorityHook.add(1, hook);

        PriorityHook.clear();

        assertNull(PriorityHook.getRegisteredHook());
    }
}
