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

import net.openhft.chronicle.core.util.Mocker;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
                        "end[333]%n"),
                out.toString());
    }

    @Test
    public void ignored() {
        final ChainedChainingTerminal logging = Mocker.ignored(ChainedChainingTerminal.class);
        logging.chains("one").alsoChains("two").end("three");
        logging.chains("111").alsoChains("222").end("333");
        assertNotNull(logging.toString());
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
