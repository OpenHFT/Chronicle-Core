package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.*;

public class CloseableUtilsTest {
    private ManagedCloseable mockCloseable;
    private AbstractCloseable anonCloseable;
    private AutoCloseable mockAutoCloseable;
    private HttpURLConnection mockHttpURLConnection;

    @Before
    public void mockitoNotSupportedOnJava21() {
        assumeTrue(Jvm.majorVersion() <= 17);
    }

    @Before
    public void setUp() {
        anonCloseable = new AbstractCloseable() {
            @Override
            protected void performClose() {

            }
        };
        mockitoNotSupportedOnJava21();
        mockCloseable = mock(ManagedCloseable.class);
        CloseableUtils.enableCloseableTracing();
        mockAutoCloseable = mock(AutoCloseable.class);
        mockHttpURLConnection = mock(HttpURLConnection.class);
    }

    @After
    public void tearDown() {
        CloseableUtils.disableCloseableTracing();
        Closeable.closeQuietly(anonCloseable);
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
    public void testIOToolsUnmonitor() {
        IOTools.unmonitor(null);
        IOTools.unmonitor("hello");
        CloseableUtils.add(anonCloseable);
        IOTools.unmonitor(anonCloseable);
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
