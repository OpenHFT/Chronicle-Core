package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Mocker;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class AbstractInvocationHandlerTest {
    @Test
    public void testInvoke() {
        final List<String> messages = new ArrayList<>();
        final Consumer<String> consumer = s -> messages.add(s);
        final CallMe mocked = Mocker.intercepting(CallMe.class, "", consumer);
        mocked.method1();
        mocked.method2();
        assertEquals(2, messages.size());
        assertEquals("method1[]", messages.get(0));
        assertEquals("method2[]", messages.get(1));
    }

    @FunctionalInterface
    public interface CallMe {
        void method1();

        default void method2() {
            throw new AssertionError("Don't call me");
        }
    }
}
