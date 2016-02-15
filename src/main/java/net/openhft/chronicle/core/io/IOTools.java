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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
                    is = new GZIPInputStream(new FileInputStream(name + ".gz"));
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

    public static void writeFile(String filename, byte[] bytes) throws IOException {
        OutputStream out = new FileOutputStream(filename);
        if (filename.endsWith(".gz"))
            out = new GZIPOutputStream(out);
        out.write(bytes);
        out.close();
    }

    public static String tempName(String filename) {
        int ext = filename.lastIndexOf('.');
        if (ext > 0 && ext > filename.length() - 5) {
            return filename.substring(0, ext) + System.nanoTime() + filename.substring(ext);
        }
        return filename + System.nanoTime();
    }
}
