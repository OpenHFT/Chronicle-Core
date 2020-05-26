/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    @Override
    public String toString() {
        return "VanillaSocketChannel{" +
                "socketChannel=" + socketChannel +
                '}';
    }
}
