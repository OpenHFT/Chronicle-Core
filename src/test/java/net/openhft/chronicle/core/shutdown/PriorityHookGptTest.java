package net.openhft.chronicle.core.shutdown;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PriorityHookGptTest {

    private List<Integer> callOrder;

    @Before
    public void setup() {
        callOrder = new ArrayList<>();
        PriorityHook.clear(); // Clearing any previously registered hooks.
    }

    @After
    public void tearDown() {
        PriorityHook.clear();
    }

    @Test
    public void testPriorityOrder() {
        PriorityHook.add(10, () -> callOrder.add(10));
        PriorityHook.add(50, () -> callOrder.add(50));
        PriorityHook.add(30, () -> callOrder.add(30));

        PriorityHook priorityHook = PriorityHook.getRegisteredHook();
        assertNotNull(priorityHook);
        priorityHook.onShutdown();

        assertEquals(3, callOrder.size());
        assertEquals(10, (int) callOrder.get(0));
        assertEquals(30, (int) callOrder.get(1));
        assertEquals(50, (int) callOrder.get(2));
    }

    @Test
    public void testAddAndGet() {
        Runnable runnable = () -> {
        };
        Hooklet hooklet1 = PriorityHook.addAndGet(Hooklet.of(20, runnable));
        Hooklet hooklet2 = PriorityHook.addAndGet(Hooklet.of(20, runnable));

        assertEquals(hooklet1, hooklet2);
    }

    @Test
    public void testClear() {
        PriorityHook.add(5, () -> {});
        assertNotNull(PriorityHook.getRegisteredHook());

        PriorityHook.clear();
        assertNull(PriorityHook.getRegisteredHook());
    }
}
