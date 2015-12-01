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

import net.openhft.chronicle.core.io.IORuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Created by peter on 01/12/15.
 */
public enum GZIP {
    ;

    public static byte[] uncompress(byte[] bytes) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bytes));
            byte[] buffer = new byte[512];
            for (int len; (len = in.read(buffer)) > 0; )
                baos.write(buffer, 0, len);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }
}
