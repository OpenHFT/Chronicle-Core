/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.cleaner.CleanerServiceLocator;
import net.openhft.chronicle.core.util.Time;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/*
 * A collection of CONCURRENT utility tools
 */
public final class IOTools {
    private IOTools() { }

    static volatile Map<Class, AtomicInteger> COUNTER_MAP = new ConcurrentHashMap<>();

    public static boolean shallowDeleteDirWithFiles(@NotNull String directory) throws IORuntimeException {
        return shallowDeleteDirWithFiles(new File(directory));
    }

    public static boolean shallowDeleteDirWithFiles(@NotNull File dir) throws IORuntimeException {
        return deleteDirWithFiles(dir, 1);
    }

    public static boolean deleteDirWithFiles(@NotNull String... dirs) throws IORuntimeException {
        boolean result = true;
        for (String dir : dirs) {
            boolean r = deleteDirWithFiles(dir, 20);
            result &= r;
        }
        return result;
    }

    public static boolean deleteDirWithFiles(@NotNull String dir, int maxDepth) throws IORuntimeException {
        return deleteDirWithFiles(new File(dir), maxDepth);
    }

    public static boolean deleteDirWithFiles(@NotNull File dir) throws IORuntimeException {
        return deleteDirWithFiles(dir, 20);
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

    public static void deleteDirWithFilesOrThrow(@NotNull String... dirs) throws IORuntimeException {
        final File[] files = Arrays.stream(dirs).map(File::new).toArray(File[]::new);
        deleteDirWithFilesOrThrow(files);
    }

    /**
     * Canonical usage is to call this *before* your test so you fail fast if you can't delete
     *
     * @param dirs dirs
     * @throws IORuntimeException
     */
    public static void deleteDirWithFilesOrThrow(@NotNull File... dirs) throws IORuntimeException {
        for (File dir : dirs)
            if (!deleteDirWithFiles(dir))
                if (dir.exists())
                    throw new AssertionError("Could not delete " + dir);
    }

    @Deprecated(/* to be removed in x.22 */)
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
        if (url == null && new File(name).exists())
            try {
                url = new URL("file", "", new File(name).getAbsolutePath());
            } catch (MalformedURLException e) {
                FileNotFoundException fnfe = new FileNotFoundException(name);
                fnfe.initCause(e);
                throw fnfe;
            }
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
        InputStream is = open(urlFor(name));

        return readAsBytes(is);
    }

    public static byte[] readFile(Class clazz, @NotNull String name) throws IOException {
        URL url = urlFor(clazz.getClassLoader(), name);
        InputStream is = open(url);

        return readAsBytes(is);
    }

    public static byte[] readAsBytes(InputStream is) throws IOException {
        if (is instanceof FileInputStream) {
            try (FileInputStream fis = (FileInputStream) is) {
                byte[] bytes = new byte[fis.available()];
                int read = fis.read(bytes);
                if (read != bytes.length)
                    throw new AssertionError();
                return bytes;
            }
        }
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

    static AtomicInteger counter(Class type) {
        return COUNTER_MAP.computeIfAbsent(type, k -> new AtomicInteger());
    }

    public static File createTempFile(String s) {
        File file = createTempDirectory(s).toFile();
        file.deleteOnExit();
        return file;
    }

    public static Path createTempDirectory(String s) {
        new File(OS.getTarget()).mkdir();
        return Paths.get(OS.getTarget(), s + "-" + Time.uniqueId() + ".tmp");
    }

    public static void unmonitor(final Object t) {
        unmonitor(t, 4);
    }

    private static void unmonitor(final Object t, int depth) {
        if (t == null)
            return;
        if (t instanceof Serializable || t instanceof MonitorReferenceCounted) // old school.
            return;
        if (t instanceof Closeable)
            AbstractCloseable.unmonitor((Closeable) t);
        if (t instanceof ReferenceCounted)
            AbstractReferenceCounted.unmonitor((ReferenceCounted) t);
        if (depth > 0)
            unmonitor(t.getClass(), t, depth - 1);
    }

    private static <T> void unmonitor(Class aClass, Object t, int depth) {
        if (aClass == null || aClass == Object.class)
            return;
        unmonitor(aClass.getSuperclass(), t, depth);
        for (Field field : aClass.getDeclaredFields()) {
            if (field.getType().isPrimitive())
                continue;
            if (Modifier.isStatic(field.getModifiers()))
                continue;
            try {
                field.setAccessible(true);
            } catch (Exception e) {
                if (!Jvm.isJava9Plus())
                    Jvm.warn().on(IOTools.class, e);
                continue;
            }
            try {
                Object o = field.get(t);
                if (o != null)
                    unmonitor(o, depth);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                Jvm.warn().on(IOTools.class, e);
            }
        }
    }
}