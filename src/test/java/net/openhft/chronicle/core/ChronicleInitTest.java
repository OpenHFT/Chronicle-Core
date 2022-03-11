package net.openhft.chronicle.core;

import net.openhft.chronicle.testframework.process.ProcessRunner;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChronicleInitTest {

    public static void main(String[] args) {
        // Normally enabled via system.properties file
        assertFalse(Jvm.isResourceTracing());
    }

    @Test
    public void testPositive() throws Exception {
        Process process = ProcessRunner.runClass(ChronicleInitTest.class,
                new String[] {"-Dchronicle.init.runnable=" + ResourceTracingInit.class.getName()}, new String[0]);

        try {
            assertEquals(0, process.waitFor());
        } finally {
            ProcessRunner.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @Test
    public void testNoInit() throws Exception {
        Process process = ProcessRunner.runClass(ChronicleInitTest.class);

        try {
            assertNotEquals(0, process.waitFor());
        } finally {
            ProcessRunner.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @Test
    public void testBadClass() throws Exception {
        Process process = ProcessRunner.runClass(ChronicleInitTest.class,
                new String[] {"-Dchronicle.init.class=" + ChronicleInitTest.class.getName()}, new String[0]);

        try {
            assertNotEquals(0, process.waitFor());
        } finally {
            ProcessRunner.printProcessOutput("ChronicleInitTest", process);
        }
    }

    public static class ResourceTracingInit implements Runnable {
        @Override
        public void run() {
            System.err.println("disabling resource tracking");
            System.setProperty("jvm.resource.tracing", "false");
        }
    }
}
