/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;
import sun.reflect.Reflection;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by peter on 26/08/15.
 * A collection of CONCURRENT utility tools
 */
public enum IOTools {
    ;

    public static boolean shallowDeleteDirWithFiles(String directory) throws IORuntimeException {
        return shallowDeleteDirWithFiles(new File(directory));
    }

    public static boolean shallowDeleteDirWithFiles(File dir) throws IORuntimeException {
        return deleteDirWithFiles(dir, 1);
    }

    public static boolean deleteDirWithFiles(String dir, int maxDepth) throws IORuntimeException {
        return deleteDirWithFiles(new File(dir), maxDepth);
    }

    public static boolean deleteDirWithFiles(File dir, int maxDepth) throws IORuntimeException {
        File[] entries = dir.listFiles();
        if (entries == null) return false;
        Stream.of(entries).filter(File::isDirectory).forEach(f -> {
            if (maxDepth < 1) {
                throw new AssertionError("Contains directory " + f);
            } else {
                deleteDirWithFiles(f, maxDepth - 1);
            }
        });
        Stream.of(entries).forEach(f -> {
            try {
                Files.delete(f.toPath());
            } catch (IOException e) {
                Jvm.debug().on(Closeable.class, "Failed to delete " + f, e);
            }
        });
        return dir.delete();
    }

    /**
     * This method first looks for the file in the classpath. If this is not found it
     * appends the suffix .gz and looks again in the classpath to see if it is present.
     * If it is still not found it looks for the file on the file system. If it not found
     * it appends the suffix .gz and looks again on the file system.
     * If it still not found a FileNotFoundException is thrown.
     *
     * @param name Name of the file
     * @return A byte[] containing the contents of the file
     * @throws IOException FileNotFoundException thrown if file is not found
     */
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

    public static void clean(ByteBuffer bb) {
        if (bb instanceof DirectBuffer) {
            Cleaner cl = ((DirectBuffer) bb).cleaner();
            if (cl != null)
                cl.clean();
        }
    }
}
