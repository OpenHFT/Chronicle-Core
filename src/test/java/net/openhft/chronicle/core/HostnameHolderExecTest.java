package net.openhft.chronicle.core;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.Jvm;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class HostnameHolderExecTest {

    @Test
    public void execHostnameReadsProvidedValue() throws Exception {
        Assumptions.assumeTrue(Jvm.majorVersion() <= 17);

        Process process = mock(Process.class);
        InputStream is = new ByteArrayInputStream("my-test-host\n".getBytes());
        when(process.getInputStream()).thenReturn(is);

        Runtime runtime = mock(Runtime.class);
        when(runtime.exec("hostname")).thenReturn(process);

        try (MockedStatic<Runtime> mocked = mockStatic(Runtime.class)) {
            mocked.when(Runtime::getRuntime).thenReturn(runtime);
            String hostname = OS.HostnameHolder.execHostname();
            assertEquals("my-test-host", hostname);
        }
    }

    @Test
    public void execHostnameReadsLocalhost() throws Exception {
        Assumptions.assumeTrue(Jvm.majorVersion() <= 17);

        Process process = mock(Process.class);
        InputStream is = new ByteArrayInputStream("localhost\n".getBytes());
        when(process.getInputStream()).thenReturn(is);

        Runtime runtime = mock(Runtime.class);
        when(runtime.exec("hostname")).thenReturn(process);

        try (MockedStatic<Runtime> mocked = mockStatic(Runtime.class)) {
            mocked.when(Runtime::getRuntime).thenReturn(runtime);
            String hostname = OS.HostnameHolder.execHostname();
            assertEquals("localhost", hostname);
        }
    }
}
