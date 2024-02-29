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

import net.openhft.chronicle.core.util.ClassLocal;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClassLocalTest extends CoreTestCommon {

    @Test
    public void computeValue() {
        long[] count = {0};
        ClassLocal<String> toString = ClassLocal.withInitial(aClass -> {
//            System.out.println(aClass);
            count[0]++;
            return aClass.toGenericString();
        });
        for (int i = 0; i < 1000; i++) {
            toString.get(ClassValue.class);
        }
        assertEquals(1, count[0]);

    }
}
