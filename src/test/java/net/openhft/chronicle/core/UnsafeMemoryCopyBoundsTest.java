/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class UnsafeMemoryCopyBoundsTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void checksActualCopiesWithAndWithoutAssertions(boolean assertions) throws Exception {
        // A missing guard must fail a bounded child process, not corrupt the owning test JVM.
        Process process = JavaProcessBuilder.create(CopyBoundsProbe.class)
                .withJvmArguments(assertions ? "-ea" : "-da")
                .withProgramArguments(Boolean.toString(assertions))
                .start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "copy probe did not terminate");
            String output = new String(IOTools.readAsBytes(process.getInputStream()), StandardCharsets.UTF_8)
                    + new String(IOTools.readAsBytes(process.getErrorStream()), StandardCharsets.UTF_8);
            assertEquals(0, process.exitValue(), output);
        } finally {
            try {
                if (process.isAlive()) {
                    process.destroyForcibly();
                    assertTrue(process.waitFor(10, TimeUnit.SECONDS), "copy probe did not stop");
                }
            } finally {
                Closeable.closeQuietly(process.getInputStream(), process.getErrorStream(), process.getOutputStream());
            }
        }
    }

    public static final class CopyBoundsProbe {
        public static void main(String[] args) {
            assertEquals(Boolean.parseBoolean(args[0]), UnsafeMemory.class.desiredAssertionStatus());
            verify(new UnsafeMemory());
            verify(new UnsafeMemory.ARMMemory());
        }

        private static void verify(UnsafeMemory memory) {
            long address = memory.allocate(64);
            try {
                byte[] bytes = new byte[16];
                Arrays.fill(bytes, (byte) 37);
                memory.setMemory(address, 64, (byte) 90);
                byte[] nativeBefore = new byte[64];
                memory.readBytes(address, nativeBefore, 0, nativeBefore.length);
                byte[] arrayBefore = bytes.clone();

                int[][] invalid = {{-1, 0}, {0, -1}, {-1, 1}, {17, 0}, {16, 1}, {0, 17},
                        {Integer.MIN_VALUE, 0}, {Integer.MAX_VALUE, 1}, {Integer.MAX_VALUE, Integer.MAX_VALUE}};
                for (int[] range : invalid) {
                    assertThrows(IllegalArgumentException.class,
                            () -> memory.writeBytes(address, bytes, range[0], range[1]), Arrays.toString(range));
                    assertThrows(IllegalArgumentException.class,
                            () -> memory.readBytes(address, bytes, range[0], range[1]), Arrays.toString(range));
                }
                for (long offset : new long[]{Long.MIN_VALUE, Long.MAX_VALUE, 1L << 32}) {
                    assertThrows(IllegalArgumentException.class, () -> memory.readBytes(address, bytes, offset, 1));
                }
                assertThrows(NullPointerException.class, () -> memory.writeBytes(address, null, 0, 0));
                assertThrows(NullPointerException.class, () -> memory.readBytes(address, null, 0, 0));
                byte[] nativeAfter = new byte[64];
                memory.readBytes(address, nativeAfter, 0, nativeAfter.length);
                assertArrayEquals(nativeBefore, nativeAfter, "rejected slices changed native memory");
                assertArrayEquals(arrayBefore, bytes, "rejected slices changed the array");

                memory.writeBytes(address, bytes, bytes.length, 0);
                memory.readBytes(address, bytes, bytes.length, 0);
                memory.writeBytes(address, new byte[0], 0, 0);
                memory.readBytes(address, new byte[0], 0, 0);
                assertArrayEquals(arrayBefore, bytes);
                memory.readBytes(address, nativeAfter, 0, nativeAfter.length);
                assertArrayEquals(nativeBefore, nativeAfter, "empty copies changed native memory");

                // Reproducible slices include exact-end boundaries and check bytes on both sides of every copy.
                Random random = new Random(846);
                for (int iteration = 0; iteration < 128; iteration++) {
                    random.nextBytes(bytes);
                    int length = iteration % 17;
                    int offset = iteration < 17 ? bytes.length - length : random.nextInt(bytes.length - length + 1);
                    memory.setMemory(address, 64, (byte) 90);
                    memory.writeBytes(address + 8, bytes, offset, length);
                    byte[] expectedNative = new byte[64];
                    Arrays.fill(expectedNative, (byte) 90);
                    System.arraycopy(bytes, offset, expectedNative, 8, length);
                    memory.readBytes(address, nativeAfter, 0, nativeAfter.length);
                    assertArrayEquals(expectedNative, nativeAfter, "write slice " + iteration);

                    byte[] result = new byte[16];
                    Arrays.fill(result, (byte) 91);
                    memory.readBytes(address + 8, result, offset, length);
                    byte[] expectedArray = new byte[16];
                    Arrays.fill(expectedArray, (byte) 91);
                    System.arraycopy(bytes, offset, expectedArray, offset, length);
                    assertArrayEquals(expectedArray, result, "read slice " + iteration);
                }
            } finally {
                memory.freeMemory(address, 64);
            }
        }
    }
}
