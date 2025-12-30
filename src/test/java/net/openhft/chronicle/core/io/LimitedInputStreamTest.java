/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link LimitedInputStream} enforcing size limits and read behaviour semantics.
 *
 * <p>Conventions:
 * <ul>
 *   <li>Each test name states the <em>given-when-then</em> scenario.</li>
 *   <li>No third-party helpers; plain JUnit 5 and core classes keep it "vanilla".</li>
 * </ul>
 */
@SuppressWarnings("deprecation")
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

    @DisplayName("constructor rejects negative size limits values")
    @Test
    void constructor_rejectsNegativeLimit() {
        assertThrows(IllegalArgumentException.class, () -> {
            try (LimitedInputStream in = new LimitedInputStream(bytes(1), -1)) {
                assertEquals(-1, in.read(), "read should return EOF when constructor rejects negative limit");
            }
        }, "constructor should reject negative size limits");
    }

    @DisplayName("single-byte reads consume budget exactly here")
    @Test
    void read_singleBytes_consumesBudgetExactly() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(3), 3)) {
            assertEquals(0, in.read(), "first read should return byte value 0");
            assertEquals(1, in.read(), "second read should return byte value 1");
            assertEquals(2, in.read(), "third read should return byte value 2");
            assertEquals(-1, in.read(), "read should return EOF when budget exhausted");        // true EOF once budget is zero
        }
    }

    @DisplayName("single-byte read throws when budget exhausted")
    @Test
    void read_singleByte_throwsWhenBudgetExhaustedAndDataRemains() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(2), 1)) {
            assertEquals(0, in.read(), "first read should consume entire budget");         // budget used up

            IOException ex = assertThrows(IOException.class, in::read, "read should throw once budget is exhausted");
            assertEquals("Size limit exceeded", ex.getMessage(),
                    "single-byte read should report size limit exceeded");
        }
    }

    @DisplayName("Bulk read within budget returns all bytes limited")
    @Test
    void read_bulkWithinLimit_returnsRequestedBytes() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(10), 10)) {
            byte[] buf = new byte[10];
            int n = in.read(buf, 0, buf.length);

            assertEquals(10, n, "bulk read should return all 10 bytes within budget");
            for (int i = 0; i < 10; i++)
                assertEquals(i, buf[i], "each byte in buffer should match expected sequence value " + i);
            assertEquals(-1, in.read(), "read should return EOF when budget and stream exhausted");        // budget exhausted, underlying EOF
        }
    }

    @DisplayName("bulk read past limit returns then throws")
    @Test
    void read_bulkCrossesLimit_allowedPartReadThenThrows() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(5), 3)) {
            byte[] buf = new byte[5];

            int n = in.read(buf, 0, 5);         // only 3 permitted
            assertEquals(3, n, "bulk read should return only budget-permitted bytes");

            IOException ex = assertThrows(IOException.class,
                    () -> in.read(buf, 0, 1),
                    "bulk read should throw once budget is exceeded");
            assertEquals("Size limit exceeded", ex.getMessage(),
                    "bulk read should report size limit exceeded");
        }
    }

    @DisplayName("zero-length buffer reads return zero length")
    @Test
    void read_zeroLengthBuffer_doesNothingAndReturnsZero() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(1), 1)) {
            byte[] zero = new byte[0];

            assertEquals(0, in.read(zero, 0, 0), "read with zero-length buffer should return zero");
            assertEquals(0, in.read(), "subsequent read should return first byte when budget unchanged");         // budget unchanged
        }
    }

    @DisplayName("zero budget with EOF returns minus one")
    @Test
    void read_budgetZeroAndUnderlyingEOF_returnsMinusOne() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(0), 0)) {
            assertEquals(-1, in.read(), "read should return EOF when budget is zero and stream is empty");
        }
    }
}
