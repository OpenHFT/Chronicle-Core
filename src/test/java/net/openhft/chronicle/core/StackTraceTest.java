package net.openhft.chronicle.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StackTraceTest {

    static void thinking() {
        Jvm.pause(1000);
    }

    @Test
    public void forThread() {
        Thread t = new Thread(StackTraceTest::thinking, "background");
        t.start();
        Jvm.pause(50);
        StackTrace st = StackTrace.forThread(t);
        t.interrupt();
        assertEquals("Thread[background,5,main] on main", st.getMessage());
        assertEquals("net.openhft.chronicle.core.Jvm.pause", st.getStackTrace()[0].toString().split("\\(")[0]);
    }
}