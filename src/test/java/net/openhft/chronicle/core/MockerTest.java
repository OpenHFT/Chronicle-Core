/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.Mocker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class MockerTest extends CoreTestCommon {

    @Test
    @DisplayName("Logging mocker captures chained calls and arguments")
    void intercepting() {
        StringWriter out = new StringWriter();
        final ChainedChainingTerminal logging = Mocker.logging(ChainedChainingTerminal.class, "", out);
        logging.chains("one").alsoChains("two").end("three");
        logging.chains("111").alsoChains("222").end("333");
        assertEquals(String.format("chains[one]%n" +
                        "alsoChains[two]%n" +
                        "end[three]%n" +
                        "chains[111]%n" +
                        "alsoChains[222]%n" +
                "end[333]%n"), out.toString(), "logging mocker should capture all method calls with arguments");
    }

    @Test
    @DisplayName("Ignored mocker ignores calls and returns toString")
    void ignored() {
        final ChainedChainingTerminal logging = Mocker.ignored(ChainedChainingTerminal.class);
        logging.chains("one").alsoChains("two").end("three");
        logging.chains("111").alsoChains("222").end("333");
        assertNotNull(logging.toString(), "ignored mocker should return non-null toString representation");
    }

    interface Chained<T> {
        T chains(String name);
    }

    interface Chaining<T> {
        T alsoChains(String name);
    }

    interface Terminal {
        void end(String text);
    }

    interface ChainingTerminal extends Chaining<Terminal> {
    }

    private interface ChainedChainingTerminal extends Chained<ChainingTerminal> {
    }

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("logging with PrintStream captures method calls")
    void loggingWithPrintStream() {
        StringWriter sw = new StringWriter();
        PrintStream ps = new PrintStream(System.out) {
            @Override
            public void println(String x) {
                sw.append(x).append(System.lineSeparator());
            }
        };
        final SimpleInterface logging = Mocker.logging(SimpleInterface.class, "test.", ps);
        logging.doSomething("arg1");
        assertTrue(sw.toString().contains("test.doSomething[arg1]"),
                "PrintStream logging should capture method call");
    }

    @Test
    @DisplayName("logging with PrintWriter captures method calls")
    void loggingWithPrintWriter() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        final SimpleInterface logging = Mocker.logging(SimpleInterface.class, "test.", pw);
        logging.doSomething("arg1");
        pw.flush();
        assertTrue(sw.toString().contains("test.doSomething[arg1]"),
                "PrintWriter logging should capture method call");
    }

    @Test
    @DisplayName("queuing adds method calls to BlockingQueue")
    void queuingAddsToQueue() throws InterruptedException {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
        final SimpleInterface queuing = Mocker.queuing(SimpleInterface.class, "q.", queue);
        queuing.doSomething("value");
        String result = queue.take();
        assertTrue(result.contains("q.doSomething[value]"),
                "queuing should add method call to queue");
    }

    @Test
    @DisplayName("intercepting with consumer receives method invocations")
    void interceptingWithConsumer() {
        List<String> captured = new ArrayList<>();
        final SimpleInterface intercepting = Mocker.intercepting(SimpleInterface.class, "i.", captured::add);
        intercepting.doSomething("test");
        assertEquals(1, captured.size(), "consumer should have received one invocation");
        assertTrue(captured.get(0).contains("i.doSomething[test]"),
                "captured string should contain method call");
    }

    @Test
    @DisplayName("ignored with additional interfaces includes all")
    void ignoredWithAdditionalInterfaces() {
        final Runnable ignored = Mocker.ignored(Runnable.class, Comparable.class);
        assertNotNull(ignored, "ignored mocker should be created");
        ignored.run(); // Should not throw
    }

    @Test
    @DisplayName("intercepting with no args method shows empty array")
    void interceptingNoArgs() {
        List<String> captured = new ArrayList<>();
        final NoArgsInterface intercepting = Mocker.intercepting(NoArgsInterface.class, "", captured::add);
        intercepting.noArgs();
        assertEquals(1, captured.size(), "consumer should have received one invocation");
        // No args shows as [] not ()
        assertTrue(captured.get(0).contains("noArgs[]"),
                "no-args method should show empty array: " + captured.get(0));
    }

    public interface SimpleInterface {
        void doSomething(String arg);
    }

    public interface NoArgsInterface {
        void noArgs();
    }
}
