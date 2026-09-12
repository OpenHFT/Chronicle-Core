/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SystemTimeProviderSelectionTest {
    private static final String PROPERTY = "chronicle.time.monotonic";

    @ParameterizedTest
    @ValueSource(strings = {"unset", "false", "true", "yes", ""})
    void selectsClockOnceInAFreshJvm(String setting) throws Exception {
        JavaProcessBuilder builder = JavaProcessBuilder.create(SelectionProbe.class)
                .withProgramArguments(setting);
        if (!setting.equals("unset"))
            builder.withJvmArguments("-D" + PROPERTY + "=" + setting);
        Process process = builder.start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "clock selection probe did not terminate");
            String output = new String(IOTools.readAsBytes(process.getInputStream()), StandardCharsets.UTF_8)
                    + new String(IOTools.readAsBytes(process.getErrorStream()), StandardCharsets.UTF_8);
            assertEquals(0, process.exitValue(), output);
        } finally {
            try {
                if (process.isAlive()) {
                    process.destroyForcibly();
                    assertTrue(process.waitFor(5, TimeUnit.SECONDS), "clock selection probe did not stop");
                }
            } finally {
                Closeable.closeQuietly(process.getInputStream(), process.getErrorStream(), process.getOutputStream());
            }
        }
    }

    public static final class SelectionProbe {
        public static void main(String[] args) {
            if (args[0].equals("unset"))
                System.clearProperty(PROPERTY);
            boolean monotonic = !args[0].equals("unset") && !args[0].equals("false");
            TimeProvider selected = SystemTimeProvider.CLOCK;
            assertSame(monotonic ? MonotonicTimeProvider.INSTANCE : SystemTimeProvider.INSTANCE, selected);
            assertSame(SystemTimeProvider.class, SystemTimeProvider.INSTANCE.getClass());
            System.setProperty(PROPERTY, Boolean.toString(!monotonic));
            assertSame(selected, SystemTimeProvider.CLOCK, "selection must not change after initialisation");
            TimeProvider override = () -> 123L;
            SystemTimeProvider.CLOCK = override;
            assertSame(override, SystemTimeProvider.CLOCK, "test overrides remain supported");
            System.out.println("selected=" + selected.getClass().getSimpleName() + " setting=" + args[0]);
        }
    }
}
