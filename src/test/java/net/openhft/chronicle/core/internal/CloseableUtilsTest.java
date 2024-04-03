package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.stubbing.OngoingStubbing;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.io.IOException;
import java.lang.ref.Reference;
import java.net.HttpURLConnection;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import static net.openhft.chronicle.core.internal.Bootstrap.uncheckedCast;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.*;

public class CloseableUtilsTest {
    private Closeable mockCloseable;
    private Collection<Closeable> mockCloseables;
    private ServerSocketChannel mockServerSocketChannel;
    private AutoCloseable mockAutoCloseable;
    private Reference<?> mockReference;
    private HttpURLConnection mockHttpURLConnection;

    @Before
    public void mockitoNotSupportedOnJava21() {
        assumeTrue(Jvm.majorVersion() <= 17);
    }

    @Before
    public void setUp() {
        mockitoNotSupportedOnJava21();
        mockCloseable = mock(Closeable.class);
        CloseableUtils.enableCloseableTracing();
        mockCloseables = uncheckedCast(mock(Collection.class));
        mockServerSocketChannel = mock(ServerSocketChannel.class);
        mockAutoCloseable = mock(AutoCloseable.class);
        mockReference = mock(Reference.class);
        mockHttpURLConnection = mock(HttpURLConnection.class);
    }

    @After
    public void tearDown() {
        CloseableUtils.disableCloseableTracing();
    }

    @Test
    public void testAdd() {
        CloseableUtils.add(mockCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertTrue(closeablesRef.get().contains(mockCloseable));
    }

    @Test
    public void testEnableCloseableTracing() {
        assertNotNull(getCloseablesRef().get());
    }

    @Test
    public void testDisableCloseableTracing() {
        CloseableUtils.disableCloseableTracing();
        assertNull(getCloseablesRef().get());
    }

    // Private helper to access the private CLOSEABLES field in CloseableUtils
    private AtomicReference<Set<Closeable>> getCloseablesRef() {
        try {
            java.lang.reflect.Field field = CloseableUtils.class.getDeclaredField("CLOSEABLES");
            field.setAccessible(true);
            //noinspection unchecked
            return (AtomicReference<Set<Closeable>>) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testWaitForCloseablesToClose() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosing()).thenReturn(true);

        assertTrue(CloseableUtils.waitForCloseablesToClose(1000));
    }

    @Test(expected = IllegalStateException.class)
    public void testWaitForCloseablesToCloseWithException() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosing()).thenReturn(false);
        doThrow(IllegalStateException.class).when(mockCloseable).isClosing();

        CloseableUtils.waitForCloseablesToClose(1000);
    }

    @Test
    public void testAssertCloseablesClosed() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosed()).thenReturn(true);

        CloseableUtils.assertCloseablesClosed();
    }

    @Test(expected = AssertionError.class)
    public void testAssertCloseablesClosedWithOpenCloseables() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosed()).thenReturn(false);

        CloseableUtils.assertCloseablesClosed();
    }

    @Test
    public void testUnmonitor() {
        CloseableUtils.add(mockCloseable);
        CloseableUtils.unmonitor(mockCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertFalse(closeablesRef.get().contains(mockCloseable));
    }

    @Test
    public void testCloseQuietlyArray() {
        Object[] array = {mock(Closeable.class), mock(Closeable.class)};

        CloseableUtils.closeQuietly(array);

        for (Object o : array) {
            verify((Closeable) o, times(1)).close();
        }
    }

    @Test
    public void testCloseQuietlyAutoCloseable() throws Exception {
        CloseableUtils.closeQuietly(mockAutoCloseable);

        verify(mockAutoCloseable, times(1)).close();
    }

    @Test
    public void testCloseQuietlyHttpURLConnection() {
        CloseableUtils.closeQuietly(mockHttpURLConnection);

        verify(mockHttpURLConnection, times(1)).disconnect();
    }
}