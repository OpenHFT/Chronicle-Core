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

import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;

public class NotNullIntrumentationTargetTest extends CoreTestCommon {

    @Test
    public void notNull() {
        test("a");
    }

    @Test(expected = NullPointerException.class)
    @Ignore("Awaiting https://github.com/osundblad/intellij-annotations-instrumenter-maven-plugin/issues/53. " +
            "When compiled by IntelliJ this class is instrumented with null checks but it will throw an IllegalArgumentExceptioj not NPE!")
    public void Null() {
        test(null);
    }

    @SuppressWarnings("EmptyMethod")
    private static void test(@NotNull String nn) {
        // This should throw an NPE if called with a null argument
    }
}
