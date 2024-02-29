package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.Test;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class ThreadLocalHelperTest {

    @Test
    void testGetTLWithSupplier() {
        ThreadLocal<WeakReference<String>> threadLocal = new ThreadLocal<>();
        AtomicInteger counter = new AtomicInteger(0);
        String value = ThreadLocalHelper.getTL(threadLocal, () -> "Value" + counter.incrementAndGet());

        assertEquals("Value1", value);
        // Ensure the same value is retrieved and not recreated
        assertEquals("Value1", ThreadLocalHelper.getTL(threadLocal, () -> "Value" + counter.incrementAndGet()));
    }

    @Test
    void testGetSTL() {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        AtomicInteger counter = new AtomicInteger(0);
        String value = ThreadLocalHelper.getSTL(threadLocal, () -> "Value" + counter.incrementAndGet());

        assertEquals("Value1", value);
        // Ensure the same value is retrieved and not recreated
        assertEquals("Value1", ThreadLocalHelper.getSTL(threadLocal, () -> "Value" + counter.incrementAndGet()));
    }

    @Test
    void testGetTLWithFunction() {
        ThreadLocal<WeakReference<Integer>> threadLocal = new ThreadLocal<>();
        String input = "123";
        Integer value = ThreadLocalHelper.getTL(threadLocal, input, Integer::valueOf);

        assertEquals(123, value);
        // Ensure the same value is retrieved and not recreated
        assertEquals(123, ThreadLocalHelper.getTL(threadLocal, "456", Integer::valueOf));
    }
}
