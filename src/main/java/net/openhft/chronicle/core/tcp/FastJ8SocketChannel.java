package net.openhft.chronicle.core.tcp;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import sun.nio.ch.DirectBuffer;
import sun.nio.ch.IOStatus;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class FastJ8SocketChannel extends VanillaSocketChannel {
    private final FileDescriptor fd;
    private final AtomicBoolean readLock = new AtomicBoolean();
    private volatile boolean open, blocking;

    FastJ8SocketChannel(SocketChannel socketChannel) {
        super(socketChannel);
        fd = Jvm.getValue(socketChannel, "fd");
        open = socketChannel.isOpen();
        blocking = socketChannel.isBlocking();
    }

    @Override
    public int read(ByteBuffer buf) throws IOException {
        if (isBlocking() || !isOpen() || !(buf instanceof DirectBuffer))
            return super.read(buf);
        return read0(buf);
    }

    @Override
    public void configureBlocking(boolean blocking) throws IOException {
        super.configureBlocking(blocking);
        this.blocking = super.isBlocking();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean isBlocking() {
        return blocking;
    }

    @Override
    public void close() throws IOException {
        super.close();
        open = super.isOpen();
    }

    @Override
    public int write(ByteBuffer byteBuffer) throws IOException {
        try {
            int write = super.write(byteBuffer);
            open &= write >= 0;
            return write;

        } catch (Exception e) {
            open = super.isOpen();
            throw e;
        }
    }

    private int read0(ByteBuffer buf) throws IOException {

        if (buf == null)
            throw new NullPointerException();

        try {
            while (true) {
                if (readLock.compareAndSet(false, true))
                    break;
                if (Thread.interrupted())
                    throw new IOException(new InterruptedException());
                if (!isOpen())
                    return -1;
            }

            int n = OS.read0(fd, ((DirectBuffer) buf).address() + buf.position(), buf.remaining());
            if ((n == IOStatus.INTERRUPTED) && isOpen()) {
                // The system call was interrupted but the channel
                // is still open, so retry
                return 0;
            }
            int ret = IOStatus.normalize(n);
            if (ret > 0)
                buf.position(buf.position() + ret);
            else if (ret < 0)
                open = false;
            return ret;

        } finally {
            readLock.compareAndSet(true, false);
        }
    }
}
