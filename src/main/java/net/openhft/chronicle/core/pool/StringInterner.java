/*
 * Copyright 2016-2020 chronicle.software
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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * <p>
 * StringInterner only guarantees it will behave in a correct manner. When you ask it for a String for a given input, it must return a String which matches the toString() of that CharSequence.
 * </p><p>
 * It doesn't guarantee that all threads see the same data, nor that multiple threads will return the same String object for the same string. It is designed to be a best-effort basis so it can be as lightweight as possible.
 * </p><p>
 * So while technically not thread safe, it doesn't prevent it operating correctly when used from multiple threads, but it is faster than added explicit locking or thread safety. NOTE: It does rely on String being thread safe, something which was guaranteed from Java 5.0 onwards c.f. JSR 133.
 * </p><p>
 * Discussion https://stackoverflow.com/questions/63383745/string-array-needless-synchronization/63383983
 * </p>
 *
 * @author peter.lawrey
 */
public class StringInterner {
    protected final String[] interner;
    protected final int mask, shift;
    protected boolean toggle = false;

    public interface Changed {
        void onChanged(int index, String value);
    }

    // throws IllegalArgumentException
    public StringInterner(int capacity) throws IllegalArgumentException {
        int n = Maths.nextPower2(capacity, 128);
        shift = Maths.intLog2(n);
        interner = new String[n];
        mask = n - 1;
    }

    /**
     * @return the size of  interner[]
     */
    public int capacity() {
        return interner.length;
    }

    @Nullable
    public String intern(@Nullable CharSequence cs) {
        if (cs == null)
            return null;
        if (cs.length() > interner.length)
            return cs.toString();
        int hash = Maths.hash32(cs);
        int h = hash & mask;
        String s = interner[h];
        if (StringUtils.isEqual(cs, s))
            return s;
        int h2 = (hash >> shift) & mask;
        String s2 = interner[h2];
        if (StringUtils.isEqual(cs, s2))
            return s2;
        String s3 = cs.toString();
        interner[s == null || (s2 != null && toggle()) ? h : h2] = s3;

        return s3;
    }

    private String[] uppercase;

    /**
     * provide
     *
     * @param cs a source string
     * @return the index that the internered string is stored in, or -1 if not stored
     * <p>
     * <p>
     * An example of the StringInterner used in conjunction with the uppercase[] to cache another value
     * <pre>
     *
     * private String[] uppercase;
     *
     * public void myMethod() throws IllegalArgumentException {
     *
     *         StringInterner si = new StringInterner(128);
     *         uppercase = new String[si.capacity()];
     *
     *         String lowerCaseString = ...
     *
     *         int index = si.index(lowerCaseString, this::changed);
     *         if (index != -1)
     *             assertEquals(lowerCaseString.toUpperCase(), uppercase[index]);
     * }
     *
     * private void changed(int index, String value) {
     *      uppercase[index] = value.toUpperCase();
     * }
     *
     * </pre>
     */
    public int index(@Nullable CharSequence cs, @Nullable Changed onChanged) {
        if (cs == null)
            return -1;
        if (cs.length() > interner.length)
            return -1;
        int hash = Maths.hash32(cs);
        int h = hash & mask;
        String s = interner[h];
        if (StringUtils.isEqual(cs, s))
            return h;

        int h2 = (hash >> shift) & mask;
        String s2 = interner[h2];
        if (StringUtils.isEqual(cs, s2))
            return h2;

        String s3 = cs.toString();
        final int i = s == null || (s2 != null && toggle()) ? h : h2;

        interner[i] = s3;
        if (onChanged != null)
            onChanged.onChanged(i, s3);
        return i;
    }

    /**
     * get an intered string based on the index
     *
     * @param index the index of the  interner string, to acquire an index call  {@link net.openhft.chronicle.core.pool.StringInterner#index}
     * @return
     */
    public String get(int index) {
        return interner[index];
    }

    protected boolean toggle() {
        return toggle = !toggle;
    }

    public int valueCount() {
        return (int) Stream.of(interner).filter(Objects::nonNull).count();
    }
}
