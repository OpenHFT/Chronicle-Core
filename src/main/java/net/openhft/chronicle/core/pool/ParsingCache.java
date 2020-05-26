/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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

package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.core.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class ParsingCache<E> {
    @NotNull
    protected final ParsedData<E>[] interner;
    protected final int mask, shift;
    private final Function<String, E> eFunction;
    protected boolean toggle = false;

    public ParsingCache(int capacity, Function<String, E> eFunction) throws IllegalArgumentException {
        this.eFunction = eFunction;
        int n = Maths.nextPower2(capacity, 128);
        shift = Maths.intLog2(n);
        interner = new ParsedData[n];
        mask = n - 1;
    }

    @Nullable
    public E intern(@Nullable CharSequence cs) {
        if (cs == null)
            return null;
        int hash = Maths.hash32(cs);
        int h = hash & mask;
        ParsedData<E> s = interner[h];
        if (s != null && StringUtils.isEqual(s.string, cs))
            return s.e;
        int h2 = (hash >> shift) & mask;
        ParsedData<E> s2 = interner[h2];
        if (s2 != null && StringUtils.isEqual(s2.string, cs))
            return s2.e;
        @NotNull String string = cs.toString();
        @NotNull ParsedData<E> s3 = new ParsedData<>(string, eFunction.apply(string));
        interner[s == null || (s2 != null && toggle()) ? h : h2] = s3;

        return s3.e;
    }

    protected boolean toggle() {
        return toggle = !toggle;
    }

    public int valueCount() {
        return (int) Stream.of(interner).filter(Objects::nonNull).count();
    }

    static class ParsedData<E> {
        final String string;
        final E e;

        ParsedData(String string, E e) {
            this.string = string;
            this.e = e;
        }
    }
}
