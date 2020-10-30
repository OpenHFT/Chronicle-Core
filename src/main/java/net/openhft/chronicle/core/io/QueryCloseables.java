package net.openhft.chronicle.core.io;

// NOT Used anywhere
@Deprecated
public enum QueryCloseables implements QueryCloseable {
    NEVER_CLOSED {
        @Override
        public boolean isClosed() {
            return false;
        }
    }
}
