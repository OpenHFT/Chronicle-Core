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

import java.util.concurrent.atomic.AtomicBoolean;

public final class ThreadConfinementLifecycle {

    private static final boolean ASSERTIONS_ENABLE = assertionsEnable();

    private ThreadConfinementLifecycle() {}

    public static ThreadConfinementAsserter create() {
        return create(ASSERTIONS_ENABLE);
    }

    public static ThreadConfinementAsserter createEnabled() {
        return create(true);
    }

    static ThreadConfinementAsserter create(boolean active) {
        return active
                ? new VanillaThreadConfinementAsserter()
                : NopThreadConfinementAsserter.INSTANCE;
    }

    static boolean assertionsEnable() {
        final AtomicBoolean ae = new AtomicBoolean();
        assert testAssert(ae);
        return ae.get();
    }

    private static boolean testAssert(final AtomicBoolean ae) {
        ae.set(true);
        return true;
    }
}
