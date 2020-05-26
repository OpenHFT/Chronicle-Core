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

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import org.jetbrains.annotations.NotNull;
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
