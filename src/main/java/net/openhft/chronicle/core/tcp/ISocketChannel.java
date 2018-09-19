package net.openhft.chronicle.core.tcp;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.annotation.NotNull;
import net.openhft.chronicle.core.io.IORuntimeException;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface ISocketChannel extends Closeable {

    boolean FAST_JAVA8_IO = isFastJava8IO();

    static boolean isFastJava8IO() {
        boolean fastJava8IO = Boolean.getBoolean("fastJava8IO") && !Jvm.isJava9Plus() && OS.isLinux();
        if (fastJava8IO) System.out.println("FastJava8IO: " + fastJava8IO);
        return fastJava8IO;
    }

    @NotNull
    static ISocketChannel wrap(SocketChannel sc) {
        assert sc != null;
        return FAST_JAVA8_IO ? new FastJ8SocketChannel(sc) : new VanillaSocketChannel(sc);
    }

    @NotNull
    static ISocketChannel wrapUnsafe(SocketChannel sc) {
        assert sc != null;
        return FAST_JAVA8_IO ? new UnsafeFastJ8SocketChannel(sc) : new VanillaSocketChannel(sc);
    }

    @NotNull
    SocketChannel socketChannel();

    int read(ByteBuffer byteBuffer) throws IOException;

    int write(ByteBuffer byteBuffer) throws IOException;

    long write(ByteBuffer[] byteBuffer) throws IOException;

    @NotNull
    Socket socket();

    void configureBlocking(boolean blocking) throws IOException;

    @NotNull
    InetSocketAddress getRemoteAddress() throws IORuntimeException;

    @NotNull
    InetSocketAddress getLocalAddress() throws IORuntimeException;

    boolean isOpen();

    boolean isBlocking();
}
