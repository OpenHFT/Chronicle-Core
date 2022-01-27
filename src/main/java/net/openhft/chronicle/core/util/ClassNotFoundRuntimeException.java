package net.openhft.chronicle.core.util;

public class ClassNotFoundRuntimeException extends RuntimeException {
    public ClassNotFoundRuntimeException(ClassNotFoundException cause) {
        super(cause);
    }

    @SuppressWarnings("squid:S3551")
    @Override
    public ClassNotFoundException getCause() {
        return (ClassNotFoundException) super.getCause();
    }
}
