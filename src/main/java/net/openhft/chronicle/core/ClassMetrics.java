package net.openhft.chronicle.core;

import java.util.Objects;

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
        return Objects.hash(offset, length);
    }

    @Override
    public String toString() {
        return "ClassMetrics{" +
                "offset=" + offset +
                ", length=" + length +
                '}';
    }
}
