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

        assertTrue(CharSequenceComparator.INSTANCE.compare(seq1, seq2) < 0, "lexically earlier sequence should compare as less than later sequence");
        assertTrue(CharSequenceComparator.INSTANCE.compare(seq2, seq1) > 0, "lexically later sequence should compare as greater than earlier sequence");
    }

    @Test
    void compareDifferentLengthSequences() {
        CharSequence seq1 = "abc";
        CharSequence seq2 = "abcd";

        assertTrue(CharSequenceComparator.INSTANCE.compare(seq1, seq2) < 0, "shorter sequence should compare as less than longer sequence with same prefix");
        assertTrue(CharSequenceComparator.INSTANCE.compare(seq2, seq1) > 0, "longer sequence should compare as greater than shorter sequence with same prefix");
    }

    @Test
    void compareEmptyAndNonEmptySequences() {
        CharSequence emptySeq = "";
        CharSequence nonEmptySeq = "test";

        assertTrue(CharSequenceComparator.INSTANCE.compare(emptySeq, nonEmptySeq) < 0, "empty sequence should compare as less than non-empty sequence");
        assertTrue(CharSequenceComparator.INSTANCE.compare(nonEmptySeq, emptySeq) > 0, "non-empty sequence should compare as greater than empty sequence");
    }
}
