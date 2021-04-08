package net.openhft.chronicle.core.resource;


import net.openhft.chronicle.core.io.Closeable;

public final class FooResource implements Closeable {

    private final StringBuilder internalResource;
    private final ManagedResourceSupport support;

    public FooResource() {
        this.internalResource = new StringBuilder().append("Long text...");
        this.support = ManagedResourceSupport.builder()
                .withOwnerThread(Thread.currentThread())
                .addCloseHandler(this::close)
                .build();
    }

    public String compute() {
        support.assertOpen();
        support.assertThreadConfined();
        return internalResource.toString();
    }

    @Override
    public void close() {
        support.assertThreadConfined();
        // idempotency
        if (support.closeIfOpen())
            internalResource.setLength(0);
    }

    @Override
    public boolean isClosed() {
        return support.is(ManagedResourceSupport.State.CLOSED);
    }

    @Override
    public boolean isClosing() {
        return support.is(ManagedResourceSupport.State.CLOSING);
    }
}
