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


/*

Command-line:

java -cp ~/.m2/repository/org/slf4j/slf4j-api/1.7.6/slf4j-api-1.7.6.jar --module-path=target/chronicle-core-java-9-test-1.9.7-SNAPSHOT.jar:$HOME/.m2/repository/net/openhft/chronicle-core/1.9.7-SNAPSHOT/chronicle-core-1.9.7-SNAPSHOT.jar:../java9-compatibility/target/chronicle-core-java-9-compatibility-1.9.7-SNAPSHOT.jar --add-opens=java.base/java.nio=chronicle.core  --add-exports=java.base/sun.nio.ch=chronicle.core  --add-exports=java.base/sun.nio.ch=net.openhft.chronicle.core  --add-exports=java.base/jdk.internal.ref=net.openhft.chronicle.core -m java_nine.module.test/net.openhft.test.CleanByteBufferMain


 */