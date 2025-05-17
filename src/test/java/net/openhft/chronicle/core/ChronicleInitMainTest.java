package net.openhft.chronicle.core;

import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChronicleInitMainTest extends CoreTestCommon {

    public static class UniquePropertyRunnable implements Runnable {
        @Override
        public void run() {
            System.setProperty("unique.test.property", "expectedValue");
        }
    }

    @Test
    public void propertySetViaInitRunnableIsVisibleInSubprocess() throws Exception {
        Process process = JavaProcessBuilder.create(ChronicleInit.class)
                .withProgramArguments("unique.test.property")
                .withJvmArguments("-Dchronicle.init.runnable=" + UniquePropertyRunnable.class.getName())
                .start();

        String output;
        try {
            assertEquals(0, process.waitFor());
            output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitMainTest", process);
        }

        assertTrue(output.contains("unique.test.property=expectedValue"));
    }
}
