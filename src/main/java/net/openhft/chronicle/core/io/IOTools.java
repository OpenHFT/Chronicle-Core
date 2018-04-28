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
import net.openhft.chronicle.core.cleaner.CleanerServiceLocator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/*
 * Created by Peter Lawrey on 26/08/15.
 * A collection of CONCURRENT utility tools
 */
public enum IOTools {
    ;

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
        CleanerServiceLocator.cleanerService().clean(bb);
    }

    public static void createDirectories(Path dir) throws IOException {
        if (dir == null || dir.getNameCount() == 0 || Files.isDirectory(dir))
            return;
        createDirectories(dir.getParent());
        try {
            Files.createDirectory(dir);
        } catch (FileAlreadyExistsException e) {
            if (Files.isSymbolicLink(dir))
                throw new IOException("Symbolic link from " + dir + " to " + Files.readSymbolicLink(dir) + " is broken", e);
            if (Files.isRegularFile(dir))
                throw new IOException("Cannot create a directory with the same name as a file " + dir, e);
        } catch (AccessDeniedException e) {
            if (!dir.toFile().canWrite())
                throw new IOException("Cannot write to " + dir, e);
        }
    }

}