/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CloseableTest extends CoreTestCommon {

    @Test
    public void closeQuietlyHandlesNull() {
        CloseableImpl closeable = new CloseableImpl();
        Closeable.closeQuietly(null, null, null, closeable);
        assertTrue(closeable.wasClosed, "closeQuietly should close non-null resource despite null parameters");
    }

    @Test
    public void closeQuietlyCallsCloseOnAutoCloseable() {
        final AutoCloseableImpl autoCloseable = new AutoCloseableImpl();
        Closeable.closeQuietly(autoCloseable);
        assertTrue(autoCloseable.wasClosed, "closeQuietly should invoke close() on AutoCloseable instance");
    }

    @Test
    public void closeQuietlyCallsCloseOnCloseable() {
        final CloseableImpl closeable = new CloseableImpl();
        Closeable.closeQuietly(closeable);
        assertTrue(closeable.wasClosed, "closeQuietly should invoke close() on Closeable instance");
    }

    @Test
    public void closeQuietlyClosesCollections() {
        final List<CloseableImpl> closeables = Arrays.asList(new CloseableImpl(), new CloseableImpl(), new CloseableImpl(), new CloseableImpl());
        Closeable.closeQuietly(closeables);
        for (CloseableImpl closeable : closeables) {
            assertTrue(closeable.wasClosed, "closeQuietly should close all elements in collection");
        }
    }

    @Test
    public void closeQuietlyClosesReferences() {
        final CloseableImpl closeable = new CloseableImpl();
        final SoftReference<CloseableImpl> closeableRef = new SoftReference<>(closeable);
        Closeable.closeQuietly(closeableRef);
        assertTrue(closeable.wasClosed, "closeQuietly should dereference and close SoftReference contents");
    }

    @Test
    public void closeQuietlyClosesRecursively() {
        CloseableImpl closeable1 = new CloseableImpl();
        CloseableImpl closeable2 = new CloseableImpl();
        CloseableImpl closeable3 = new CloseableImpl();
        CloseableImpl closeable4 = new CloseableImpl();
        SoftReference<List<List<CloseableImpl>>> structure =
                new SoftReference<>(Arrays.asList(Arrays.asList(closeable1, closeable2), Arrays.asList(closeable3, closeable4)));
        Closeable.closeQuietly(structure);
        assertTrue(closeable1.wasClosed, "closeQuietly should recursively close first element in nested structure");
        assertTrue(closeable2.wasClosed, "closeQuietly should recursively close second element in nested structure");
        assertTrue(closeable3.wasClosed, "closeQuietly should recursively close third element in nested structure");
        assertTrue(closeable4.wasClosed, "closeQuietly should recursively close fourth element in nested structure");
    }

    @Test
    public void closeQuietlyServerSocketChannel() throws IOException {
        ServerSocketChannel ssc;
        try {
            ssc = ServerSocketChannel.open();
            ssc.bind(new InetSocketAddress(0));
        } catch (IOException ioe) {
            // Some CI environments disallow socket operations; skip in that case.
            Assumptions.assumeTrue(false, "Network not permitted in this environment");
            return;
        }
        ssc.close();
        // can throw an IOException ssc.close();
        Closeable.closeQuietly(ssc);
        assertTrue(true, "execution should reach this point without exception");
    }

    static class CloseableImpl implements Closeable {
        boolean wasClosed = false;

        @Override
        public void close() {
            wasClosed = true;
        }

        @Override
        public boolean isClosed() {
            return wasClosed;
        }
    }

    static class AutoCloseableImpl implements AutoCloseable {
        boolean wasClosed = false;

        @Override
        public void close() {
            wasClosed = true;
        }
    }
}
