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
