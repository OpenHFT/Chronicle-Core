/*
 * Copyright 2016 higherfrequencytrading.com
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class CloseablesManager implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(CloseablesManager.class.getName());

    private final List<Closeable> closeables = new ArrayList<>();

    public synchronized void add(Closeable closeable) {
        if (closeable == null)
            throw new NullPointerException();
        if (!closeables.contains(closeable))
            closeables.add(closeable);
    }

    public synchronized void close(Closeable closeable) throws IOException {
        closeables.remove(closeable);
        closeable.close();
    }

    public synchronized void closeQuietly(Closeable closeable) {
        try {
            close(closeable);
        } catch (IOException e) {
            Jvm.warn().on(getClass(), e);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        IOException ex = null;
        for (int i = closeables.size() - 1; i >= 0; i--) {
            Closeable closeable = closeables.get(i);
            try {
                closeable.close();
            } catch (NullPointerException e) {
                // at java.nio.channels.spi.AbstractSelectableChannel.removeKey
                // (AbstractSelectableChannel.java:129) is throwing a NULL on close
                Jvm.warn().on(getClass(), e);
            } catch (IOException e) {
                Jvm.warn().on(getClass(), e);
                ex = e;
            }
        }
        closeables.clear();

        if (ex != null)
            throw ex;
    }

    public synchronized void closeQuietly() {
        net.openhft.chronicle.core.io.Closeable.closeQuietly(this);
    }

    public synchronized boolean isEmpty() {
        return closeables.isEmpty();
    }
}
