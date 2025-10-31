/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.*;

public class MockerFacadeTest {

    interface Sample {
        void run(String value);
    }

    @Test
    public void loggingToStringWriterDelegates() {
        StringWriter writer = new StringWriter();
        Sample sample = Mocker.logging(Sample.class, "sample-", writer);
        sample.run("value");

        assertTrue(writer.toString().contains("sample-run"));
        assertTrue(writer.toString().contains("value"));
    }

    @Test
    public void loggingToPrintStreamDelegates() {
        ByteArrayOutputStream backing = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(backing, true);
        Sample sample = Mocker.logging(Sample.class, "ps-", stream);
        sample.run("data");

        String logged = backing.toString();
        assertTrue(logged.contains("ps-run"));
        assertTrue(logged.contains("data"));
    }

    @Test
    public void queuingAddsEntries() throws InterruptedException {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);
        Sample sample = Mocker.queuing(Sample.class, "queue-", queue);
        sample.run("payload");

        assertEquals("queue-run[payload]", queue.take());
    }

    @Test
    public void ignoredProxySupportsCalls() {
        Sample sample = Mocker.ignored(Sample.class);
        sample.run("whatever");
        assertNotNull(sample);
    }
}
