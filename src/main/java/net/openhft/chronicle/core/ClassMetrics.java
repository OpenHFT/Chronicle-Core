/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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
package net.openhft.chronicle.core;

public class ClassMetrics {
    private final int offset;
    private final int length;

    public ClassMetrics(int offset, int length) {
        this.offset = offset;
        this.length = length;
    }

    public int offset() {
        return offset;
    }

    public int length() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassMetrics that = (ClassMetrics) o;
        return offset == that.offset &&
                length == that.length;
    }

    @Override
    public int hashCode() {
        return Maths.hash(offset, length);
    }

    @Override
    public String toString() {
        return "ClassMetrics{" +
                "offset=" + offset +
                ", length=" + length +
                '}';
    }
}
