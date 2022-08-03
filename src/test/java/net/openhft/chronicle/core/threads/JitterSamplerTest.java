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

package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import static org.junit.Assert.*;

public class JitterSamplerTest {

    @Test
    public void takeSnapshot() throws InterruptedException {

//        Assume.assumeTrue(!Jvm.isArm());
        Thread t = new Thread(() -> {
            JitterSampler.atStage("started");
            int millis = Jvm.isArm() ? 120 : 60;
            JitterSampler.sleepSilently(millis);
            JitterSampler.atStage("finishing");
            JitterSampler.sleepSilently(millis);
            JitterSampler.finished();
        });
        t.start();
        STARTED:
        {
            for (int j = 20; j >= 0; j--) {
                Jvm.pause(20);
                if (JitterSampler.desc != null)
                    break STARTED;
            }
            fail("Not started");
        }
        for (int i = 0; i < 10; i++) {
            JitterSampler.sleepSilently(10);
            String s = JitterSampler.takeSnapshot(10_000_000);
            //System.out.println(s);
            if ("finishing".equals(JitterSampler.desc)) {
                if (s != null && s.contains("finish"))
                    break;
            } else {
                assertEquals("started", JitterSampler.desc);
            }
        }
        t.join();
        String s = JitterSampler.takeSnapshot();
        assertNull(s);
    }
}