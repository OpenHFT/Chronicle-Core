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

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.internal.Bootstrap;
import net.openhft.chronicle.core.util.ClassLocal;
import net.openhft.chronicle.core.util.ThrowingFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.nio.ch.FileChannelImpl;

import javax.naming.TimeLimitExceededException;
import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static net.openhft.chronicle.core.util.Longs.*;

/**
 * Provides low-level access to operating system-specific functionality.
 * The OS class contains utility methods and fields related to the operating system, such as file handling, memory mapping, and process information.
 * This class includes methods to access and manipulate OS-specific resources using reflection and native methods.
 */
public final class OS {
    @Deprecated(/* to be removed in x.26, use getUserDir() */)
    public static final String USER_DIR = Jvm.getProperty("user.dir");

    /**
     * The temporary directory path for the operating system.
     */
    public static final String TMP = findTmp();

    /**
     * The home directory of the current user.
     */
    public static final String USER_HOME = Jvm.getProperty("user.home");

    /**
     * An exception instance indicating a time limit has been exceeded.
     */
    public static final Exception TIME_LIMIT = new TimeLimitExceededException();

    /**
     * The safe page size for memory mapping, set to 64 KB.
     */
    public static final int SAFE_PAGE_SIZE = 64 << 10;

    /**
     * ClassLocal cache for MethodHandles related to memory mapping.
     */
    static final ClassLocal<MethodHandle> MAP0_MH = ClassLocal.withInitial(c -> {
        try {
            Method map0;
            // Determine the correct method signature for the map0 method based on the Java version
            if (Jvm.isJava20Plus()) {
                Class<?> dispatcherClass = OS.isWindows() ? findClass("sun.nio.ch.FileDispatcherImpl") : findClass("sun.nio.ch.UnixFileDispatcherImpl");
                map0 = Jvm.getMethod(dispatcherClass, "map0", FileDescriptor.class, int.class, long.class, long.class, boolean.class);
            } else if (Jvm.isJava19Plus()) {
                map0 = Jvm.getMethod(c, "map0", FileDescriptor.class, int.class, long.class, long.class, boolean.class);
            } else if (Jvm.isJava14Plus()) {
                map0 = Jvm.getMethod(c, "map0", int.class, long.class, long.class, boolean.class);
            } else {
                map0 = Jvm.getMethod(c, "map0", int.class, long.class, long.class);
            }
            // Create a MethodHandle for the map0 method
            return MethodHandles.lookup().unreflect(map0);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    });

    // Field references and constants used for OS-level operations
    private static final Field FD_FIELD = Jvm.getField(FileChannelImpl.class, "fd");
    private static final String TARGET = findTarget();
    private static final String USER_NAME = Jvm.getProperty("user.name");
    private static final int MAP_RO = 0;  // Read-only memory mapping
    private static final int MAP_RW = 1;  // Read-write memory mapping
    private static final int MAP_PV = 2;  // Private memory mapping
    private static final boolean IS64BIT = is64Bit0();  // Checks if the OS is 64-bit
    private static final AtomicInteger PROCESS_ID = new AtomicInteger();  // Stores the current process ID
    private static final AtomicLong memoryMapped = new AtomicLong();  // Tracks the total memory mapped
    private static final MethodHandle UNMAPP0_MH;  // MethodHandle for unmapping memory
    private static final MethodHandle READ0_MH;  // MethodHandle for reading from a file descriptor
    private static final MethodHandle WRITE0_MH;  // MethodHandle for writing to a file descriptor
    private static final MethodHandle WRITE0_MH2;  // MethodHandle for alternate writing to a file descriptor
    private static final String PROC_SELF = "/proc/self";  // Path to self-process information in Unix-like systems
    private static final String PROC_SYS_KERNEL_PID_MAX = "/proc/sys/kernel/pid_max";  // Path to max PID in Unix-like systems
    private static int pageSize;  // Page size for memory mapping, avoid circular initialization
    private static int mapAlignment;  // Alignment for memory mapping

    // Static block for initializing MethodHandles and other OS-specific fields
    static {
        // Ensure JVM is initialized first
        Jvm.debug();
        try {
            Method unmap0;
            // Determine the correct unmap0 method based on the Java version
            if (Jvm.isJava20Plus()) {
                Class<?> dispatcherClass = OS.isWindows() ? findClass("sun.nio.ch.FileDispatcherImpl") : findClass("sun.nio.ch.UnixFileDispatcherImpl");
                unmap0 = Jvm.getMethod(dispatcherClass, "unmap0", long.class, long.class);
            } else {
                unmap0 = Jvm.getMethod(FileChannelImpl.class, "unmap0", long.class, long.class);
            }
            // Create a MethodHandle for the unmap0 method
            UNMAPP0_MH = MethodHandles.lookup().unreflect(unmap0);

            Class<?> fdi = Class.forName("sun.nio.ch.FileDispatcherImpl");
            // Create MethodHandles for read0 and write0 methods
            Method read0 = Jvm.getMethod(fdi, "read0", FileDescriptor.class, long.class, int.class);
            READ0_MH = MethodHandles.lookup().unreflect(read0);

            final WriteZero wz = new WriteZero(fdi);
            WRITE0_MH = wz.write0Mh;
            WRITE0_MH2 = wz.write0Mh2;

            // Set TIME_LIMIT exception's stack trace to empty
            TIME_LIMIT.setStackTrace(new StackTraceElement[0]);

        } catch (IllegalAccessException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private OS() {
    }

    /**
     * Finds and loads a class by its fully qualified name using the context class loader of the current thread.
     *
     * @param name The fully qualified name of the class to find.
     * @return The {@link Class} object representing the loaded class.
     * @throws IllegalStateException if the class could not be found.
     */
    private static Class<?> findClass(String name) {
        try {
            // Attempt to load the class using the current thread's context class loader
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(name);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Not found: " + name, e);
        }
    }

    /**
     * Checks if the operating system supports sparse files
     *
     * @return if the OS supports sparse files
     */
    public static boolean isSparseFileSupported() {
        return OS.isLinux() && OS.is64Bit();
    }

    /**
     * Finds the temporary directory path.
     *
     * @return the path of the temporary directory
     */
    static String findTmp() {
        return asRelativePath(findTmp0());
    }

    /**
     * Finds and returns the path to the temporary directory. This method first checks if a project build directory
     * has been specified via the 'project.build.directory' property. If so, it creates and uses a 'tmp' directory within
     * this build directory. If not, it falls back to the system temporary directory ('java.io.tmpdir') if it is writable.
     * Finally, if neither of these options are available, it defaults to a 'tmp' directory in the current working directory.
     *
     * @return The path to the temporary directory.
     */
    private static String findTmp0() {
        // Check for project build directory system property
        String target = Jvm.getProperty("project.build.directory");
        if (target != null) {
            final File tmp = new File(target, "tmp");
            tmp.mkdir(); // Create 'tmp' directory within the build directory
            return tmp.getPath();
        }

        // Fallback to system temporary directory if it is writable
        final String tmp = Jvm.getProperty("java.io.tmpdir");
        if (tmp != null
                && new File(tmp).isDirectory()
                && new File(tmp).canWrite())
            return tmp;
        // Default to a 'tmp' directory in the current working directory
        new File("tmp").mkdirs();
        return "tmp";
    }

    /**
     * Finds the target directory path and returns it as a relative path.
     *
     * @return The relative path to the target directory.
     */
    @NotNull
    private static String findTarget() {
        // Find the absolute path of the target directory
        String path = findTarget0();
        // Convert it to a relative path
        return asRelativePath(path);
    }

    /**
     * Converts an absolute path to a relative path with respect to the user's current working directory.
     * If the path is the same as the user's current directory, it returns ".".
     *
     * @param path The absolute path to convert.
     * @return The relative path.
     */
    @NotNull
    private static String asRelativePath(String path) {
        String userDir = new File(USER_DIR).getAbsolutePath();
        if (!userDir.endsWith(File.separator))
            userDir += File.separator;
        if (path.startsWith(userDir)) {
            if (path.equals(userDir))
                return ".";
            return path.substring(userDir.length());
        }
        return path;
    }

    /**
     * Finds the target directory for the build or temporary files. This method looks for a 'target' directory,
     * typically used by Maven, or a 'build' directory used by Gradle. If neither is found, it defaults to a
     * 'target' directory in the system temporary directory.
     *
     * @return The path to the target directory.
     */
    private static String findTarget0() {
        // Check for project build directory system property
        String target = Jvm.getProperty("project.build.directory");
        if (target != null)
            return target;
        // Traverse up the directory tree to find 'target' or 'build' directories
        for (File dir = new File(Jvm.getProperty("user.dir")); dir != null; dir = dir.getParentFile()) {
            @NotNull File mavenTarget = new File(dir, "target");
            if (mavenTarget.exists())
                return mavenTarget.getAbsolutePath();
            @NotNull File gradleTarget = new File(dir, "build");
            if (gradleTarget.exists())
                return gradleTarget.getAbsolutePath();
        }

        // Default to a 'target' directory in the system temporary directory
        final File dir = new File(Jvm.getProperty("java.io.tmpdir"), "target");
        dir.mkdirs();
        return dir.getPath();
    }

    /**
     * Finds a directory with the specified suffix in the class path.
     *
     * @param suffix the suffix of the directory
     * @return the path of the found directory
     * @throws FileNotFoundException if no directory with the specified suffix is found
     */
    @NotNull
    public static String findDir(@NotNull String suffix) throws FileNotFoundException {
        String[] split = Jvm.getProperty("java.class.path").split(File.pathSeparator);
        for (@NotNull String s : split) {
            if (s.endsWith(suffix) && new File(s).isDirectory())
                return s;
        }
        throw new FileNotFoundException(suffix);
    }

    /**
     * Search a list of directories to find a path which is the last element.
     *
     * @param path of directories to use if found, the last path is always appended.
     * @return the resulting File path.
     */
    @NotNull
    public static File findFile(@NotNull String... path) {
        @NotNull File dir = new File(".").getAbsoluteFile();
        for (int i = 0; i < path.length - 1; i++) {
            @NotNull File dir2 = new File(dir, path[i]);
            if (dir2.isDirectory())
                dir = dir2;
        }
        return new File(dir, path[path.length - 1]);
    }

    /**
     * Returns the host name of the current machine.
     *
     * @return the host name
     */
    public static String getHostName() {
        return HostnameHolder.HOST_NAME;
    }

    /**
     * Returns the IP address of the current machine.
     *
     * @return the IP address
     */
    public static String getIPAddress() {
        return IPAddressHolder.IP_ADDRESS;
    }

    /**
     * Returns the username of the current user.
     *
     * @return the username
     */
    public static String getUserName() {
        return USER_NAME;
    }

    /**
     * Returns the target of the operating system.
     *
     * @return the target
     */
    public static String getTarget() {
        return TARGET;
    }

    /**
     * Returns the temporary directory path.
     *
     * @return the temporary directory path
     */
    public static String getTmp() {
        return TMP;
    }

    /**
     * @return native memory accessor class
     */
    @NotNull
    public static Memory memory() {
        return UnsafeMemory.INSTANCE;
    }

    /**
     * Align the size to page boundary
     *
     * @param size     the size to align
     * @param pageSize
     * @return aligned size
     * @see #pageSize()
     */
    public static long pageAlign(long size, int pageSize) {
        final long mask = pageSize - 1L;
        return (size + mask) & ~mask;
    }

    /**
     * Align the size to page boundary
     *
     * @param size the size to align
     * @return aligned size
     * @see #pageSize()
     */
    public static long pageAlign(long size) {
        return pageAlign(size, pageSize());
    }

    /**
     * @return size of pages
     * @see #pageAlign(long)
     */
    public static int pageSize() {
        if (pageSize == 0)
            pageSize = memory().pageSize();
        return pageSize;
    }

    /**
     * Returns default OS page size.
     */
    public static int defaultOsPageSize() {
        // Windows 10 produces this error for alignment of less than 64K
        // java.io.IOException: The base address or the file offset specified does not have the proper alignment
        // c.f. https://docs.microsoft.com/en-us/windows/win32/memory/creating-a-view-within-a-file
        return isWindows() ? SAFE_PAGE_SIZE : pageSize();
    }
    /**
     * Aligns the specified offset for a memory-mapped file based on the operating system's page size.
     * Memory mapping typically requires that the offset be aligned to the operating system's page size.
     * This method calculates the nearest higher offset that meets this alignment requirement.
     *
     * <p>For example, if the operating system's page size is 4096 bytes and an offset of 6000 is specified,
     * this method would return 8192, as 8192 is the next multiple of 4096 greater than 6000.
     *
     * @param offset the offset to be aligned. It must be non-negative.
     * @return the aligned offset.
     * @throws IllegalArgumentException if offset is negative.
     * @see #mapAlignment()
     */
    public static long mapAlign(long offset) {
        return mapAlign(offset, defaultOsPageSize());
    }

    /**
     * Aligns the specified offset for a memory-mapped file to the nearest higher multiple of the given pageAlignment.
     * This method calculates the nearest higher offset that meets this alignment requirement.
     *
     * <p>For example, if the provided alignment is 2M bytes and an offset of 2.5M is specified,
     * this method would return 4M, as 4M is the next multiple of 2M greater than 2.5M.
     *
     * @param offset        the offset to be aligned. It must be non-negative.
     * @param pageAlignment the alignment size, in bytes. This should typically be the operating system's page size or a multiple thereof.
     * @return the aligned offset.
     * @throws IllegalArgumentException if offset is negative or pageAlignment is non-positive.
     */
    public static long mapAlign(long offset, int pageAlignment) {
        requireNonNegative(offset);
        require(positive(), pageAlignment);

        return (offset + pageAlignment - 1) / pageAlignment * pageAlignment;
    }

    /**
     * Returns the alignment of offsets in file, from which memory mapping could start, based on OS.
     *
     * @return granularity of an offset in a file
     * @see #mapAlign(long)
     */
    public static long mapAlignment() {
        if (mapAlignment == 0)
            mapAlignment = defaultOsPageSize();
        return mapAlignment;
    }

    /**
     * @return is the JVM 64-bit
     */
    public static boolean is64Bit() {
        return IS64BIT;
    }

    /**
     * Determines if the current JVM is running in a 64-bit environment. This method checks various system properties
     * to determine if the JVM is running in 64-bit mode.
     *
     * @return {@code true} if the JVM is running in a 64-bit environment; {@code false} otherwise.
     */
    private static boolean is64Bit0() {
        String systemProp;

        // Check IBM JVM property for bit mode
        systemProp = Jvm.getProperty("com.ibm.vm.bitmode");
        if (systemProp != null) {
            return "64".equals(systemProp);
        }

        // Check Sun JVM property for data model
        systemProp = Jvm.getProperty("sun.arch.data.model");
        if (systemProp != null) {
            return "64".equals(systemProp);
        }

        // Check generic JVM version property for 64-bit indication
        systemProp = Jvm.getProperty("java.vm.version");
        return systemProp != null && systemProp.contains("_64");
    }

    /**
     * Returns the process ID of the current running process.
     *
     * <p>
     * Note: Getting the process ID may be slow if the reserve DNS is not set up correctly.
     * 
     *
     * @return the process ID
     */
    public static int getProcessId() {
        // getting the process id is slow if the reserve DNS is not setup correctly.
        // which is frustrating since we don't actually use the hostname.
        int id = PROCESS_ID.get();
        if (id == 0) {
            id = getProcessId0();
            PROCESS_ID.set(id);
        }
        return id;
    }

    /**
     * Returns the process ID of the current running process.
     *
     * @return the process ID
     */
    static int getProcessId0() {
        @Nullable String pid = null;
        @NotNull final File self = new File(PROC_SELF);
        try {
            if (self.exists())
                pid = self.getCanonicalFile().getName();
        } catch (IOException ignored) {
            // ignored
        }
        if (pid == null)
            pid = getRuntimeMXBean().getName().split("@", 0)[0];
        if (pid != null) {
            try {
                return Integer.parseInt(pid);
            } catch (NumberFormatException e) {
                // ignored
            }
        }
        final int minPid = 2;
        final int rpid = minPid + new SecureRandom().nextInt((1 << 16) - minPid);
        Jvm.warn().on(OS.class, "Unable to determine PID, picked a random number=" + rpid);
        return rpid;
    }

    /**
     * Returns if this JVM runs on the Windows operating system.
     *
     * @return if runs on Windows
     */
    public static boolean isWindows() {
        return Bootstrap.IS_WIN;
    }

    /**
     * Returns if this JVM runs on the MacOS operating system.
     *
     * @return if runs on MacOS
     */
    public static boolean isMacOSX() {
        return Bootstrap.IS_MAC;
    }

    /**
     * Returns if this JVM runs on the Linux operating system.
     *
     * @return if runs on Linux
     */
    public static boolean isLinux() {
        return Bootstrap.IS_LINUX;
    }

    /**
     * @return the maximum PID.
     */
    public static long getPidMax() {
        if (isLinux()) {
            @NotNull File file = new File(PROC_SYS_KERNEL_PID_MAX);
            if (file.canRead())
                try {
                    try (Scanner scanner = new Scanner(file)) {
                        return Maths.nextPower2(scanner.nextLong(), 1);
                    }
                } catch (FileNotFoundException e) {
                    Jvm.debug().on(OS.class, e);
                }
        } else if (isMacOSX()) {
            return 1L << 24;
        }
        // the default.
        return Bootstrap.IS_WIN10 ? 1L << 32 : 1L << 16;
    }

    /**
     * Map a region of a file into memory.
     *
     * @param fileChannel to map
     * @param mode        of access
     * @param start       offset within a file
     * @param size        of region to map.
     * @return the address of the memory mapping.
     * @throws IOException              if the mapping fails
     * @throws IllegalArgumentException if the arguments are not valid
     */
    public static long map(@NotNull FileChannel fileChannel, FileChannel.MapMode mode, long start, long size, int pageSize)
            throws IOException, IllegalArgumentException {
        if (isWindows() && size > 4L << 30)
            throw new IllegalArgumentException("Mapping more than 4096 MiB is unusable on Windows, size = " + (size >> 20) + " MiB");
        final long address = map0(fileChannel, imodeFor(mode), mapAlign(start, pageSize), pageAlign(size, pageSize));
        final long threshold = Math.min(64 * size, 32L << 40);
        if (isLinux() && (address > 0 && address < threshold) && Jvm.is64bit()) {
            double ratio = (double) threshold / address;
            final long durationMs = Math.max(5000, (long) (250 * ratio * ratio * ratio));
            System.err.println("Running low on virtual memory, pausing " + durationMs + " ms, address: " + Long.toUnsignedString(address, 16));
            Jvm.pause(durationMs);
        }
        return address;
    }

    /**
     * Maps a region of a file directly into memory. This method abstracts the details of different memory mapping
     * strategies depending on the operating system and JVM version.
     *
     * @param fileChannel The file channel from which the region is to be mapped.
     * @param mode        The mode in which the file is to be mapped (read-only, read-write, etc.).
     * @param start       The starting position of the region to be mapped.
     * @param size        The size of the region to be mapped.
     * @return The address of the mapped region.
     * @throws IOException If an I/O error occurs.
     */
    public static long map(@NotNull FileChannel fileChannel, FileChannel.MapMode mode, long start, long size)
            throws IOException, IllegalArgumentException {
        return map(fileChannel, mode, start, size, (int) mapAlignment());
    }


    /**
     * Invokes the native method to map a region of a file directly into memory. This method handles different
     * Java versions and operating systems by using reflection and MethodHandles to invoke the appropriate native method.
     *
     * @param map0         The MethodHandle for the native mapping method.
     * @param fileChannel  The file channel from which the region is to be mapped.
     * @param imode        The integer mode representing how the file is to be mapped.
     * @param start        The starting position of the region to be mapped.
     * @param size         The size of the region to be mapped.
     * @param errorHandler A function to handle {@link OutOfMemoryError} exceptions.
     * @return The address of the mapped region.
     * @throws IOException If an I/O error occurs.
     */
    private static long invokeFileChannelMap0(@NotNull MethodHandle map0, @NotNull FileChannel fileChannel, int imode, long start, long size,
                                              @NotNull ThrowingFunction<OutOfMemoryError, Long, IOException> errorHandler) throws IOException {
        try {
            // For now, access is assumed to be non-synchronous
            // TODO - Support passing/deducing synchronous flag externally
            if (Jvm.isJava20Plus()) {
                final FileDescriptor fd = (FileDescriptor) FD_FIELD.get(fileChannel);
                return (long) map0.invokeExact(fd, imode, start, size, false);
            } else if (Jvm.isJava19Plus()) {
                final FileDescriptor fd = (FileDescriptor) FD_FIELD.get(fileChannel);
                return (long) map0.invokeExact((FileChannelImpl) fileChannel, fd, imode, start, size, false);
            } else if (Jvm.isJava14Plus())
                return (long) map0.invokeExact((FileChannelImpl) fileChannel, imode, start, size, false);
            else
                return (long) map0.invokeExact((FileChannelImpl) fileChannel, imode, start, size);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Method map0 is not accessible", e);
        } catch (OutOfMemoryError oom) {
            // Handle out of memory error using provided error handler
            return errorHandler.apply(oom);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    /**
     * Maps a region of a file directly into memory. This is a lower-level method that uses MethodHandles
     * to call the native memory mapping functions and handles potential errors, including out-of-memory conditions.
     *
     * @param fileChannel The file channel from which the region is to be mapped.
     * @param imode       The integer mode representing how the file is to be mapped.
     * @param start       The starting position of the region to be mapped.
     * @param size        The size of the region to be mapped.
     * @return The address of the mapped region.
     * @throws IOException If an I/O error occurs or if the mapping fails.
     */
    static long map0(@NotNull FileChannel fileChannel, int imode, long start, long size) throws IOException {
        MethodHandle map0 = MAP0_MH.get(fileChannel.getClass());

        // Attempt to map the file and handle potential out-of-memory errors
        final long address = invokeFileChannelMap0(map0, fileChannel, imode, start, size, oome1 -> {
            System.gc(); // Suggest garbage collection
            Jvm.pause(100); // Pause briefly to allow GC to free memory

            // Retry mapping and throw IOException if it fails again
            return invokeFileChannelMap0(map0, fileChannel, imode, start, size, oome2 -> {
                throw new IOException("Map failed", oome2);
            });
        });

        // Update the total memory mapped counter
        memoryMapped.addAndGet(size);
        return address;
    }

    /**
     * Unmap a region of memory.
     *
     * @param address of the start of the mapping.
     * @param size    of the region mapped.
     * @throws IOException if the unmap fails.
     */
    public static void unmap(long address, long size, int pageSize) throws IOException {
        try {
            final long size2 = pageAlign(size, pageSize);
            // n must be used here
            final int n = (int) UNMAPP0_MH.invokeExact(address, size2);
            memoryMapped.addAndGet(-size2);
        } catch (Throwable e) {
            throw asAnIOException(e);
        }
    }

    /**
     * Unmaps a memory-mapped region from the virtual memory of the process.
     *
     * @param address The starting address of the memory region to unmap.
     * @param size    The size of the memory region to unmap.
     * @throws IOException If an I/O error occurs during unmapping.
     */
    public static void unmap(long address, long size) throws IOException {
        // Call the overloaded unmap method with the default alignment
        unmap(address, size, (int) mapAlignment());
    }

    /**
     * Returns the number of bytes that are currently memory-mapped.
     *
     * @return The total number of bytes that are memory-mapped.
     */
    public static long memoryMapped() {
        return memoryMapped.get();
    }

    /**
     * Converts a {@link Throwable} into an {@link IOException}. If the throwable is an
     * {@link InvocationTargetException}, its cause is unwrapped. If the throwable is already an
     * {@link IOException}, it is returned as-is; otherwise, it is wrapped in a new {@link IOException}.
     *
     * @param e The throwable to convert.
     * @return An {@link IOException} representing the throwable.
     */
    @NotNull
    private static IOException asAnIOException(Throwable e) {
        if (e instanceof InvocationTargetException)
            e = e.getCause();
        if (e instanceof IOException)
            return (IOException) e;
        return new IOException(e);
    }

    /**
     * Converts a {@link FileChannel.MapMode} to its corresponding integer representation.
     *
     * @param mode The {@link FileChannel.MapMode} to convert.
     * @return An integer representing the map mode.
     */
    static int imodeFor(FileChannel.MapMode mode) {
        int imode = -1;
        if (FileChannel.MapMode.READ_ONLY.equals(mode))
            imode = MAP_RO;
        else if (FileChannel.MapMode.READ_WRITE.equals(mode))
            imode = MAP_RW;
        else if (FileChannel.MapMode.PRIVATE.equals(mode))
            imode = MAP_PV;
        assert (imode >= 0);
        return imode;
    }

    /**
     * Returns the space actually used by a file.
     *
     * @param filename The name of the file to get the actual size of.
     * @return The size in bytes of the space used by the file.
     */
    public static long spaceUsed(@NotNull String filename) {
        return spaceUsed(new File(filename));
    }

    /**
     * Returns the space actually used by a file.
     * For Unix-like systems, this method uses the 'du' command to get the disk usage of the file.
     * On other systems, it simply returns the file length.
     *
     * @param file The file to get the actual size of.
     * @return The size in bytes of the space used by the file.
     */
    private static long spaceUsed(@NotNull File file) {
        if (!isWindows()) {
            try {
                // Execute the 'du' command to get the file size in kilobytes
                final String du = run("du", "-ks", file.getAbsolutePath());
                return Long.parseLong(du.substring(0, du.indexOf('\t')));
            } catch (@NotNull IOException | NumberFormatException e) {
                // Log a warning if the command fails or the output cannot be parsed
                Jvm.warn().on(OS.class, e);
            }
        }
        return file.length();
    }

    /**
     * Executes a command and returns its output as a string.
     *
     * @param cmds The command and arguments to execute.
     * @return The output of the command.
     * @throws IOException If an I/O error occurs during command execution.
     */
    private static String run(String... cmds) throws IOException {
        @NotNull ProcessBuilder pb = new ProcessBuilder(cmds);
        pb.redirectErrorStream(true); // Redirect error stream to the standard output
        Process process = pb.start(); // Start the process

        @NotNull StringWriter sw = new StringWriter();
        char @NotNull [] chars = new char[1024];
        try (@NotNull Reader r = new InputStreamReader(process.getInputStream())) {
            // Read the process output into a StringWriter
            for (int len; (len = r.read(chars)) > 0; ) {
                sw.write(chars, 0, len);
            }
        }
        return sw.toString();
    }

    /**
     * Returns the current working directory of the user.
     *
     * @return The user's current working directory.
     */
    public static String userDir() {
        return USER_DIR;
    }

    /**
     * Reads data from a file descriptor into a memory address.
     *
     * @param fd     The file descriptor to read from.
     * @param address The memory address to read into.
     * @param len     The number of bytes to read.
     * @return The number of bytes read.
     * @throws IOException If an I/O error occurs.
     */
    public static int read0(FileDescriptor fd, long address, int len) throws IOException {
        try {
            return (int) READ0_MH.invokeExact(fd, address, len);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    /**
     * Writes data from a memory address to a file descriptor.
     *
     * @param fd     The file descriptor to write to.
     * @param address The memory address to write from.
     * @param len     The number of bytes to write.
     * @return The number of bytes written.
     * @throws IOException If an I/O error occurs.
     */
    public static int write0(FileDescriptor fd, long address, int len) throws IOException {
        try {
            if (WRITE0_MH2 == null)
                return (int) WRITE0_MH.invokeExact(fd, address, len);
            else
                return (int) WRITE0_MH2.invokeExact(fd, address, len, false);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    /**
     * Checks if a string is not null and not empty.
     *
     * @param s The string to check.
     * @return {@code true} if the string is not null and not empty; {@code false} otherwise.
     */
    private static boolean isSet(String s) {
        return !(s == null || s.isEmpty());
    }

    /**
     * A utility class to provide MethodHandles for the 'write0' method on the FileDispatcherImpl class.
     * This class attempts to find and unreflect the correct 'write0' method signature based on availability.
     */
    private static final class WriteZero {
        private MethodHandle write0Mh = null;
        private MethodHandle write0Mh2 = null;

        /**
         * Constructs a new WriteZero utility object for accessing the 'write0' method.
         *
         * @param fdi The FileDispatcherImpl class to reflect upon.
         * @throws IllegalAccessException If the method cannot be accessed.
         */
        public WriteZero(final Class<?> fdi) throws IllegalAccessException {
            try {
                // Attempt to get the single-parameter 'write0' method
                Method write0 = Jvm.getMethod(fdi, "write0", FileDescriptor.class, long.class, int.class);
                write0Mh = MethodHandles.lookup().unreflect(write0);
            } catch (AssertionError ae) {
                // Fallback to getting the two-parameter 'write0' method
                Method write0 = Jvm.getMethod(fdi, "write0", FileDescriptor.class, long.class, int.class, boolean.class);
                write0Mh2 = MethodHandles.lookup().unreflect(write0);
            }
        }
    }

    @Deprecated(/* to be moved in x.26 to an internal package of Chronicle-Bytes */)
    public static final class Unmapper implements Runnable {
        private final long size;

        private final int pageSize;

        private volatile long address;

        public Unmapper(long address, long size, int pageSize) throws IllegalStateException {

            assert (address != 0);
            this.address = address;
            this.size = size;
            this.pageSize = pageSize;
        }

        @Override
        public void run() {
            if (address == 0)
                return;

            try {
                unmap(address, size, pageSize);
                address = 0;

            } catch (@NotNull IOException e) {
                Jvm.warn().on(OS.class, "Error on unmap and release", e);
            }
        }
    }

    /**
     * A utility class for retrieving the IP address of the local machine.
     * This class attempts to determine the IP address using multiple strategies,
     * including using the local host address, creating a socket connection to an external server,
     * and using a datagram socket to connect to a known IP address.
     */
    static class IPAddressHolder {

        /**
         * A constant representing no address.
         */
        public static final String NO_ADDRESS = "0.0.0.0";

        /**
         * The detected IP address of the local machine.
         */
        static final String IP_ADDRESS = getIPAddress0();

        /**
         * Determines the IP address of the local machine using various methods.
         *
         * @return The IP address as a string, or "0.0.0.0" if no address could be determined.
         */
        static String getIPAddress0() {
            // Try to get IP address using localhost
            String addr = getIpAddressByLocalHost();
            if (isSet(addr))
                return addr;
            // Try to get IP address using a datagram socket
            addr = getIpAddressByDatagram();
            if (isSet(addr))
                return addr;
            // Try to get IP address using a TCP socket
            addr = getIpAddressBySocket();
            if (isSet(addr))
                return addr;
            // Return no address if all methods fail
            return NO_ADDRESS;
        }

        /**
         * Checks if a given string is set and is not equal to NO_ADDRESS.
         *
         * @param s The string to check.
         * @return {@code true} if the string is not null, not empty, and not equal to NO_ADDRESS;
         *         {@code false} otherwise.
         */
        static boolean isSet(String s) {
            return !(s == null || s.isEmpty() || s.equals(NO_ADDRESS));
        }

        /**
         * Attempts to get the local IP address by using the local host's InetAddress.
         *
         * @return The local IP address as a string, or an empty string if it cannot be determined.
         */
        static String getIpAddressByLocalHost() {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (Throwable e) {
                // Return an empty string if an exception occurs
                return "";
            }
        }

        /**
         * Attempts to get the local IP address by creating a socket connection to an external server.
         *
         * @return The local IP address as a string, or an empty string if it cannot be determined.
         */
        static String getIpAddressBySocket() {
            try {
                try (Socket socket = new Socket()) {
                    // Connect to an external server to determine local IP address
                    socket.connect(new InetSocketAddress("google.com", 80));
                    return socket.getLocalAddress().getHostAddress();
                }
            } catch (Throwable e) {
                // Return an empty string if an exception occurs
                return "";
            }
        }

        /**
         * Attempts to get the local IP address by using a datagram socket to connect to a known IP address.
         *
         * @return The local IP address as a string, or {@code null} if it cannot be determined.
         */
        static String getIpAddressByDatagram() {
            try {
                try (final DatagramSocket socket = new DatagramSocket()) {
                    // Connect to a known IP address to determine local IP address
                    socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                    return socket.getLocalAddress().getHostAddress();
                }
            } catch (Throwable e) {
                // Return null if an exception occurs
                return null;
            }
        }
    }

    /**
     * A utility class for retrieving the hostname of the local machine.
     * This class attempts to determine the hostname using the system environment variable,
     * the local host's InetAddress, or by executing the 'hostname' command.
     */
    static class HostnameHolder {

        /**
         * The detected hostname of the local machine.
         */
        static final String HOST_NAME = getHostName0();

        /**
         * Determines the hostname of the local machine using various methods.
         *
         * @return The hostname as a string.
         */
        private static String getHostName0() {
            // Check if the operating system is Windows
            if (isWindows()) {
                String computerName = System.getenv().get("COMPUTERNAME");
                if (isSet(computerName))
                    return computerName.toLowerCase();
            }
            try {
                // Try to get the hostname using the local host's InetAddress
                return InetAddress.getLocalHost().getHostName();
            } catch (Throwable e) {
                try {
                    // Fallback to executing the 'hostname' command
                    return execHostname();
                } catch (IOException ioe) {
                    // Return "localhost" if all methods fail
                    return "localhost";
                }
            }
        }

        /**
         * Executes the 'hostname' command to get the hostname of the local machine.
         *
         * @return The hostname as a string.
         * @throws IOException If an I/O error occurs during command execution.
         */
        static String execHostname() throws IOException {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            Runtime.getRuntime().exec("hostname")
                                    .getInputStream()))) {
                return br.readLine();
            }
        }
    }
}
