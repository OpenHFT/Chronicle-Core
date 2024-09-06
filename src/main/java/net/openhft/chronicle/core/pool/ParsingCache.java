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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A cache for parsed values that is optimized for fast lookup. This class is used to cache objects
 * that are constructed from strings, often as a result of parsing. The cache has a fixed capacity
 * and uses a hash function for fast indexing.
 *
 * <p>Use case is when there is a need to repeatedly parse the same strings into objects,
 * such as BigDecimal in the example test case, and would like to reuse the parsed object rather than
 * creating a new one each time.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * ParsingCache<BigDecimal> cache = new ParsingCache<>(128, BigDecimal::new);
 * BigDecimal value = cache.intern("123.456");
 * }
 * </pre>
 *
 * @param <E> The type of object the cache stores. Typically, these are objects created from strings.
 */
public class ParsingCache<E> {

    /**
     * An array of ParsedData objects used to store the cached entries.
     */
    protected final ParsedData<E>[] interner;

    /**
     * A bit mask used to calculate the index in the cache array.
     */
    protected final int mask;

    /**
     * The number of bits to shift when calculating the secondary index.
     */
    protected final int shift;

    /**
     * A function to create instances of type E from strings, used when a cache miss occurs.
     */
    private final Function<String, E> eFunction;

    /**
     * A toggle used to determine which slot to replace on a cache miss.
     */
    protected boolean toggle = false;

    /**
     * Constructs a new ParsingCache with the specified capacity.
     *
     * @param capacity  The capacity of the cache.
     * @param eFunction A function that creates new instances of type E from a string.
     */
    public ParsingCache(int capacity, Function<String, E> eFunction) {
        this.eFunction = eFunction;
        // Calculate the nearest power of two greater than or equal to the specified capacity.
        int n = Maths.nextPower2(capacity, 128);
        // Calculate the number of bits needed to represent the cache size.
        shift = Maths.intLog2(n);
        // Initialize the cache array with the calculated size.
        interner = (ParsedData<E>[]) new ParsedData[n];
        // Set the mask for fast index calculation.
        mask = n - 1;
    }

    /**
     * Retrieves the object associated with the given CharSequence from the cache. If the object
     * does not exist in the cache, it is created using the function provided in the constructor,
     * stored in the cache, and then returned.
     *
     * @param cs The CharSequence to be parsed.
     * @return The object of type E corresponding to the provided CharSequence.
     */
    @Nullable
    public E intern(@Nullable CharSequence cs) {
        if (cs == null)
            return null;
        // Compute a 64-bit hash of the CharSequence.
        long h1 = Maths.hash64(cs);
        h1 ^= h1 >> 32;
        // Convert the 64-bit hash to a 32-bit hash.
        int hash = (int) h1;
        // Calculate the primary index using the mask.
        int h = hash & mask;
        ParsedData<E> s = interner[h];
        // Check if the cached object matches the given CharSequence.
        if (s != null && StringUtils.isEqual(s.string, cs))
            return s.e;
        // Calculate the secondary index using the hash shift and mask.
        int h2 = (int) (h1 >> shift) & mask;
        ParsedData<E> s2 = interner[h2];
        // Check if the cached object at the secondary index matches the CharSequence.
        if (s2 != null && StringUtils.isEqual(s2.string, cs))
            return s2.e;
        // Convert the CharSequence to a String and create a new ParsedData object.
        @NotNull String string = cs.toString();
        @NotNull ParsedData<E> s3 = new ParsedData<>(string, eFunction.apply(string));
        // Replace the entry in the cache based on the toggle's state.
        interner[s == null || (s2 != null && toggle()) ? h : h2] = s3;

        return s3.e;
    }

    /**
     * Toggles the state between true and false, used to alternate cache slot replacement
     * between two possible slots.
     *
     * @return The new state of the toggle after toggling.
     */
    protected boolean toggle() {
        toggle = !toggle;
        return toggle;
    }

    /**
     * Returns the number of values currently in the cache.
     *
     * @return The number of values in the cache.
     */
    public int valueCount() {
        // Count non-null entries in the interner array.
        return (int) Stream.of(interner).filter(Objects::nonNull).count();
    }

    /**
     * Inner class representing parsed data stored in the cache.
     *
     * @param <E> The type of the cached object.
     */
    static class ParsedData<E> {

        /**
         * The string representation of the cached data.
         */
        final String string;

        /**
         * The cached object of type E.
         */
        final E e;

        /**
         * Constructs a new ParsedData object with the given string and parsed object.
         *
         * @param string The string representation of the cached data.
         * @param e      The parsed object of type E.
         */
        ParsedData(String string, E e) {
            this.string = string;
            this.e = e;
        }
    }
}
