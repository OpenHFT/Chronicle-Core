package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.Mockito.*;

class ByteConsumerTest {

    @Test
    void acceptShouldPerformOperation() {
        byte[] resultContainer = new byte[1];
        ByteConsumer consumer = value -> resultContainer[0] = value;

        consumer.accept((byte) 10);

        assertEquals((byte) 10, resultContainer[0]);
    }

    @Test
    void andThenShouldPerformBothOperationsInSequence() {
        byte[] resultContainer = new byte[2];
        ByteConsumer firstConsumer = value -> resultContainer[0] = value;
        ByteConsumer secondConsumer = value -> resultContainer[1] = (byte) (value + 10);

        ByteConsumer combinedConsumer = firstConsumer.andThen(secondConsumer);

        combinedConsumer.accept((byte) 5);

        assertEquals((byte) 5, resultContainer[0]);
        assertEquals((byte) 15, resultContainer[1]);
    }

    @Test
    void andThenShouldThrowNullPointerExceptionIfAfterIsNull() {
        ByteConsumer consumer = value -> { /* Do something */ };
        assertThrows(NullPointerException.class, () -> consumer.andThen(null));
    }

    @Test
    void andThenShouldNotPerformSecondOperationIfFirstThrowsException() {
        ByteConsumer firstConsumer = value -> { throw new RuntimeException(); };
        ByteConsumer secondConsumer = mock(ByteConsumer.class);

        ByteConsumer combinedConsumer = firstConsumer.andThen(secondConsumer);

        assertThrows(RuntimeException.class, () -> combinedConsumer.accept((byte) 5));
        verify(secondConsumer, never()).accept(anyByte());
    }
}
