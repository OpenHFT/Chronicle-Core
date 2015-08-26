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

package net.openhft.chronicle.core.io;

import sun.reflect.Reflection;

import java.io.*;

/**
 * Created by peter on 26/08/15.
 */
public enum IOTools {
    ;

    public static byte[] readFile(String name) throws IOException {
        ClassLoader classLoader;
        try {
            classLoader = Reflection.getCallerClass().getClassLoader();
        } catch (Throwable e) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        InputStream is = classLoader.getResourceAsStream(name);
        if (is == null)
            is = classLoader.getResourceAsStream(name + ".gz");
        if (is == null)
            try {
                is = new FileInputStream(name);
            } catch (FileNotFoundException e) {
                try {
                    is = new FileInputStream(name + ".gz");
                } catch (FileNotFoundException e1) {
                    throw e;
                }
            }
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(512, is.available()));
        byte[] bytes = new byte[1024];
        for (int len; (len = is.read(bytes)) > 0; )
            out.write(bytes, 0, len);
        return out.toByteArray();
    }
}
