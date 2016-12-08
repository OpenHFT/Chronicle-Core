/*
 * Copyright 2016 higherfrequencytrading.com
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

package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 08/12/16.
 */
public class SystemTimeProviderTest {
    @Test
    public void currentTimeMicros() throws Exception {
        Jvm.recordExceptions(true);
        TimeProvider tp = SystemTimeProvider.INSTANCE;
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start + 10000) {
            long time2 = tp.currentTimeMicros();
            Thread.yield();
        }
        Map<ExceptionKey, Integer> exceptions = new HashMap<>();
        Jvm.dumpException(exceptions);
        System.out.println(exceptions);
    }
}