/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
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

package net.openhft.chronicle.core.threads;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

public class OnDemandEventLoop implements EventLoop {
    private final Supplier<EventLoop> eventLoopSupplier;
    private volatile EventLoop eventLoop;

    public OnDemandEventLoop(@NotNull final Supplier<EventLoop> eventLoopSupplier) {
        this.eventLoopSupplier = requireNonNull(eventLoopSupplier);
    }

    EventLoop eventLoop() {
        EventLoop el = this.eventLoop;
        if (el != null)
            return el;
        synchronized (this) {
            el = this.eventLoop;
            if (el != null)
                return el;
            eventLoop = eventLoopSupplier.get();
            return eventLoop;
        }
    }

    public boolean hasEventLoop() {
        return eventLoop != null;
    }

    @Override
    public String name() {
        return eventLoop().name();
    }

    @Override
    public void addHandler(EventHandler handler) {
        eventLoop().addHandler(handler);
    }

    @Override
    public void start() {
        eventLoop().start();
    }

    @Override
    public void unpause() {
        if (hasEventLoop())
            eventLoop().unpause();
    }

    @Override
    public void stop() {
        if (hasEventLoop())
            eventLoop().stop();
    }

    @Override
    public boolean isClosed() {
        return !hasEventLoop() || eventLoop().isClosed();
    }

    @Override
    public boolean isAlive() {
        return hasEventLoop() && eventLoop().isAlive();
    }

    @Override
    public boolean isStopped() {
        final EventLoop el = this.eventLoop;
        return el != null && el.isStopped();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void awaitTermination() {
        if (hasEventLoop())
            eventLoop().awaitTermination();
    }

    @Override
    public void close() {
        if (hasEventLoop())
            eventLoop().close();
    }
}
