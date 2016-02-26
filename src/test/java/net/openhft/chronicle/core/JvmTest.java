package net.openhft.chronicle.core;

import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import javax.naming.ConfigurationException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Created by peter on 26/02/2016.
 */
public class JvmTest {

    @Test(expected = ConfigurationException.class)
    public void testRethrow() throws Exception {
        Jvm.rethrow(new ConfigurationException());
    }

    @Test
    public void testTrimStackTrace() throws Exception {
        // TODO: 26/02/2016
    }

    @Test
    public void testTrimFirst() throws Exception {
        // TODO: 26/02/2016

    }

    @Test
    public void testTrimLast() throws Exception {
        // TODO: 26/02/2016

    }

    @Test
    public void testIsInternal() throws Exception {
        assertTrue(Jvm.isInternal(String.class.getName()));
        assertFalse(Jvm.isInternal(getClass().getName()));
    }

    @Test
    public void testPause() throws Exception {
        // TODO: 26/02/2016

    }

    @Test
    public void testBusyWaitMicros() throws Exception {
        // TODO: 26/02/2016
    }

    @Test
    public void testGetField() throws Exception {
        // TODO: 26/02/2016

    }

    @Test
    public void testGetValue() throws Exception {
        ByteBuffer bb = ByteBuffer.allocateDirect(128);
        long address = Jvm.getValue(bb, "address");
        assertEquals(((DirectBuffer) bb).address(), address);

    }

    @Test
    public void testLockWithStack() throws Exception {
        // TODO: 26/02/2016

    }

    @Test
    public void testUsedDirectMemory() throws Exception {
        long used = Jvm.usedDirectMemory();
        ByteBuffer.allocateDirect(4 << 10);
        assertEquals(used + (4 << 10), Jvm.usedDirectMemory());
    }

    @Test
    public void testMaxDirectMemory() throws Exception {
        long maxDirectMemory = Jvm.maxDirectMemory();
        assertTrue(maxDirectMemory > 0);
    }
}