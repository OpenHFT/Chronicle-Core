//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
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
