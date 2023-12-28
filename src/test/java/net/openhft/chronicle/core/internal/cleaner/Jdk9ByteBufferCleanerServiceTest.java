package net.openhft.chronicle.core.internal.cleaner;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService.Impact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class Jdk9ByteBufferCleanerServiceTest {

    private Jdk9ByteBufferCleanerService cleanerService;

    @BeforeEach
    public void setUp() {
        cleanerService = new Jdk9ByteBufferCleanerService();
    }

    @Test
    public void cleanInvalidByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        assertThrows(Exception.class, () -> cleanerService.clean(buffer));
    }

    @Test
    public void impactShouldBeNoImpact() {
        assertEquals(Impact.NO_IMPACT, cleanerService.impact());
    }
}
