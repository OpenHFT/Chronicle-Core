/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Maths;

/**
 * ClassMetrics is a utility class that holds offset and length metrics of a class.
 * It provides methods to access these metrics and overrides equals, hashCode, and toString methods.
 */
public class ClassMetrics {
    private final int offset;
    private final int length;

    /**
     * Constructs a ClassMetrics object with the specified offset and length.
     *
     * @param offset the offset value
     * @param length the length value
     */
    public ClassMetrics(int offset, int length) {
        this.offset = offset;
        this.length = length;
    }

    /**
     * @return the offset
     */
    public int offset() {
        return offset;
    }

    /**
     * @return the length
     */
    public int length() {
        return length;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare
     * @return true if this object is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassMetrics that = (ClassMetrics) o;
        return offset == that.offset &&
                length == that.length;
    }

    /**
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Maths.hash(offset, length);
    }

    /**
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "ClassMetrics{" +
                "offset=" + offset +
                ", length=" + length +
                '}';
    }
}
