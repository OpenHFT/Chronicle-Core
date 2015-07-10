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

package net.openhft.chronicle.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by peter on 10/07/15.
 */
public class TimeTest {

    @Test
    public void testTickTime() throws InterruptedException {
        long start = Time.tickTime();
        for (int i = 0; i < 10; i++) {
            Thread.sleep(10);
            Time.tickTime();
        }
        long last = Time.tickTime();
        assertEquals(11, last - start, 1);
    }

    @Test
    public void testFastTime() throws InterruptedException {
        long start = Time.tickTime();
        for (int i = 0; i < 100; i++) {
            Thread.sleep(1);
            Time.tickTime();
        }
        long last = Time.tickTime();
        assertEquals(102, last - start, 2);
    }
}