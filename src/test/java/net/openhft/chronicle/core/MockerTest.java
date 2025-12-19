/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.Mocker;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MockerTest extends CoreTestCommon {

    @Test
    public void intercepting() {
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
    public void ignored() {
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
}
