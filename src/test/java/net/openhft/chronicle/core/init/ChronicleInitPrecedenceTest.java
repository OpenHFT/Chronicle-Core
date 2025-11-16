/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.init;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChronicleInitPrecedenceTest extends CoreTestCommon {

    @Test
    @Ignore("Init precedence wiring not yet enabled on this branch")
    public void initRunnableMayRedirectSystemPropertiesBeforeJvmLoads() throws Exception {
        Path props = Files.createTempFile("chronicle-init-precedence", ".properties");
        Files.write(props, "chronicle.init.test.flag=primed\n".getBytes(StandardCharsets.ISO_8859_1));

        Process process = JavaProcessBuilder.create(SystemPropertiesProbeMain.class)
                .withJvmArguments(
                        "-Dchronicle.init.runnable=" + SystemPropertiesPrimingRunnable.class.getName(),
                        "-Dchronicle.init.test.systemPropertiesPath=" + props.toAbsolutePath())
                .start();
        try {
            assertEquals("Child JVM should exit cleanly", 0, process.waitFor());
            String stdout = readStdOut(process);
            assertTrue("chronicle.init.test.flag should be present in stdout", stdout.contains("primed"));
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitPrecedenceTest", process);
            Files.deleteIfExists(props);
        }
    }

    private static String readStdOut(Process process) throws IOException {
        try (InputStream in = process.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[512];
            int read;
            while ((read = in.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
            return out.toString(StandardCharsets.ISO_8859_1.name());
        }
    }
}
