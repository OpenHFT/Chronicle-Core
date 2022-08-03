/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
