package net.openhft.chronicle.core.tcp;

import net.openhft.chronicle.core.io.IORuntimeException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class VanillaSocketChannel implements ISocketChannel {
    protected final SocketChannel socketChannel;

    VanillaSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public SocketChannel socketChannel() {
        return socketChannel;
    }

    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        return socketChannel.read(byteBuffer);
    }

    @Override
    public int write(ByteBuffer byteBuffer) throws IOException {
        return socketChannel.write(byteBuffer);
    }

    @Override
    public long write(ByteBuffer[] byteBuffer) throws IOException {
        return socketChannel.write(byteBuffer);
    }

    @Override
    public Socket socket() {
        return socketChannel.socket();
    }

    @Override
    public void configureBlocking(boolean blocking) throws IOException {
        socketChannel.configureBlocking(blocking);
    }

    @Override
    public InetSocketAddress getRemoteAddress() throws IORuntimeException {
        try {
            return (InetSocketAddress) socketChannel.getRemoteAddress();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public InetSocketAddress getLocalAddress() throws IORuntimeException {
        try {
            return (InetSocketAddress) socketChannel.getLocalAddress();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public boolean isOpen() {
        return socketChannel.isOpen();
    }

    @Override
    public boolean isBlocking() {
        return socketChannel.isBlocking();
    }

    @Override
    public void close() throws IOException {
        socketChannel.close();
    }
}
