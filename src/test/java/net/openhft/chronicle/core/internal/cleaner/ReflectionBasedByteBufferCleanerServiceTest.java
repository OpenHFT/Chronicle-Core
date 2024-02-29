package net.openhft.chronicle.core.internal.cleaner;

import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService.Impact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectionBasedByteBufferCleanerServiceTest {

    private final ReflectionBasedByteBufferCleanerService cleanerService = new ReflectionBasedByteBufferCleanerService();

    @Test
    @EnabledIfSystemProperty(named = "java.version", matches = "1\\.8.*")
    public void cleanShouldWorkOnJava8() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        assertDoesNotThrow(() -> cleanerService.clean(buffer), "Cleaning a direct buffer should not throw an exception on Java 8");
    }

    @Test
    @EnabledIfSystemProperty(named = "java.version", matches = "9|1[0-9].*")
    public void cleanShouldWorkOnJava9Plus() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        assertDoesNotThrow(() -> cleanerService.clean(buffer), "Cleaning a direct buffer should not throw an exception on Java 9+");
    }

    @Test
    public void impactShouldReturnValidImpact() {
        Impact impact = cleanerService.impact();
        assertTrue(impact == Impact.SOME_IMPACT || impact == Impact.UNAVAILABLE, "Impact should be either SOME_IMPACT or UNAVAILABLE");
    }
}
