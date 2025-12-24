/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CharSequenceComparatorTest {

    @Test
    void compareIdenticalSequences() {
        CharSequence seq1 = "test";
        CharSequence seq2 = "test";

        assertEquals(0, CharSequenceComparator.INSTANCE.compare(seq1, seq2), "comparing identical sequences should return zero");
    }

    @Test
    void compareDifferentSequencesSameLength() {
        CharSequence seq1 = "abc";
        CharSequence seq2 = "abd";

        int firstResult = CharSequenceComparator.INSTANCE.compare(seq1, seq2);
        int secondResult = CharSequenceComparator.INSTANCE.compare(seq2, seq1);
        assertTrue(firstResult < 0, "same-length comparison compare(\"" + seq1 + "\", \"" + seq2 + "\") should be < 0, was " + firstResult);
        assertTrue(secondResult > 0, "same-length comparison compare(\"" + seq2 + "\", \"" + seq1 + "\") should be > 0, was " + secondResult);
    }

    @Test
    void compareDifferentLengthSequences() {
        CharSequence seq1 = "abc";
        CharSequence seq2 = "abcd";

        int firstResult = CharSequenceComparator.INSTANCE.compare(seq1, seq2);
        int secondResult = CharSequenceComparator.INSTANCE.compare(seq2, seq1);
        assertTrue(firstResult < 0, "prefix-length comparison compare(\"" + seq1 + "\", \"" + seq2 + "\") should be < 0, was " + firstResult);
        assertTrue(secondResult > 0, "prefix-length comparison compare(\"" + seq2 + "\", \"" + seq1 + "\") should be > 0, was " + secondResult);
    }

    @Test
    void compareEmptyAndNonEmptySequences() {
        CharSequence emptySeq = "";
        CharSequence nonEmptySeq = "test";

        int firstResult = CharSequenceComparator.INSTANCE.compare(emptySeq, nonEmptySeq);
        int secondResult = CharSequenceComparator.INSTANCE.compare(nonEmptySeq, emptySeq);
        assertTrue(firstResult < 0, "empty sequence comparison compare(\"" + emptySeq + "\", \"" + nonEmptySeq + "\") should be < 0, was " + firstResult);
        assertTrue(secondResult > 0, "empty sequence comparison compare(\"" + nonEmptySeq + "\", \"" + emptySeq + "\") should be > 0, was " + secondResult);
    }
}
