/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
 * <p>
 * It doesn't guarantee that all threads see the same data, nor that multiple threads will return the same String object for the same string. It is designed to be a best-effort basis so it can be as lightweight as possible.
 * <p>
 * So while technically not thread safe, it doesn't prevent it operating correctly when used from multiple threads, but it is faster than added explicit locking or thread safety. NOTE: It does rely on String being thread safe, something which was guaranteed from Java 5.0 onwards c.f. JSR 133.
 * <p>
 * Discussion <a href="https://stackoverflow.com/questions/63383745/string-array-needless-synchronization/63383983">...</a>
 * <p>
 * This class provides string interning functionality.
 * It's used to optimize memory usage by caching strings and
 * referring to them by index, rather than storing duplicate strings.
 * When you 'intern' a string, it's looked up in the cache and
 * if an equal string is found, it's returned instead of creating a new one.
 *
 * @author peter.lawrey
 */

public class StringInterner {
    protected final String[] interner;
    protected final int mask;
    protected final int shift;
    protected boolean toggle = false;

    /**
     * Callback for receiving notification when a value is stored.
     */
    public interface Changed {
        /**
         * Invoked when a value is inserted or replaced.
         *
         * @param index the slot used for storage
         * @param value the interned string
         */
        void onChanged(int index, String value);
    }

    /**
     * Constructs a new StringInterner with the specified capacity.
     *
     * @param capacity the initial capacity of the interner.
     * @throws IllegalArgumentException if the capacity is invalid.
     */
    public StringInterner(int capacity) throws IllegalArgumentException {
        int n = Maths.nextPower2(capacity, 128);
        shift = Maths.intLog2(n);
        interner = new String[n];
        mask = n - 1;
    }

    /**
     * @return the size of interner[]
     */
    public int capacity() {
        return interner.length;
    }

    /**
     * Interns the specified CharSequence.
     *
     * @param cs the CharSequence to intern.
     * @return the interned string, or the original CharSequence
     * if it's not interned.
     */
    @Nullable
    public String intern(@Nullable CharSequence cs) {
        if (cs == null)
            return null;
        if (cs.length() > interner.length)
            return cs.toString();
        long h1 = Maths.hash64(cs);
        h1 ^= h1 >> 32;
        int h = (int) h1 & mask;
        String s = interner[h];
        if (StringUtils.isEqual(cs, s))
            return s;
        int h2 = (int) (h1 >> shift) & mask;
        String s2 = interner[h2];
        if (StringUtils.isEqual(cs, s2))
            return s2;
        String s3 = cs.toString();
        interner[s == null || (s2 != null && toggle()) ? h : h2] = s3;

        return s3;
    }

    /**
     * Returns the slot index for the given text, adding it if absent.
     *
     * @param cs        the source text
     * @param onChanged callback invoked when a new value is stored
     * @return the slot index or {@code -1} if the text is too long
     */
    public int index(@Nullable CharSequence cs, @Nullable Changed onChanged) {
        if (cs == null)
            return -1;
        if (cs.length() > interner.length)
            return -1;
        long h1 = Maths.hash64(cs);
        h1 ^= h1 >> 32;
        int h = (int) h1 & mask;
        String s = interner[h];
        if (StringUtils.isEqual(cs, s))
            return h;

        int h2 = (int) (h1 >> shift) & mask;
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
     * Returns the interned string held at the given index.
     *
     * @param index the slot obtained from {@link #index(CharSequence, Changed)}
     * @return the interned string, or {@code null} if no value is stored at that index
     */
    @Nullable
    public String get(int index) {
        return interner[index];
    }

    protected boolean toggle() {
        toggle = !toggle;
        return toggle;
    }

    /**
     * Returns the number of values in the interner.
     *
     * @return the count of non-null strings in the interner.
     */
    public int valueCount() {
        return (int) Stream.of(interner).filter(Objects::nonNull).count();
    }
}
