/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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
 * The {@code StringInterner} class provides string interning functionality to optimize memory usage by caching strings.
 * When a string is interned, it is looked up in the cache, and if an equal string is found, it is returned instead of creating a new one.
 *
 * <p>This class only guarantees correct behavior in terms of interning. When you ask it for a {@code String} for a given input,
 * it must return a {@code String} that matches the {@code toString()} of that {@code CharSequence}.
 *
 * <p>It does not guarantee that all threads will see the same data, nor that multiple threads will return the same {@code String} object for the same string.
 * It is designed to be a best-effort basis to be as lightweight as possible.
 *
 * <p>While technically not thread-safe, it can operate correctly when used from multiple threads without explicit locking or thread safety,
 * leveraging the thread safety of {@code String} in Java from version 5.0 onwards (JSR 133).
 *
 * <p>For more discussion on this, see: <a href="https://stackoverflow.com/questions/63383745/string-array-needless-synchronization/63383983">...</a>
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * StringInterner si = new StringInterner(128);
 * String internedString = si.intern("example");
 * }
 * </pre>
 *
 * <p>The {@code StringInterner} is used to optimize memory usage by caching strings and referring to them by index, rather than storing duplicate strings.
 * When you 'intern' a string, it is looked up in the cache, and if an equal string is found, it is returned instead of creating a new one.
 * </p>
 *
 */
public class StringInterner {

    /**
     * Array that stores the interned strings.
     */
    protected final String[] interner;

    /**
     * A bit mask used for index calculation in the cache array.
     */
    protected final int mask;

    /**
     * The number of bits to shift when calculating the secondary index.
     */
    protected final int shift;

    /**
     * A toggle used to determine which slot to replace on a cache miss.
     */
    protected boolean toggle = false;

    /**
     * Functional interface for handling changes in interned strings.
     */
    public interface Changed {
        void onChanged(int index, String value);
    }

    /**
     * Constructs a new {@code StringInterner} with the specified capacity.
     *
     * @param capacity the initial capacity of the interner.
     * @throws IllegalArgumentException if the capacity is invalid.
     */
    public StringInterner(int capacity) throws IllegalArgumentException {
        // Calculate the nearest power of two greater than or equal to the specified capacity.
        int n = Maths.nextPower2(capacity, 128);
        // Calculate the number of bits needed to represent the cache size.
        shift = Maths.intLog2(n);
        // Initialize the interner array with the calculated size.
        interner = new String[n];
        // Set the mask for fast index calculation.
        mask = n - 1;
    }

    /**
     * Returns the size of the interner array.
     *
     * @return the size of the interner array.
     */
    public int capacity() {
        return interner.length;
    }

    /**
     * Interns the specified {@code CharSequence}.
     * If an equal string is found in the cache, it is returned; otherwise,
     * the provided {@code CharSequence} is added to the cache.
     *
     * @param cs the {@code CharSequence} to intern.
     * @return the interned string, or the original {@code CharSequence}
     * if it is not interned.
     */
    @Nullable
    public String intern(@Nullable CharSequence cs) {
        if (cs == null)
            return null;
        if (cs.length() > interner.length)
            return cs.toString();

        // Compute a 64-bit hash of the CharSequence.
        long h1 = Maths.hash64(cs);
        h1 ^= h1 >> 32;

        // Calculate the primary index using the mask.
        int h = (int) h1 & mask;
        String s = interner[h];

        // Check if the cached string matches the given CharSequence.
        if (StringUtils.isEqual(cs, s))
            return s;

        // Calculate the secondary index using the hash shift and mask.
        int h2 = (int) (h1 >> shift) & mask;
        String s2 = interner[h2];

        // Check if the cached string at the secondary index matches the CharSequence.
        if (StringUtils.isEqual(cs, s2))
            return s2;

        // Convert the CharSequence to a String and add it to the cache.
        String s3 = cs.toString();
        interner[s == null || (s2 != null && toggle()) ? h : h2] = s3;

        return s3;
    }

    /**
     * Returns the index of the interned string in the cache, or -1 if not stored.
     * This method can be used in conjunction with the {@code Changed} interface to handle
     * cache changes.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * StringInterner si = new StringInterner(128);
     * String[] uppercase = new String[si.capacity()];
     *
     * String lowerCaseString = "example";
     *
     * int index = si.index(lowerCaseString, (i, value) -> uppercase[i] = value.toUpperCase());
     * if (index != -1)
     *     assertEquals(lowerCaseString.toUpperCase(), uppercase[index]);
     * }
     * </pre>
     *
     * @param cs        a source string to be interned.
     * @param onChanged a callback function invoked when the interned string changes.
     * @return the index where the interned string is stored, or -1 if not stored.
     */
    public int index(@Nullable CharSequence cs, @Nullable Changed onChanged) {
        if (cs == null)
            return -1;
        if (cs.length() > interner.length)
            return -1;

        // Compute a 64-bit hash of the CharSequence.
        long h1 = Maths.hash64(cs);
        h1 ^= h1 >> 32;

        // Calculate the primary index using the mask.
        int h = (int) h1 & mask;
        String s = interner[h];

        // Check if the cached string matches the given CharSequence.
        if (StringUtils.isEqual(cs, s))
            return h;

        // Calculate the secondary index using the hash shift and mask.
        int h2 = (int) (h1 >> shift) & mask;
        String s2 = interner[h2];

        // Check if the cached string at the secondary index matches the CharSequence.
        if (StringUtils.isEqual(cs, s2))
            return h2;

        // Convert the CharSequence to a String and add it to the cache.
        String s3 = cs.toString();
        final int i = s == null || (s2 != null && toggle()) ? h : h2;

        interner[i] = s3;

        // Notify the onChanged callback if provided.
        if (onChanged != null)
            onChanged.onChanged(i, s3);

        return i;
    }

    /**
     * Retrieves an interned string based on the specified index.
     *
     * @param index the index of the interned string, obtained via {@link #index}.
     * @return the interned string at the specified index.
     */
    public String get(int index) {
        return interner[index];
    }

    /**
     * Toggles the internal state between true and false, used to alternate
     * cache slot replacement between two possible slots.
     *
     * @return the new state of the toggle after toggling.
     */
    protected boolean toggle() {
        toggle = !toggle;
        return toggle;
    }

    /**
     * Returns the number of values currently stored in the interner.
     *
     * @return the count of non-null strings in the interner.
     */
    public int valueCount() {
        return (int) Stream.of(interner).filter(Objects::nonNull).count();
    }
}
