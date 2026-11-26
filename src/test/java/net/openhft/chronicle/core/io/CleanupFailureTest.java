/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;


import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class CleanupFailureTest extends CoreTestCommon {
    @TempDir Path directory;
    private static class TrackedInput extends ByteArrayInputStream {
        int closes;
        TrackedInput() { super(new byte[] { 1, 2, 3 }); }
        @Override public void close() throws IOException { closes++; }
    }
    @Test void readFailureStillClosesInput() {
        TrackedInput in = new TrackedInput() { @Override public synchronized int read(byte[] b, int o, int n) { throw new IllegalStateException("read"); } };
        assertThrows(IllegalStateException.class, () -> IOTools.readAsBytes(in)); assertEquals(1, in.closes);
    }
    @Test void availableFailureStillClosesInput() {
        TrackedInput in = new TrackedInput() { @Override public synchronized int available() { throw new IllegalStateException("available"); } };
        assertThrows(IllegalStateException.class, () -> IOTools.readAsBytes(in)); assertEquals(1, in.closes);
    }
    @Test void emptyInputClosesWithoutReadingPastEnd() throws Exception {
        TrackedInput in = new TrackedInput(); in.skip(3);
        assertArrayEquals(new byte[0], IOTools.readAsBytes(in)); assertEquals(1, in.closes);
    }
    @Test void shortStreamReadsPreserveAllBytes() throws Exception {
        TrackedInput in = new TrackedInput() { @Override public synchronized int read(byte[] b, int o, int n) { return super.read(b, o, Math.min(n, 1)); } };
        assertArrayEquals(new byte[]{1,2,3}, IOTools.readAsBytes(in)); assertEquals(1, in.closes);
    }
    @Test void shortFileReadDoesNotLoseRemainingBytes() throws Exception {
        Path file = directory.resolve("short.bin"); Files.write(file, new byte[]{1,2,3});
        try (FileInputStream in = new FileInputStream(file.toFile()) {
            @Override public int read(byte[] b) throws IOException { return read(b, 0, b.length); }
            @Override public int read(byte[] b, int off, int len) throws IOException { return super.read(b, off, Math.min(1, len)); }
        }) {
            assertArrayEquals(new byte[]{1,2,3}, IOTools.readAsBytes(in));
        }
    }
    @SuppressWarnings("java:S1874") // Java 8 requires this constructor to install a per-URL test handler.
    private URL gzipURL(InputStream in) throws Exception {
        return new URL(null, "test:/broken.gz", new URLStreamHandler() {
            @Override protected URLConnection openConnection(URL u) { return new URLConnection(u) {
                @Override public void connect() { /* In-memory fixture; no network connection is opened. */ }
                @Override public InputStream getInputStream() { return in; }
            }; }
        });
    }
    @Test void fileShrinkingDuringReadReturnsTheAvailablePrefix() throws Exception {
        Path file = directory.resolve("shrinking.bin");
        Files.write(file, new byte[]{1, 2, 3});
        try (FileInputStream in = new FileInputStream(file.toFile()) {
            boolean first = true;
            @Override public int read(byte[] b, int off, int len) throws IOException {
                if (first) {
                    first = false;
                    Files.write(file, new byte[]{1, 2});
                }
                return super.read(b, off, len);
            }
        }) {
            assertArrayEquals(new byte[]{1, 2}, IOTools.readAsBytes(in));
        }
    }
    @Test void sameFailureDuringGzipReadAndCloseIsNotSelfSuppressed() throws Exception {
        RuntimeException failure = new IllegalStateException("shared");
        TrackedInput in = new TrackedInput() {
            @Override public synchronized int read() { throw failure; }
            @Override public void close() { closes++; throw failure; }
        };
        URL url = gzipURL(in);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> IOTools.open(url)));
        assertEquals(0, failure.getSuppressed().length);
        assertEquals(1, in.closes);
    }
    @Test void gzipHeaderFailureRetainsUncheckedCloseFailureAsSuppressed() throws Exception {
        RuntimeException closeFailure = new IllegalStateException("close");
        TrackedInput in = new TrackedInput() { @Override public void close() { closes++; throw closeFailure; } };
        URL url = gzipURL(in);
        IOException failure = assertThrows(IOException.class, () -> IOTools.open(url));
        assertArrayEquals(new Throwable[]{closeFailure}, failure.getSuppressed()); assertEquals(1, in.closes);
    }
    @Test void gzipHeaderFailureRetainsCheckedCloseFailureAsSuppressed() throws Exception {
        IOException closeFailure = new IOException("close");
        TrackedInput in = new TrackedInput() { @Override public void close() throws IOException { closes++; throw closeFailure; } };
        URL url = gzipURL(in);
        IOException failure = assertThrows(IOException.class, () -> IOTools.open(url));
        assertArrayEquals(new Throwable[]{closeFailure}, failure.getSuppressed()); assertEquals(1, in.closes);
    }
    @Test void disconnectFailureDoesNotAbandonLaterResources() throws Exception {
        expectException("Error occurred closing resources");
        HttpURLConnection connection = new HttpURLConnection(java.net.URI.create("http://localhost/").toURL()) {
            @Override public void disconnect() { throw new IllegalStateException("disconnect"); }
            @Override public boolean usingProxy() { return false; }
            @Override public void connect() { /* In-memory fixture; no network connection is opened. */ }
        };
        AtomicInteger closed = new AtomicInteger();
        Closeable.closeQuietly(connection, (AutoCloseable) closed::incrementAndGet);
        assertEquals(1, closed.get());
    }
    @Test void mutatingCollectionDoesNotSkipOtherResources() {
        List<Object> resources = new ArrayList<>(); AtomicInteger closed = new AtomicInteger();
        resources.add((AutoCloseable) resources::clear); resources.add((AutoCloseable) closed::incrementAndGet);
        Closeable.closeQuietly(resources); assertEquals(1, closed.get());
    }
    @Test void nestedReferenceAndArrayReachOwnedResource() {
        AtomicInteger closed = new AtomicInteger(); AutoCloseable c = closed::incrementAndGet;
        Closeable.closeQuietly(new Object[]{new WeakReference<>(new Object[]{c})}); assertEquals(1, closed.get());
    }
    @Test void clearedReferenceDoesNotAbandonLaterResources() {
        WeakReference<Object> ref = new WeakReference<>(new Object()); ref.clear(); AtomicInteger closed = new AtomicInteger();
        Closeable.closeQuietly(ref, (AutoCloseable) closed::incrementAndGet); assertEquals(1, closed.get());
    }
    @Test void closeErrorDoesNotAbandonLaterResources() {
        expectException("Error occurred closing resources"); AtomicInteger closed = new AtomicInteger();
        Closeable.closeQuietly((AutoCloseable) () -> { throw new AssertionError("close"); }, (AutoCloseable) closed::incrementAndGet);
        assertEquals(1, closed.get());
    }
}
