/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.Maths;

public class StaticEnumClass<E extends Enum<E>> extends EnumCache<E> {
    private final int initialSize;

    StaticEnumClass(Class<E> eClass) {
        super(eClass);
        this.initialSize = guessInitialSize(eClass);
    }

    private static int guessInitialSize(Class<? extends Enum> eClass) {
        Enum[] enumConstants = eClass.getEnumConstants();
        int initialSize = Maths.nextPower2(enumConstants.length * 2, 16);
        for (int i = 0; i < 3; i++) {
            Enum[] cache = new Enum[initialSize];
            int conflicts = 0;
            for (Enum enumConstant : enumConstants) {
                int n = Maths.hash32(enumConstant.name()) & (initialSize - 1);
                if (cache[n] == null)
                    cache[n] = enumConstant;
                else
                    conflicts++;
            }
            if (conflicts > 0) {
                Jvm.debug().on(eClass, "EnumCache " + initialSize + " conflicts " + conflicts);
                initialSize *= 2;
            }
        }
        return initialSize;
    }

    @Override
    public E valueOf(String name) {
        return name == null || name.isEmpty() ? null : Enum.valueOf(eClass, name);
    }

    @Override
    public int initialSize() {
        return initialSize;
    }
}
