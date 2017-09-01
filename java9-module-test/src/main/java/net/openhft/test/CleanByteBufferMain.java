package net.openhft.test;

import java.nio.ByteBuffer;
import net.openhft.chronicle.core.cleaner.CleanerServiceLocator;

public final class CleanByteBufferMain {
    public static void main(String[] args) {
        System.out.println("Cleaning a ByteBuffer...");
        CleanerServiceLocator.cleanerService().clean(ByteBuffer.allocateDirect(64));
        System.out.println("Cleaned a ByteBuffer");
    }
}
