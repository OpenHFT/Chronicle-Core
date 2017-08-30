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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.nio.ch.DirectBuffer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/*
 * Created by Peter Lawrey on 26/08/15.
 * A collection of CONCURRENT utility tools
 */
public enum IOTools {
    ;

    private static MethodHandle DIRECT_BUFFER_CLEANER_METHOD_HANDLE;
    private static MethodHandle CLEANER_CLEAN_METHOD_HANDLE;

    static {
        final Class<?> cleanerClass;
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            if (Jvm.isJava9Plus()) {
                cleanerClass = Class.forName("jdk.internal.ref.Cleaner");
            } else {
                cleanerClass = Class.forName("sun.misc.Cleaner");
            }

            DIRECT_BUFFER_CLEANER_METHOD_HANDLE = lookup.findVirtual(DirectBuffer.class, "cleaner",
                    MethodType.methodType(cleanerClass));
            CLEANER_CLEAN_METHOD_HANDLE = lookup.findVirtual(cleanerClass, "clean",
                    MethodType.methodType(Void.class));
        } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            Jvm.warn().on(IOTools.class, "Failed to load method handles for JDK 9 compatibility", e);
        }
    }

    public static boolean shallowDeleteDirWithFiles(@NotNull String directory) throws IORuntimeException {
        return shallowDeleteDirWithFiles(new File(directory));
    }

    public static boolean shallowDeleteDirWithFiles(@NotNull File dir) throws IORuntimeException {
        return deleteDirWithFiles(dir, 1);
    }

    public static boolean deleteDirWithFiles(@NotNull String dir, int maxDepth) throws IORuntimeException {
        return deleteDirWithFiles(new File(dir), maxDepth);
    }

    public static boolean deleteDirWithFiles(@NotNull File dir, int maxDepth) throws IORuntimeException {
        @Nullable File[] entries = dir.listFiles();
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
            } catch (NoSuchFileException fe) {
                // ignored
            } catch (IOException e) {
                Jvm.debug().on(Closeable.class, "Failed to delete " + f, e);
            }
        });
        return dir.delete();
    }

    @Deprecated
    public static URL urlFor(String name) throws FileNotFoundException {
        // use the callers class loader not the default one if possible.
        return urlFor(Thread.currentThread().getContextClassLoader(), name);
    }

    @NotNull
    public static URL urlFor(Class clazz, String name) throws FileNotFoundException {
        return urlFor(clazz.getClassLoader(), name);
    }

    @NotNull
    public static URL urlFor(ClassLoader classLoader, String name) throws FileNotFoundException {
        URL url = classLoader.getResource(name);
        if (url == null && name.startsWith("/"))
            url = classLoader.getResource(name.substring(1));
        if (url == null)
            url = classLoader.getResource(name + ".gz");
        if (url == null)
            throw new FileNotFoundException(name);
        return url;
    }

    public static InputStream open(URL url) throws IOException {
        InputStream in = url.openStream();
        if (url.getFile().endsWith(".gz"))
            in = new GZIPInputStream(in);
        return in;
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
    public static byte[] readFile(@NotNull String name) throws IOException {
        URL url = urlFor(name);
        InputStream is = open(url);

        return readAsBytes(is);
    }

    public static byte[] readFile(Class clazz, @NotNull String name) throws IOException {
        URL url = urlFor(clazz, name);
        InputStream is = open(url);

        return readAsBytes(is);
    }

    public static byte[] readAsBytes(InputStream is) throws IOException {
        try {
            @NotNull ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(512, is.available()));
            @NotNull byte[] bytes = new byte[1024];
            for (int len; (len = is.read(bytes)) > 0; )
                out.write(bytes, 0, len);
            return out.toByteArray();
        } finally {
            Closeable.closeQuietly(is);
        }
    }

    public static void writeFile(@NotNull String filename, @NotNull byte[] bytes) throws IOException {
        try (@NotNull OutputStream out0 = new FileOutputStream(filename)) {
            OutputStream out = out0;
            if (filename.endsWith(".gz"))
                out = new GZIPOutputStream(out);
            out.write(bytes);
            out.close();
        }
    }

    @NotNull
    public static String tempName(@NotNull String filename) {
        int ext = filename.lastIndexOf('.');
        if (ext > 0 && ext > filename.length() - 5) {
            return filename.substring(0, ext) + System.nanoTime() + filename.substring(ext);
        }
        return filename + System.nanoTime();
    }

    public static void clean(ByteBuffer bb) {
        if (bb instanceof DirectBuffer &&
                DIRECT_BUFFER_CLEANER_METHOD_HANDLE != null &&
                CLEANER_CLEAN_METHOD_HANDLE != null) {
            try {
                final Object cleaner = DIRECT_BUFFER_CLEANER_METHOD_HANDLE.invoke((DirectBuffer) bb);


                if (cleaner != null) {
                    CLEANER_CLEAN_METHOD_HANDLE.invoke(cleaner);
                }
            } catch (Throwable t) {
                Jvm.warn().on(IOTools.class, "Failed to invoke cleaner on DirectBuffer", t);
            }
        }
    }
}