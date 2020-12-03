package net.openhft.chronicle.core.io;

// NOT Used anywhere
@Deprecated(/* to be removed in x.22 */)
public enum QueryCloseables implements QueryCloseable {
    NEVER_CLOSED {
        @Override
        public boolean isClosed() {
            return false;
        }
    }
}
