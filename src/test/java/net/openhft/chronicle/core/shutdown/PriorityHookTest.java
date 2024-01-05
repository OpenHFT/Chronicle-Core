package net.openhft.chronicle.core.shutdown;

import org.junit.jupiter.api.*;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PriorityHookTest {

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
        ((InOrder) inOrder).verify(hook1).run();
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
