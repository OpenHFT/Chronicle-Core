/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

public class MockerFacadeTest {

    @Test
    public void loggingToStringWriterDelegates() {
        StringWriter writer = new StringWriter();
        Sample sample = Mocker.logging(Sample.class, "sample-", writer);
        sample.run("value");

        assertTrue(writer.toString().contains("sample-run"), "loggingToStringWriterDelegates: L25");
        assertTrue(writer.toString().contains("value"), "loggingToStringWriterDelegates: L26");
    }

    @Test
    public void loggingToPrintStreamDelegates() {
        ByteArrayOutputStream backing = new ByteArrayOutputStream();
        PrintStream stream;
        try {
            stream = new PrintStream(backing, true, UTF_8.name());
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException("ISO-8859-1 should always be supported", e);
        }
        Sample sample = Mocker.logging(Sample.class, "ps-", stream);
        sample.run("data");

        String logged = new String(backing.toByteArray(), UTF_8);
        assertTrue(logged.contains("ps-run"), "loggingToPrintStreamDelegates: L42");
        assertTrue(logged.contains("data"), "loggingToPrintStreamDelegates: L43");
    }

    @Test
    public void queuingAddsEntries() throws InterruptedException {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);
        Sample sample = Mocker.queuing(Sample.class, "queue-", queue);
        sample.run("payload");

        assertEquals("queue-run[payload]", queue.take(), "queuingAddsEntries: L52");
    }

    @Test
    public void ignoredProxySupportsCalls() {
        Sample sample = Mocker.ignored(Sample.class);
        sample.run("whatever");
        assertNotNull(sample, "ignoredProxySupportsCalls: L59");
    }

    interface Sample {
        void run(String value);
    }
}
