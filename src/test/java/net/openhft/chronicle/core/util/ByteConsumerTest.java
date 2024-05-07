package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ByteConsumerTest {

    @Test
    void acceptShouldPerformOperation() {
        byte[] resultContainer = new byte[1];
        ByteConsumer consumer = value -> resultContainer[0] = value;

        consumer.accept((byte) 10);

        assertEquals((byte) 10, resultContainer[0]);
    }
}
