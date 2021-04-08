package net.openhft.chronicle.core.resource;

import net.openhft.chronicle.core.util.Builder;

public interface ManagedResourceSupportBuilder extends Builder<ManagedResourceSupport> {

    ManagedResourceSupportBuilder withResourceTracing(boolean resourceTracing);

    ManagedResourceSupportBuilder withOwnerThread(Thread ownerThread);

    ManagedResourceSupportBuilder withDiscardWarnings(boolean discardWarnings);

    ManagedResourceSupportBuilder withWarningTimeNs(long warningTimeNs);

    ManagedResourceSupportBuilder addCloseHandler(Runnable closeHandler);

    @Override
    ManagedResourceSupport build();

}
