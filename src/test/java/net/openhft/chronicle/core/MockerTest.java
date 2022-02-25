package net.openhft.chronicle.core;

import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class MockerTest {

    @Test
    public void intercepting() {
        StringWriter out = new StringWriter();
        final ChainedChainingTerminal logging = Mocker.logging(ChainedChainingTerminal.class, "", out);
        logging.chains("one").alsoChains("two").end("three");
        logging.chains("111").alsoChains("222").end("333");
        assertEquals("" +
                        "chains[one]\n" +
                        "alsoChains[two]\n" +
                        "end[three]\n" +
                        "chains[111]\n" +
                        "alsoChains[222]\n" +
                        "end[333]\n",
                out.toString());
    }

    @Test
    public void ignored() {
        final ChainedChainingTerminal logging = Mocker.ignored(ChainedChainingTerminal.class);
        logging.chains("one").alsoChains("two").end("three");
        logging.chains("111").alsoChains("222").end("333");
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

    interface ChainedChainingTerminal extends Chained<ChainingTerminal> {
    }
}