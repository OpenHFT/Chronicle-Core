package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Mocker;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;

public class IgnoresEverythingTest {
    @Test
    public void test() {
        assertTrue(Mocker.ignored(Consumer.class) instanceof IgnoresEverything);
    }

    @Test
    public void returnsIgnored() {
        assertTrue(Mocker.ignored(Chained.class).method1() instanceof IgnoresEverything);
    }

    interface Chained {
        Chained2 method1();
    }

    interface Chained2 {
        Object method2();
    }
}