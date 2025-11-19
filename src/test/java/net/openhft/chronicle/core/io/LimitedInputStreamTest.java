/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link LimitedInputStream}.
 *
 * <p>Conventions:
 * <ul>
 *   <li>Each test name states the <em>given-when-then</em> scenario.</li>
 *   <li>No third-party helpers; plain JUnit 5 and core classes keep it "vanilla".</li>
 * </ul>
 */
final class LimitedInputStreamTest {
    /**
     * Returns a new stream filled with {@code length} consecutive ascending bytes.
     */
    private static ByteArrayInputStream bytes(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++)
            data[i] = (byte) i;
        return new ByteArrayInputStream(data);
    }

    @Test
    void constructor_rejectsNegativeLimit() {
        assertThrows(IllegalArgumentException.class, () -> {
            try (LimitedInputStream in = new LimitedInputStream(bytes(1), -1)) {
                assertEquals(-1, in.read());
            }
        });
    }

    @Test
    void read_singleBytes_consumesBudgetExactly() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(3), 3)) {
            assertEquals(0, in.read());
            assertEquals(1, in.read());
            assertEquals(2, in.read());
            assertEquals(-1, in.read());        // true EOF once budget is zero
        }
    }

    @Test
    void read_singleByte_throwsWhenBudgetExhaustedAndDataRemains() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(2), 1)) {
            assertEquals(0, in.read());         // budget used up

            IOException ex = assertThrows(IOException.class, in::read);
            assertEquals("Size limit exceeded", ex.getMessage());
        }
    }

    @Test
    void read_bulkWithinLimit_returnsRequestedBytes() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(10), 10)) {
            byte[] buf = new byte[10];
            int n = in.read(buf, 0, buf.length);

            assertEquals(10, n);
            for (int i = 0; i < 10; i++)
                assertEquals(i, buf[i]);
            assertEquals(-1, in.read());        // budget exhausted, underlying EOF
        }
    }

    @Test
    void read_bulkCrossesLimit_allowedPartReadThenThrows() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(5), 3)) {
            byte[] buf = new byte[5];

            int n = in.read(buf, 0, 5);         // only 3 permitted
            assertEquals(3, n);

            IOException ex = assertThrows(IOException.class,
                    () -> in.read(buf, 0, 1));
            assertEquals("Size limit exceeded", ex.getMessage());
        }
    }

    @Test
    void read_zeroLengthBuffer_doesNothingAndReturnsZero() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(1), 1)) {
            byte[] zero = new byte[0];

            assertEquals(0, in.read(zero, 0, 0));
            assertEquals(0, in.read());         // budget unchanged
        }
    }

    @Test
    void read_budgetZeroAndUnderlyingEOF_returnsMinusOne() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(0), 0)) {
            assertEquals(-1, in.read());
        }
    }
}
