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
                assertEquals(-1, in.read(), "constructor_rejectsNegativeLimit: L38");
            }
        });
    }

    @Test
    void read_singleBytes_consumesBudgetExactly() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(3), 3)) {
            assertEquals(0, in.read(), "read_singleBytes_consumesBudgetExactly: L46");
            assertEquals(1, in.read(), "read_singleBytes_consumesBudgetExactly: L47");
            assertEquals(2, in.read(), "read_singleBytes_consumesBudgetExactly: L48");
            assertEquals(-1, in.read(), "read_singleBytes_consumesBudgetExactly: L49");        // true EOF once budget is zero
        }
    }

    @Test
    void read_singleByte_throwsWhenBudgetExhaustedAndDataRemains() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(2), 1)) {
            assertEquals(0, in.read(), "read_singleByte_throwsWhenBudgetExhaustedAndDataRemains: L56");         // budget used up

            IOException ex = assertThrows(IOException.class, in::read);
            assertEquals("Size limit exceeded", ex.getMessage(), "read_singleByte_throwsWhenBudgetExhaustedAndDataRemains: L59");
        }
    }

    @Test
    void read_bulkWithinLimit_returnsRequestedBytes() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(10), 10)) {
            byte[] buf = new byte[10];
            int n = in.read(buf, 0, buf.length);

            assertEquals(10, n, "read_bulkWithinLimit_returnsRequestedBytes: L69");
            for (int i = 0; i < 10; i++)
                assertEquals(i, buf[i], "read_bulkWithinLimit_returnsRequestedBytes: L71");
            assertEquals(-1, in.read(), "read_bulkWithinLimit_returnsRequestedBytes: L72");        // budget exhausted, underlying EOF
        }
    }

    @Test
    void read_bulkCrossesLimit_allowedPartReadThenThrows() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(5), 3)) {
            byte[] buf = new byte[5];

            int n = in.read(buf, 0, 5);         // only 3 permitted
            assertEquals(3, n, "read_bulkCrossesLimit_allowedPartReadThenThrows: L82");

            IOException ex = assertThrows(IOException.class,
                    () -> in.read(buf, 0, 1));
            assertEquals("Size limit exceeded", ex.getMessage(), "read_bulkCrossesLimit_allowedPartReadThenThrows: L86");
        }
    }

    @Test
    void read_zeroLengthBuffer_doesNothingAndReturnsZero() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(1), 1)) {
            byte[] zero = new byte[0];

            assertEquals(0, in.read(zero, 0, 0), "read_zeroLengthBuffer_doesNothingAndReturnsZero: L95");
            assertEquals(0, in.read(), "read_zeroLengthBuffer_doesNothingAndReturnsZero: L96");         // budget unchanged
        }
    }

    @Test
    void read_budgetZeroAndUnderlyingEOF_returnsMinusOne() throws IOException {
        try (LimitedInputStream in = new LimitedInputStream(bytes(0), 0)) {
            assertEquals(-1, in.read(), "read_budgetZeroAndUnderlyingEOF_returnsMinusOne: L103");
        }
    }
}
