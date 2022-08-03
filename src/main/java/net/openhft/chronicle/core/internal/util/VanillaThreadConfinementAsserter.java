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

package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.util.ThreadConfinementAsserter;

class VanillaThreadConfinementAsserter implements ThreadConfinementAsserter {

    private volatile Thread initialThread;

    @Override
    public void assertThreadConfined() {
        final Thread current = Thread.currentThread();
        Thread past = initialThread;
        if (past == null) {
            synchronized (this) {
                if (initialThread == null) {
                    initialThread = current;
                }
            }
            past = current;
        }
        if (past != current) {
            throw new IllegalStateException("Thread " + current + " accessed a thread confined class that was already accessed by thread " + initialThread);
        }
    }

    @Override
    public String toString() {
        return "VanillaThreadConfinementAsserter{" +
                "initialThread=" + initialThread +
                '}';
    }
}
