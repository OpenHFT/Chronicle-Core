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

class MockerFacadeTest {

    @Test
    void loggingToStringWriterDelegates() {
        StringWriter writer = new StringWriter();
        Sample sample = Mocker.logging(Sample.class, "sample-", writer);
        sample.run("value");

        String logged = writer.toString();
        assertTrue(logged.contains("sample-run"), "logged output should contain sample-run: " + logged);
        assertTrue(logged.contains("value"), "logged output should contain value: " + logged);
    }

    @Test
    void loggingToPrintStreamDelegates() {
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
        assertTrue(logged.contains("ps-run"), "printed output should contain ps-run: " + logged);
        assertTrue(logged.contains("data"), "printed output should contain data: " + logged);
    }

    @Test
    void queuingAddsEntries() throws InterruptedException {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);
        Sample sample = Mocker.queuing(Sample.class, "queue-", queue);
        sample.run("payload");

        assertEquals("queue-run[payload]", queue.take(), "queue should contain formatted method call with prefix and argument");
    }

    @Test
    void ignoredProxySupportsCalls() {
        Sample sample = Mocker.ignored(Sample.class);
        sample.run("whatever");
        assertNotNull(sample, "required object should not be null");
    }

    interface Sample {
        void run(String value);
    }
}
