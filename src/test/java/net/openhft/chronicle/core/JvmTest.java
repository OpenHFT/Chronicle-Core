/*
 *
 *  *     Copyright (C) 2016  higherfrequencytrading.com
 *  *
 *  *     This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU Lesser General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU Lesser General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU Lesser General Public License
 *  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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