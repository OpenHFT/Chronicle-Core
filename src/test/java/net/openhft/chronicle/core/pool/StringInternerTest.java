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

package net.openhft.chronicle.core.pool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by peter on 17/09/15.
 */
public class StringInternerTest {
    @Test
    public void testIntern() throws Exception {
        StringInterner si = new StringInterner(128);
        for (int i = 0; i < 100; i++) {
            si.intern("" + i);
        }
        assertEquals(68, si.valueCount());
    }
}