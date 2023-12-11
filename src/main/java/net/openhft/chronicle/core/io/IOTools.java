/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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
import net.openhft.chronicle.core.internal.util.DirectBufferUtil;
import net.openhft.chronicle.core.util.Time;
import org.jetbrains.annotations.NotNull;
import sun.nio.ch.IOStatus;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * IOTools is a utility class containing a variety of methods and constants
 * designed for handling input/output (IO) operations, especially regarding
 * file and directory manipulation.
 * <p>
 * It provides functionality such as:
 * <ul>
 *     <li>Determining if an exception is due to a closed connection</li>
 *     <li>Deleting directories and their files</li>
 *     <li>Reading files from classpath or file system</li>
 *     <li>Writing files to file system</li>
 *     <li>Creating temporary files and directories</li>
 * </ul>
 * <p>
 * It also handles some specific byte buffer operations and manages a
 * {@link ConcurrentHashMap} for tracking object counts of specific types.
 * <p>
 * Note: This class is designed to be thread-safe and is intended to be used
 * concurrently.
 */
public final class IOTools {
    public static final int IOSTATUS_INTERRUPTED = IOStatus.INTERRUPTED;
    private static final Map<Class<?>, AtomicInteger> COUNTER_MAP = new ConcurrentHashMap<>();
    private static final Set<String> CLOSED_MESSAGES =
            new HashSet<>(
                    Arrays.asList(
                            // isAWindowsEstablishedConnectionAbortedException
                            "An established connection was aborted by the software in your host machine",
                            // isAWindowsConnectionResetException
                            "An existing connection was forcibly closed by the remote host",
                            // isAnOSXBrokenPipeException
                            "Broken pipe",
                            // on Windows
                            "Broken pipe (Write failed)",
                            // isALinuxJava13OrGreaterConnectionResetException
                            "Connection reset",
                            // isALinuxJava12OrLessConnectionResetException
                            "Connection reset by peer",
                            "Remotely Closed",
                            // Timeout of a WinSock write on Windows
                            "Software caused connection abort: socket write error",
                            // If you attempt to write to a closed socket
                            "Socket is closed",
                            // If you attempt to write to a closed stream
                            "Stream is closed",
                            // Non socket messages e.g. InflaterInputStream
                            "Stream closed"
                    )
            );

    // Suppresses default constructor, ensuring non-instantiability.
    private IOTools() {
    }

    /**
     * Is the passed exception one that results from reading from or writing to
     * a reset or closed connection?
     * <p>
     * NOTE: This is not reliable and shouldn't be used for anything critical. We use it
     * to make logging less noisy, false negatives are acceptable and expected. It should
     * not produce false positives, but there's no guarantees it doesn't.
     *
     * @param e The exception
     * @return true if the exception is one that signifies the connection was reset/closed
     */
    public static boolean isClosedException(Exception e) {
        Language.warnOnce();
        return e instanceof IOException
                && (CLOSED_MESSAGES.contains(e.getMessage())
                || e.getClass().getName().contains("Close"));
    }

    /**
     * Attempts to delete a directory with its files. If the directory or any
     * subdirectories contain directories themselves, the method will stop at the
     * first layer of directories and will not delete them.
     *
     * @param directory The directory to be deleted
     * @return true if deletion is successful, false otherwise
     * @throws IORuntimeException if an I/O error occurs
     */
    public static boolean shallowDeleteDirWithFiles(@NotNull String directory) throws IORuntimeException {
        return shallowDeleteDirWithFiles(new File(directory));
    }

    /**
     * Attempts to delete a directory with its files. If the directory or any
     * subdirectories contain directories themselves, the method will stop at the
     * first layer of directories and will not delete them.
     *
     * @param dir The directory to be deleted
     * @return true if deletion is successful, false otherwise
     * @throws IORuntimeException if an I/O error occurs
     */
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

    /**
     * Attempts to delete the specified directory and its files recursively up to a
     * given depth. If the depth limit is reached, an {@link AssertionError} is thrown.
     *
     * @param dir      The directory to be deleted
     * @param maxDepth The maximum depth of directories to be deleted
     * @return true if deletion is successful, false otherwise
     * @throws IORuntimeException if an I/O error occurs
     */
    public static boolean deleteDirWithFiles(@NotNull String dir, int maxDepth) throws IORuntimeException {
        return deleteDirWithFiles(new File(dir), maxDepth);
    }

