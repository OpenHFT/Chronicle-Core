package net.openhft.chronicle.core.io;

public enum QueryCloseables implements QueryCloseable {
    NEVER_CLOSED {
        @Override
        public boolean isClosed() {
            return false;
        }
    }
}
