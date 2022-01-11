package net.openhft.chronicle.core.util;

import net.openhft.affinity.Affinity;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.values.IntArrayValues;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.security.SecureRandom;

public class ThreadIndexAssigner {
    private final IntArrayValues values;

    public ThreadIndexAssigner(IntArrayValues values) {
        this.values = values;
    }

    public int getId() throws IllegalStateException, BufferOverflowException, BufferUnderflowException {
        int threadId = Affinity.getThreadId();
        int size = (int) values.getCapacity();
        values.setMaxUsed(size);
        // already assigned?
        for (int i = 0; i < size; i++) {
            int value = values.getVolatileValueAt(i);
            if (value == threadId)
                return i;
        }

        int index = nextIndex(size);
        for (int i = 0; i < size * 2; i++) {
            int value = values.getVolatileValueAt(index);
            boolean processAlive = Jvm.isProcessAlive(value);
            if ((value == 0 || !processAlive) && (values.compareAndSet(index, value, threadId)))
                    return index;
            index++;
            if (index >= size) {
                Thread.yield();
                index = 0;
            }
        }
        throw new IllegalStateException("Unable to acquire an id as all ids are taken");
    }

    protected int nextIndex(int size) {
        return new SecureRandom().nextInt(size);
    }
}
