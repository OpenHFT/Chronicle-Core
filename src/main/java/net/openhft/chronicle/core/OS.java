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

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.ThrowingFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.nio.ch.FileChannelImpl;

import javax.naming.TimeLimitExceededException;
import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
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

/**
 * Low level access to OS class.
 */
public final class OS {

    public static final String TMP = findTmp();
    public static final String USER_DIR = Jvm.getProperty("user.dir");
    public static final String USER_HOME = Jvm.getProperty("user.home");
    public static final Exception TIME_LIMIT = new TimeLimitExceededException();
    public static final int SAFE_PAGE_SIZE = 64 << 10;
    static final ClassLocal<MethodHandle> MAP0_MH = ClassLocal.withInitial(c -> {
        try {
            Method map0;
            if (Jvm.isJava14Plus()) map0 = Jvm.getMethod(c, "map0", int.class, long.class, long.class, boolean.class);
            else map0 = Jvm.getMethod(c, "map0", int.class, long.class, long.class);
            return MethodHandles.lookup().unreflect(map0);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    });
    private static final String TARGET = findTarget();
    private static final String USER_NAME = Jvm.getProperty("user.name");
    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int MAP_PV = 2;
    private static final boolean IS64BIT = is64Bit0();
    private static final AtomicInteger PROCESS_ID = new AtomicInteger();
    private static final String OS_NAME = Jvm.getProperty("os.name").toLowerCase();
    private static final boolean IS_LINUX = OS_NAME.startsWith("linux");
    private static final boolean IS_MAC = OS_NAME.contains("mac");
    private static final boolean IS_WIN = OS_NAME.startsWith("win");
    private static final boolean IS_WIN10 = OS_NAME.equals("windows 10");
    private static final AtomicLong memoryMapped = new AtomicLong();
    private static final MethodHandle UNMAPP0_MH;
    private static final MethodHandle READ0_MH;
    private static final MethodHandle WRITE0_MH;
    private static final MethodHandle WRITE0_MH2;
    private static final String PROC_SELF = "/proc/self";
    private static final String PROC_SYS_KERNEL_PID_MAX = "/proc/sys/kernel/pid_max";
    private static int pageSize; // avoid circular initialisation
    private static int mapAlignment;

    static {
        // make sure it is initialised first.
        Jvm.debug();
        try {
            Method unmap0 = Jvm.getMethod(FileChannelImpl.class, "unmap0", long.class, long.class);
            UNMAPP0_MH = MethodHandles.lookup().unreflect(unmap0);

            Class<?> fdi = Class.forName("sun.nio.ch.FileDispatcherImpl");
            Method read0 = Jvm.getMethod(fdi, "read0", FileDescriptor.class, long.class, int.class);
            READ0_MH = MethodHandles.lookup().unreflect(read0);

            final WriteZero wz = new WriteZero(fdi);
            WRITE0_MH = wz.write0Mh;
            WRITE0_MH2 = wz.write0Mh2;

            TIME_LIMIT.setStackTrace(new StackTraceElement[0]);

        } catch (IllegalAccessException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    // Suppresses default constructor, ensuring non-instantiability.
    private OS() {
    }

    public static boolean isSparseFileSupported() {
        return OS.isLinux() && OS.is64Bit();
    }

    private static String findTmp() {
        String target = Jvm.getProperty("project.build.directory");
        if (target != null) {
            final File tmp = new File(target, "tmp");
            tmp.mkdir();
            return tmp.getPath();
        }
        final String tmp = Jvm.getProperty("java.io.tmpdir");
        if (tmp != null
                && new File(tmp).isDirectory()
                && new File(tmp).canWrite())
            return tmp;
        new File("tmp").mkdirs();
        return "tmp";
    }

    @NotNull
    private static String findTarget() {
        String target = Jvm.getProperty("project.build.directory");
        if (target != null)
            return target;
        for (File dir = new File(Jvm.getProperty("user.dir")); dir != null; dir = dir.getParentFile()) {
            @NotNull File mavenTarget = new File(dir, "target");
            if (mavenTarget.exists())
                return mavenTarget.getAbsolutePath();
            @NotNull File gradleTarget = new File(dir, "build");
            if (gradleTarget.exists())
                return gradleTarget.getAbsolutePath();
        }
        final File dir = new File(Jvm.getProperty("java.io.tmpdir"), "target");
        dir.mkdirs();
        return dir.getPath();
    }

    @NotNull
    public static String findDir(@NotNull String suffix) throws FileNotFoundException {
        for (@NotNull String s : Jvm.getProperty("java.class.path").split(":")) {
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

    public static String getHostName() {
        return HostnameHolder.HOST_NAME;
    }

    public static String getIPAddress() {
        return IPAddressHolder.IP_ADDRESS;
    }

    public static String getUserName() {
        return USER_NAME;
    }

    public static String getTarget() {
        return TARGET;
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
     * @param size the size to align
     * @return aligned size
     * @see #pageSize()
     */
    public static long pageAlign(long size) {
        final long mask = pageSize() - 1L;
        return (size + mask) & ~mask;
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
     * Align an offset of a memory mapping in file based on OS.
     *
     * @param offset to align
     * @return offset aligned
     * @see #mapAlignment()
     */
    public static long mapAlign(long offset) {
        int chunkMultiple = (int) mapAlignment();
        return (offset + chunkMultiple - 1) / chunkMultiple * chunkMultiple;
    }

    /**
     * Returns the alignment of offsets in file, from which memory mapping could start, based on
     * OS.
     *
     * @return granularity of an offset in a file
     * @see #mapAlign(long)
     */
    public static long mapAlignment() {
        if (mapAlignment == 0)
            // Windows 10 produces this error for alignment of less than 64K
            // java.io.IOException: The base address or the file offset specified does not have the proper alignment
            // c.f. https://docs.microsoft.com/en-us/windows/win32/memory/creating-a-view-within-a-file
            mapAlignment = isWindows() ? SAFE_PAGE_SIZE : pageSize();
        return mapAlignment;
    }

    /**
     * @return is the JVM 64-bit
     */
    public static boolean is64Bit() {
        return IS64BIT;
    }

    private static boolean is64Bit0() {
        String systemProp;
        systemProp = Jvm.getProperty("com.ibm.vm.bitmode");
        if (systemProp != null) {
            return "64".equals(systemProp);
        }
        systemProp = Jvm.getProperty("sun.arch.data.model");
        if (systemProp != null) {
            return "64".equals(systemProp);
        }
        systemProp = Jvm.getProperty("java.vm.version");
        return systemProp != null && systemProp.contains("_64");
    }

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

    private static int getProcessId0() {
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
        return IS_WIN;
    }

    /**
     * Returns if this JVM runs on the MacOS operating system.
     *
     * @return if runs on MacOS
     */
    public static boolean isMacOSX() {
        return IS_MAC;
    }

    /**
     * Returns if this JVM runs on the Linux operating system.
     *
     * @return if runs on Linux
     */
    public static boolean isLinux() {
        return IS_LINUX;
    }

    /**
     * @return the maximum PID.
     * @throws NumberFormatException if ?
     * @throws AssertionError        if ?
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
                } catch (IllegalArgumentException e) {
                    throw new AssertionError(e);
                }
        } else if (isMacOSX()) {
            return 1L << 24;
        }
        // the default.
        return IS_WIN10 ? 1L << 32 : 1L << 16;
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
    public static long map(@NotNull FileChannel fileChannel, FileChannel.MapMode mode, long start, long size)
            throws IOException, IllegalArgumentException {
        if (isWindows() && size > 4L << 30)
            throw new IllegalArgumentException("Mapping more than 4096 MiB is unusable on Windows, size = " + (size >> 20) + " MiB");
        final long address = map0(fileChannel, imodeFor(mode), mapAlign(start), pageAlign(size));
        final long threshold = Math.min(64 * size, 32L << 40);
        if (isLinux() && (address > 0 && address < threshold) && Jvm.is64bit()) {
            double ratio = (double) threshold / address;
            final long durationMs = Math.max(5000, (long) (250 * ratio * ratio * ratio));
            System.err.println("Running low on virtual memory, pausing " + durationMs + " ms, address: " + Long.toUnsignedString(address, 16));
            Jvm.pause(durationMs);
        }
        return address;
    }

    private static long invokeFileChannelMap0(@NotNull MethodHandle map0, @NotNull FileChannel fileChannel, int imode, long start, long size,
                                              @NotNull ThrowingFunction<OutOfMemoryError, Long, IOException> errorHandler) throws IOException {
        try {
            // For now, access is assumed to be non-synchronous
            // TODO - Support passing/deducing synchronous flag externally
            if (Jvm.isJava14Plus())
                return (long) map0.invokeExact((FileChannelImpl) fileChannel, imode, start, size, false);
            else
                return (long) map0.invokeExact((FileChannelImpl) fileChannel, imode, start, size);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Method map0 is not accessible", e);
        } catch (OutOfMemoryError oom) {
            return errorHandler.apply(oom);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    static long map0(@NotNull FileChannel fileChannel, int imode, long start, long size) throws IOException {
        MethodHandle map0 = MAP0_MH.get(fileChannel.getClass());
        final long address = invokeFileChannelMap0(map0, fileChannel, imode, start, size, oome1 -> {
            System.gc();
            Jvm.pause(100);

            return invokeFileChannelMap0(map0, fileChannel, imode, start, size, oome2 -> {
                throw new IOException("Map failed", oome2);
            });
        });
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
    public static void unmap(long address, long size) throws IOException {
        try {
            final long size2 = pageAlign(size);
            // n must be used here
            final int n = (int) UNMAPP0_MH.invokeExact(address, size2);
            memoryMapped.addAndGet(-size2);
        } catch (Throwable e) {
            throw asAnIOException(e);
        }
    }

    /**
     * Returns the number of bytes that is memory mapped.
     *
     * @return bytes memory mapped
     */
    public static long memoryMapped() {
        return memoryMapped.get();
    }

    @NotNull
    private static IOException asAnIOException(Throwable e) {
        if (e instanceof InvocationTargetException)
            e = e.getCause();
        if (e instanceof IOException)
            return (IOException) e;
        return new IOException(e);
    }

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
     * Get the space actually used by a file.
     *
     * @param filename to get the actual size of
     * @return size in bytes.
     */
    public static long spaceUsed(@NotNull String filename) {
        return spaceUsed(new File(filename));
    }

    private static long spaceUsed(@NotNull File file) {
        if (!isWindows()) {
            try {
                final String du = run("du", "-ks", file.getAbsolutePath());
                return Long.parseLong(du.substring(0, du.indexOf('\t')));
            } catch (@NotNull IOException | NumberFormatException e) {
                Jvm.warn().on(OS.class, e);
            }
        }
        return file.length();
    }

    private static String run(String... cmds) throws IOException {
        @NotNull ProcessBuilder pb = new ProcessBuilder(cmds);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        @NotNull StringWriter sw = new StringWriter();
        @NotNull char[] chars = new char[1024];
        try (@NotNull Reader r = new InputStreamReader(process.getInputStream())) {
            for (int len; (len = r.read(chars)) > 0; ) {
                sw.write(chars, 0, len);
            }
        }
        return sw.toString();
    }

    public static String userDir() {
        return USER_DIR;
    }

    public static int read0(FileDescriptor fd, long address, int len) throws IOException {
        try {
            return (int) READ0_MH.invokeExact(fd, address, len);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

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

    private static boolean isSet(String s) {
        return !(s == null || s.isEmpty());
    }

    private static final class WriteZero {
        private MethodHandle write0Mh = null;
        private MethodHandle write0Mh2 = null;

        public WriteZero(final Class<?> fdi) throws IllegalAccessException {
            try {
                Method write0 = Jvm.getMethod(fdi, "write0", FileDescriptor.class, long.class, int.class);
                write0Mh = MethodHandles.lookup().unreflect(write0);
            } catch (AssertionError ae) {
                Method write0 = Jvm.getMethod(fdi, "write0", FileDescriptor.class, long.class, int.class, boolean.class);
                write0Mh2 = MethodHandles.lookup().unreflect(write0);
            }
        }
    }

    public static final class Unmapper implements Runnable {
        private final long size;

        private volatile long address;

        public Unmapper(long address, long size) throws IllegalStateException {

            assert (address != 0);
            this.address = address;
            this.size = size;
        }

        @Override
        public void run() {
            if (address == 0)
                return;

            try {
                unmap(address, size);
                address = 0;

            } catch (@NotNull IOException e) {
                Jvm.warn().on(OS.class, "Error on unmap and release", e);
            }
        }
    }

    static class IPAddressHolder {
        public static final String NO_ADDRESS = "0.0.0.0";
        static final String IP_ADDRESS = getIPAddress0();

        static String getIPAddress0() {
            String addr = getIpAddressByLocalHost();
            if (isSet(addr))
                return addr;
            addr = getIpAddressByDatagram();
            if (isSet(addr))
                return addr;
            addr = getIpAddressBySocket();
            if (isSet(addr))
                return addr;
            return NO_ADDRESS;
        }

        static boolean isSet(String s) {
            return !(s == null || s.isEmpty() || s.equals(NO_ADDRESS));
        }

        static String getIpAddressByLocalHost() {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (Throwable e) {
                return "";
            }
        }

        static String getIpAddressBySocket() {
            try {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress("google.com", 80));
                    return socket.getLocalAddress().getHostAddress();
                }
            } catch (Throwable e) {
                return "";
            }
        }

        static String getIpAddressByDatagram() {
            try {
                try (final DatagramSocket socket = new DatagramSocket()) {
                    socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                    return socket.getLocalAddress().getHostAddress();
                }
            } catch (Throwable e) {
                return null;
            }
        }
    }

    static class HostnameHolder {
        static final String HOST_NAME = getHostName0();

        private static String getHostName0() {
            if (isWindows()) {
                String computerName = System.getenv().get("COMPUTERNAME");
                if (isSet(computerName))
                    return computerName.toLowerCase();
            }
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (Throwable e) {
                try {
                    return execHostname();
                } catch (IOException ioe) {
                    return "localhost";
                }
            }
        }

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