    /**
     * Attempts to delete directories and their files. If any directory is not
     * deleted successfully, throws an {@link AssertionError}.
     *
     * @param dir The directories to be deleted
     * @throws IORuntimeException if an I/O error occurs
     */
    public static boolean deleteDirWithFiles(@NotNull File dir) throws IORuntimeException {
        return deleteDirWithFiles(dir, 20);
    }

    /**
     * Attempts to delete directories and their files. If any directory is not
     * deleted successfully, throws an {@link AssertionError}.
     *
     * @param dir      The directories to be deleted
     * @param maxDepth The maximum depth of directories to be deleted
     * @throws IORuntimeException if an I/O error occurs
     */
    public static boolean deleteDirWithFiles(@NotNull File dir, int maxDepth) throws IORuntimeException {
        final File[] entries = dir.listFiles();
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

    /**
     * Canonical usage is to call this *before* your test, so you fail fast if you can't delete
     *
     * @param dirs dirs
     */
    public static void deleteDirWithFilesOrThrow(@NotNull String... dirs) throws IORuntimeException {
        final File[] files = Arrays.stream(dirs).map(File::new).toArray(File[]::new);
        deleteDirWithFilesOrThrow(files);
    }

    /**
     * Canonical usage is to call this *before* your test, so you fail fast if you can't delete
     *
     * @param dirs dirs
     */
    public static void deleteDirWithFilesOrThrow(@NotNull File... dirs) throws IORuntimeException {
        for (File dir : dirs) {
            if (!deleteDirWithFiles(dir) && dir.exists())
                throw new AssertionError("Could not delete " + dir);
        }
    }

    /**
     * Ensures that directory is absent or deleted, awaits for given timeout if necessary.
     *
     * @param timeoutMs Time to ensure that the directory is absent.
     * @param dir       Dir to delete.
     * @throws AssertionError If timeout passed and the directory is still present.
     */
    public static void deleteDirWithFilesOrWait(long timeoutMs, @NotNull File dir) {
        long startTs = System.currentTimeMillis();

        do {
            if (deleteDirWithFiles(dir))
                return;

            if (dir.exists())
                Jvm.pause(50);
            else
                return;
        } while (System.currentTimeMillis() - startTs < timeoutMs);

        throw new AssertionError("Failed to delete dir " + dir + " within " + timeoutMs + "ms");
    }

    /**
     * Ensures that directory is absent or deleted, awaits for given timeout if necessary.
     *
     * @param clazz file to use to determine the class loader.
     * @param name  Name of the file to find.
     * @return URL of the file.
     * @throws FileNotFoundException if the file is not found.
     */
    @NotNull
    public static URL urlFor(Class<?> clazz, String name) throws FileNotFoundException {
        return urlFor(clazz.getClassLoader(), name);
    }

    /**
     * Ensures that directory is absent or deleted, awaits for given timeout if necessary.
     *
     * @param classLoader Class loader to use to find the file.
     * @param name        Name of the file to find.
     * @return URL of the file.
     * @throws FileNotFoundException if the file is not found.
     */
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

    /**
     * Creates and returns a new InputStream from the provided {@code url}.
     * <p>
     * It is up to the caller to close the returned InputStream after being used.
     *
     * @param url to create an InputStream from
     * @return an InputStream
     * @throws IOException if the URL cannot be opened
     */
    public static InputStream open(URL url) throws IOException {
        final InputStream in = url.openStream();
        if (url.getFile().endsWith(".gz")) {
            return new GZIPInputStream(in);
        }
        return in;
    }

    /**
     * This method first looks for the file in the classpath. If this is not found it
     * appends the suffix .gz and looks again in the classpath to see if it is present.
     * If it is still not found it looks for the file on the file system. If it not found
     * it appends the suffix .gz and looks again on the file system.
     * If it still not found a FileNotFoundException is thrown.
     *
     * @param clazz file to use to determine the class loader.
     * @param name  Name of the file
     * @return A byte[] containing the contents of the file
     * @throws IOException FileNotFoundException thrown if file is not found
     */
    public static byte[] readFile(Class<?> clazz, @NotNull String name) throws IOException {
        URL url = urlFor(clazz, name);
        InputStream is = open(url);

        return readAsBytes(is);
    }

    /**
     * Reads data from an input stream and returns it as a byte array.
     *
     * @param is The input stream to read data from
     * @return A byte array containing the data read from the input stream
     * @throws IOException If an I/O error occurs
     */
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
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(512, is.available()))) {
            byte[] bytes = new byte[1024];
            for (int len; (len = is.read(bytes)) > 0; )
                out.write(bytes, 0, len);
            return out.toByteArray();
        } finally {
            Closeable.closeQuietly(is);
        }
    }

    /**
     * Writes the given byte array to a file with the specified name.
     *
     * @param filename The name of the file to write to
     * @param bytes    The byte array to write to the file
     * @throws IOException If an I/O error occurs
     */
    public static void writeFile(@NotNull String filename, byte @NotNull [] bytes) throws IOException {
        try (@NotNull OutputStream out0 = new FileOutputStream(filename)) {
            OutputStream out = out0;
            if (filename.endsWith(".gz"))
                out = new GZIPOutputStream(out);
            out.write(bytes);
            out.close();
        }
    }

    /**
     * Creates a temporary name for a file by appending the system's current
     * nanosecond time to the file name.
     *
     * @param filename The original file name
     * @return A temporary file name
     */
    @NotNull
    public static String tempName(@NotNull String filename) {
        int ext = filename.lastIndexOf('.');
        if (ext > 0 && ext > filename.length() - 5) {
            return filename.substring(0, ext) + System.nanoTime() + filename.substring(ext);
        }
        return filename + System.nanoTime();
    }

    /**
     * Calls the system's Cleaner Service to clean the given ByteBuffer.
     *
     * @param bb The ByteBuffer to clean
     */
    public static void clean(ByteBuffer bb) {
        CleanerServiceLocator.cleanerService().clean(bb);
    }

    /**
     * Creates a directory at the specified path, including any necessary
     * but nonexistent parent directories.
     *
     * @param dir The path of the directory to create
     * @throws IOException If an I/O error occurs
     */
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

    /**
     * Gets the atomic counter associated with a specific class.
     *
     * @param type The class for which to get the counter
     * @return An AtomicInteger that is the counter for the specified class
     */
    static AtomicInteger counter(final Class<?> type) {
        return COUNTER_MAP.computeIfAbsent(type, k -> new AtomicInteger());
    }

    /**
     * Creates a temporary file in a temporary directory with the specified name.
     *
     * @param s The name of the temporary file to create
     * @return The temporary file that was created
     */
    public static File createTempFile(String s) {
        File file = createTempDirectory(s).toFile();
        file.deleteOnExit();
        return file;
    }

    /**
     * Creates a temporary directory with the specified name.
     *
     * @param s The name of the temporary directory to create
     * @return The path to the temporary directory that was created
     */
    public static Path createTempDirectory(String s) {
        new File(OS.getTarget()).mkdir();
        return Paths.get(OS.getTarget(), s + "-" + Time.uniqueId() + ".tmp");
    }

    /**
     * Stops the monitoring of the specified object.
     *
     * @param t The object to stop monitoring
     */
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

    private static void unmonitor(Class<?> aClass, Object t, int depth) {
        if (aClass == null || aClass == Object.class)
            return;
        unmonitor(aClass.getSuperclass(), t, depth);
        for (Field field : aClass.getDeclaredFields()) {
            if (field.getType().isPrimitive() || Modifier.isStatic(field.getModifiers()))
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

    /**
     * Checks if a ByteBuffer is a direct buffer.
     *
     * @param byteBuffer The ByteBuffer to check
     * @return true if the ByteBuffer is a direct buffer, false otherwise
     */
    public static boolean isDirectBuffer(ByteBuffer byteBuffer) {
        return byteBuffer.isDirect();
    }

    /**
     * Gets the memory address of the given ByteBuffer.
     *
     * @param byteBuffer The ByteBuffer for which to get the memory address
     * @return The memory address of the ByteBuffer
     */
    public static long addressFor(ByteBuffer byteBuffer) {
        return DirectBufferUtil.addressOrThrow(byteBuffer);
    }

    /**
     * Normalizes an I/O operation status code.
     *
     * @param n The I/O status code to normalize
     * @return The normalized I/O status code
     */
    public static int normaliseIOStatus(int n) {
        return IOStatus.normalize(n);
    }

    // has to be moved to another class to avoid a live lock
    @NotNull
    static Runnable close3(SocketChannel sc, SocketChannel s2) {
        return () -> {
            Jvm.pause(50);
            System.out.println("Close " + sc);
            Closeable.closeQuietly(sc);
            Jvm.pause(10);
            Closeable.closeQuietly(s2);
        };
    }

    // has to be moved to another class to avoid a live lock
    @NotNull
    static Runnable close4(SocketChannel sc, SocketChannel s2, Thread main) {
        return () -> {
            Jvm.pause(50);
            main.interrupt();
            Jvm.pause(10);
            Closeable.closeQuietly(sc);
            Closeable.closeQuietly(s2);
        };
    }

    private static final class Language {
        static {
            if (!Locale.getDefault().getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                try {
                    addRegionalMessages();
                } catch (IOException ioe) {
                    Jvm.warn().on(IOTools.class,
                            "Running under non-English locale '" + Locale.getDefault().getLanguage() +
                                    "', transient exceptions will be reported with higher level than necessary.", ioe);
                }
            }
        }

        static void addRegionalMessages() throws IOException {
            try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
                ssc.bind(new InetSocketAddress(0));
                final int port = ssc.socket().getLocalPort();
                final InetSocketAddress address = new InetSocketAddress("localhost", port);
                try (Socket s = new Socket("localhost", port);
                     SocketChannel s2 = ssc.accept()) {
                    final OutputStream os = s.getOutputStream();
                    s2.close();
                    final byte[] bytes = new byte[512];
                    try {
                        for (int i = 0; i < 100; i++) {
                            os.write(bytes);
                        }
                    } catch (IOException ioe) {
                        CLOSED_MESSAGES.add(ioe.getMessage());
                    } finally {
                        s.close();
                    }
                    try {
                        s.getOutputStream().write(bytes);
                    } catch (IOException ioe) {
                        CLOSED_MESSAGES.add(ioe.getMessage());
                    }
                }
                try (Socket s = new Socket("localhost", port);
                     SocketChannel s2 = ssc.accept()) {
                    OutputStream os = s.getOutputStream();
                    os.close();
                    os.write(1);
                } catch (IOException ioe) {
                    CLOSED_MESSAGES.add(ioe.getMessage());
                }
                ByteBuffer bytes = ByteBuffer.allocateDirect(1024);
                try (SocketChannel sc = SocketChannel.open(address);
                     SocketChannel s2 = ssc.accept()) {
                    try {
                        for (int i = 0; i < 100; i++) {
                            bytes.clear();
                            sc.write(bytes);
                        }
                    } catch (IOException ioe) {
                        CLOSED_MESSAGES.add(ioe.getMessage());
                    }
                }
                try (SocketChannel sc = SocketChannel.open(address);
                     SocketChannel s2 = ssc.accept()) {
                    Thread t = new Thread(close3(sc, s2), "close~3");
                    t.setDaemon(true);
                    t.start();
                    try {
                        for (int i = 0; i < 10000; i++) {
                            bytes.clear();
                            final int write = sc.write(bytes);
                            assert write > 0;
                        }
                    } catch (IOException ioe) {
                        CLOSED_MESSAGES.add(ioe.getMessage());
                    }
                }
                try (SocketChannel sc = SocketChannel.open(address);
                     SocketChannel s2 = ssc.accept()) {
                    Thread main = Thread.currentThread();
                    Thread t = new Thread(close4(sc, s2, main), "close~4");
                    t.setDaemon(true);
                    t.start();
                    try {
                        for (int i = 0; i < 10000; i++) {
                            bytes.clear();
                            final int write = sc.write(bytes);
                            assert write > 0;
                        }
                    } catch (IOException ioe) {
                        CLOSED_MESSAGES.add(ioe.getMessage());
                    }
                }
            }
        }

        static void warnOnce() {
            // No-op.
        }
    }
}
