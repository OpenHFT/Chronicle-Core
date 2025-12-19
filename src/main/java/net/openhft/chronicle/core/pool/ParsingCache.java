/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.core.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.openhft.chronicle.core.Jvm.uncheckedCast;

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
@Deprecated(/* to be removed in 2027, only used in tests */)
public class ParsingCache<E> {
    /**
     * Ring buffer of cached parse results indexed by hash.
     */
    protected final ParsedData<E>[] interner;
    /**
     * Mask applied to hashes to fit the table size.
     */
    protected final int mask;
    /**
     * Bit-shift used when double hashing.
     */
    protected final int shift;
    private final Function<String, E> eFunction;
    /**
     * Alternates bucket selection when both primary buckets are occupied.
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
        int n = Maths.nextPower2(capacity, 128);
        shift = Maths.intLog2(n);
        @SuppressWarnings("rawtypes")
        ParsedData[] obj = new ParsedData[n];
        interner = uncheckedCast(obj);
        mask = n - 1;
    }

    /**
     * Retrieves the object associated with the given CharSequence from the cache. If the object
     * does not exist in the cache, it is created using the function provided in the constructor,
     * stored in the cache, and then returned.
     *
     * @param cs The CharSequence to be parsed.
     * @return The object of type E corresponding to the provided CharSequence,
     * or {@code null} if {@code cs} is {@code null}.
     */
    @Nullable
    public E intern(@Nullable CharSequence cs) {
        if (cs == null)
            return null;
        long h1 = Maths.hash64(cs);
        h1 ^= h1 >> 32;
        int hash = (int) h1;
        int h = hash & mask;
        ParsedData<E> s = interner[h];
        if (s != null && StringUtils.isEqual(s.string, cs))
            return s.e;
        int h2 = (int) (h1 >> shift) & mask;
        ParsedData<E> s2 = interner[h2];
        if (s2 != null && StringUtils.isEqual(s2.string, cs))
            return s2.e;
        @NotNull String string = cs.toString();
        @NotNull ParsedData<E> s3 = new ParsedData<>(string, eFunction.apply(string));
        interner[s == null || (s2 != null && toggle()) ? h : h2] = s3;

        return s3.e;
    }

    /**
     * Flips the toggle used for alternating bucket selection.
     *
     * @return the updated toggle state
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
        return (int) Stream.of(interner).filter(Objects::nonNull).count();
    }

    /**
     * Tuple of the cached text and its parsed representation.
     */
    static class ParsedData<E> {
        final String string;
        final E e;

        ParsedData(String string, E e) {
            this.string = string;
            this.e = e;
        }
    }
}
