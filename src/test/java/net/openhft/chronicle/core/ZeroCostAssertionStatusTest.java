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

import net.openhft.chronicle.assertions.AssertUtil;
import org.junit.jupiter.api.Test;

class ZeroCostAssertionStatusTest {

    @Test
    void show() {
        boolean ae = false;
        try {
            assert 0 != 0;
        } catch (AssertionError assertionError) {
            ae = true;
        }

        boolean zcae = false;
        try {
            assert AssertUtil.SKIP_ASSERTIONS || 0 != 0;
        } catch (AssertionError assertionError) {
            zcae = true;
        }

        System.out.println("Normal assertions are " + (ae ? "ON" : "OFF"));
        System.out.println("Zero-cost assertions are " + (zcae ? "ON" : "OFF"));
    }

}