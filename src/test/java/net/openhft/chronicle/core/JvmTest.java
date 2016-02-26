package net.openhft.chronicle.core;

import org.junit.Test;

import javax.naming.ConfigurationException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * Created by peter_2 on 26/02/2016.
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
        // TODO: 26/02/2016

    }

    @Test
    public void testIsDebug() throws Exception {
        // TODO: 26/02/2016

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
        // TODO: 26/02/2016

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
    public void testUsedNativeMemory() throws Exception {

    }

    @Test
    public void testMaxDirectMemory() throws Exception {

    }
}