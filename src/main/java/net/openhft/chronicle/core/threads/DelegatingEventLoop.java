package net.openhft.chronicle.core.threads;

import org.jetbrains.annotations.NotNull;

public class DelegatingEventLoop implements EventLoop {
    @NotNull
    private final EventLoop inner;

    public DelegatingEventLoop(@NotNull EventLoop eventLoop) {
        this.inner = eventLoop;
    }

    @Override
    public String name() {
        return inner.name();
    }

    @Override
    public void start() {
        inner.start();
    }

    @Override
    public void unpause() {
        inner.unpause();
    }

    @Override
    public void stop() {
        inner.stop();
    }

    @Override
    public boolean isClosed() {
        return inner.isClosed();
    }

    @Override
    public boolean isStopped() {
        return inner.isStopped();
    }

    @Override
    public boolean isClosing() {
        return inner.isClosing();
    }

    @Override
    public boolean isAlive() {
        return inner.isAlive();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void awaitTermination() {
        inner.awaitTermination();
    }

    @Override
    public void close() {
        inner.close();
    }

    @Override
    public void addHandler(EventHandler handler) {
        inner.addHandler(handler);
    }
}
