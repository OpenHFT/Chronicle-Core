/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.shutdown;

import org.junit.jupiter.api.*;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PriorityHookTest {

    @Test
    public void testAddHook() {
        Runnable hook1 = mock(Runnable.class);
        boolean added1 = PriorityHook.add(1, hook1);
        assertFalse(added1);

        boolean addedAgain = PriorityHook.add(1, hook1);
        assertFalse(addedAgain);
    }

    @Test
    public void testHookExecutionOrder() {
        Runnable hook1 = mock(Runnable.class);
        Runnable hook2 = mock(Runnable.class);
        PriorityHook.add(1, hook1);
        PriorityHook.add(2, hook2);

        PriorityHook.getRegisteredHook().onShutdown();

        InOrder inOrder = inOrder(hook1, hook2);
        inOrder.verify(hook1).run();
        inOrder.verify(hook2).run();
    }

    @Test
    public void testClearHooks() {
        Runnable hook = mock(Runnable.class);
        PriorityHook.add(1, hook);

        PriorityHook.clear();

        assertNull(PriorityHook.getRegisteredHook());
    }
}
