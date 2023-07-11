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
package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * A singleton enum implementing a Comparator for comparing instances of {@code CharSequence}.
 * <p>
 * This comparator can be used, for instance, in sorting lists or performing binary searches where
 * the elements are CharSequence instances. It performs a lexicographic comparison character by
 * character.
 * <p>
 * Example:
 * <pre>
 *     // Sorting a list of data objects based on CharSequence id before marshalling.
 *     dataList.sort(comparing(Data::getId, CharSequenceComparator.INSTANCE));
 *
 *     // Finding an index within a sorted list based on a CharSequence key.
 *     int index = binarySearch(dataList, Data::getId, "DEF", CharSequenceComparator.INSTANCE);
 * </pre>
 *
 * @see CharSequence
 * @see Comparator
 */
public enum CharSequenceComparator implements Comparator<CharSequence> {
    /**
     * The singleton instance of CharSequenceComparator.
     */
    INSTANCE;

    /**
     * Compares two {@code CharSequence} instances lexicographically.
     * <p>
     * Returns a negative integer, zero, or a positive integer as the first argument is less than,
     * equal to, or greater than the second, respectively.
     *
     * @param o1 The first {@code CharSequence} to be compared.
     * @param o2 The second {@code CharSequence} to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is less than,
     * equal to, or greater than the second.
     * @throws AssertionError if an {@code IndexOutOfBoundsException} occurs, indicating a bug.
     */
    @Override
    public int compare(@NotNull CharSequence o1, @NotNull CharSequence o2) {
        final int o1Length = o1.length();
        final int o2Length = o2.length();
        final int len = Math.min(o1Length, o2Length);
        for (int i = 0; i < len; i++) {
            final int cmp = Character.compare(o1.charAt(i), o2.charAt(i));
            if (cmp != 0)
                return cmp;
        }
        return Integer.compare(o1Length, o2Length);
    }
}
