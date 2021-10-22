package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.util.ThreadConfinementAsserter;

class VanillaThreadConfinementAsserter implements ThreadConfinementAsserter {

    private volatile Thread initialThread;

    @Override
    public void assertThreadConfined() {
        final Thread current = Thread.currentThread();
        Thread past = initialThread;
        if (past == null) {
            synchronized (this) {
                if (initialThread == null) {
                    initialThread = current;
                }
            }
            past = current;
        }
        if (past != current) {
            throw new IllegalStateException("Thread " + current + " accessed a thread confined class that was already accessed by thread " + initialThread);
        }
    }

    @Override
    public String toString() {
        return "VanillaThreadConfinementAsserter{" +
                "initialThread=" + initialThread +
                '}';
    }
}
