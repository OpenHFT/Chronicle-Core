/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CharSequenceComparatorTest {

    @DisplayName("Identical sequences compare equal with comparator")
    @Test
    void compareIdenticalSequences() {
        CharSequence seq1 = "test";
        CharSequence seq2 = "test";

        assertEquals(0, CharSequenceComparator.INSTANCE.compare(seq1, seq2), "comparing identical sequences should return zero");
    }

    @DisplayName("Different same length sequences compare lexicographically")
    @Test
    void compareDifferentSequencesSameLength() {
        CharSequence seq1 = "abc";
        CharSequence seq2 = "abd";

        int forward = CharSequenceComparator.INSTANCE.compare(seq1, seq2);
        int reverse = CharSequenceComparator.INSTANCE.compare(seq2, seq1);
        assertTrue(forward < 0, "lexically earlier sequence should compare as less: comparison=" + forward);
        assertTrue(reverse > 0, "lexically later sequence should compare as greater: comparison=" + reverse);
    }

    @DisplayName("Shorter sequence compares less when prefix matches")
    @Test
    void compareDifferentLengthSequences() {
        CharSequence seq1 = "abc";
        CharSequence seq2 = "abcd";

        int forward = CharSequenceComparator.INSTANCE.compare(seq1, seq2);
        int reverse = CharSequenceComparator.INSTANCE.compare(seq2, seq1);
        assertTrue(forward < 0, "shorter sequence should compare as less: comparison=" + forward);
        assertTrue(reverse > 0, "longer sequence should compare as greater: comparison=" + reverse);
    }

    @DisplayName("Empty sequence sorts before populated sequence order")
    @Test
    void compareEmptyAndNonEmptySequences() {
        CharSequence emptySeq = "";
        CharSequence nonEmptySeq = "test";

        int forward = CharSequenceComparator.INSTANCE.compare(emptySeq, nonEmptySeq);
        int reverse = CharSequenceComparator.INSTANCE.compare(nonEmptySeq, emptySeq);
        assertTrue(forward < 0, "empty sequence should compare as less: comparison=" + forward);
        assertTrue(reverse > 0, "non-empty sequence should compare as greater: comparison=" + reverse);
    }
}
