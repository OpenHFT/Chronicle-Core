/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.core;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class OSTest {
    @Test
    public void testIs64Bit() {
        System.out.println("is64 = " + OS.is64Bit());
    }

    @Test
    public void testGetProcessId() {
        System.out.println("pid = " + OS.getProcessId());
    }

    @Test
    @Ignore("Should always pass, or crash the JVM based on length")
    public void testMap()            throws Exception {
        if (!OS.isWindows()) return;

        // crashes the JVM.
//        long length = (4L << 30L) + (64 << 10);
        // doesn't crash the JVM.
        long length = (4L << 30L);

        String name = OS.TARGET + "/deleteme";
        new File(name).deleteOnExit();
        FileChannel fc = new RandomAccessFile(name, "rw").getChannel();
        long address = OS.map0(fc, OS.imodeFor(FileChannel.MapMode.READ_WRITE), 0, length);
        for (long offset = 0; offset < length; offset += OS.pageSize())
            OS.memory().writeLong(address + offset, offset);
        OS.unmap(address, length);
    }
}