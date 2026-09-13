/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.provider.Arguments;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class UnsafeMemoryOrderingLifecycleTest {
    @Test
    void interruptedOwnerJoinsWorkersBeforeFreeingNativeMemory() throws InterruptedException {
        runControl("interrupt");
    }

    @Test
    void partialStartupJoinsTheStartedWorkerBeforeFreeingNativeMemory() throws InterruptedException {
        runControl("second-start");
    }

    private static void runControl(String mode) throws InterruptedException {
        Process process = JavaProcessBuilder.create(LifecycleMain.class).withProgramArguments(mode).start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Native-memory ownership control timed out: " + mode);
            assertEquals(0, process.exitValue(), JavaProcessBuilder.getProcessStdErr(process));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            process.destroyForcibly();
            boolean interrupted = Thread.interrupted();
            while (process.isAlive()) {
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    public static final class LifecycleMain {
        public static void main(String[] args) throws InterruptedException {
            final boolean partialStart = "second-start".equals(args[0]);
            final Thread owner = Thread.currentThread();
            final CountDownLatch started = new CountDownLatch(partialStart ? 1 : 2);
            final AssertionError startFailure = new AssertionError("second worker did not start");
            final UnsafeMemoryTestMixin.Variant variant = new UnsafeMemoryTestMixin.Variant(
                    Arguments.of("lifecycle control", new UnsafeMemory.ARMMemory(), UnsafeMemoryTestMixin.Mode.NATIVE_ADDRESS));
            variant.memory().writeInt(variant.addr(), 42);
            final Runnable accessMemory = () -> {
                started.countDown();
                while (!Thread.currentThread().isInterrupted()) {
                    variant.memory().readVolatileInt(variant.addr());
                    Thread.yield();
                }
            };
            final Thread writer = new Thread(accessMemory, "lifecycle-writer");
            final Thread reader = partialStart ? new Thread(accessMemory, "lifecycle-reader") {
                @Override
                public synchronized void start() {
                    try {
                        assertTrue(started.await(5, TimeUnit.SECONDS));
                    } catch (InterruptedException e) {
                        throw new AssertionError(e);
                    }
                    throw startFailure;
                }
            } : new Thread(accessMemory, "lifecycle-reader");
            final Thread interrupter = new Thread(() -> {
                try {
                    assertTrue(started.await(5, TimeUnit.SECONDS));
                    owner.interrupt();
                } catch (InterruptedException e) {
                    throw new AssertionError(e);
                }
            }, "lifecycle-interrupter");
            try {
                if (!partialStart)
                    interrupter.start();
                final Executable attempt = () -> {
                    try (UnsafeMemoryTestMixin.OrderingWorkers workers = new UnsafeMemoryTestMixin.OrderingWorkers(writer, reader)) {
                        workers.startAndWait();
                    }
                };
                if (partialStart)
                    assertSame(startFailure, assertThrows(AssertionError.class, attempt));
                else
                    assertThrows(InterruptedException.class, attempt);
                assertEquals(!partialStart, Thread.currentThread().isInterrupted(), "Restore owner interruption after joining");
                assertFalse(writer.isAlive(), "Writer can still access native memory");
                assertFalse(reader.isAlive(), "Reader can still access native memory");
            } finally {
                // A broken ownership scope must fail only this bounded subprocess.
                if (writer.isAlive() || reader.isAlive()) {
                    System.err.println("Refusing to free memory while an ordering worker is alive");
                    Runtime.getRuntime().halt(2);
                }
                variant.close();
                Thread.interrupted();
                interrupter.join();
            }
        }
    }
}
