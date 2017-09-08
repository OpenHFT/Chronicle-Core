package net.openhft.chronicle.core.tcp;


import net.openhft.chronicle.core.annotation.NotNull;
import net.openhft.chronicle.core.io.IORuntimeException;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface ISocketChannel extends Closeable {

    @NotNull
    static ISocketChannel wrap(SocketChannel sc) {
        assert sc != null;
        return new VanillaSocketChannel(sc);
    }

    @NotNull
    SocketChannel socketChannel();

    int read(ByteBuffer byteBuffer) throws IOException;

    int write(ByteBuffer byteBuffer) throws IOException;

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
