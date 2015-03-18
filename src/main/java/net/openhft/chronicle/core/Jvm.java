package net.openhft.chronicle.core;

public enum Jvm {
    ;

    public static void checkInterrupted() throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
    }
}
