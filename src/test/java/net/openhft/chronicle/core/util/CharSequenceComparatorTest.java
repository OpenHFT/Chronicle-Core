package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CharSequenceComparatorTest {

    @Test
    void compareIdenticalSequences() {
        CharSequence seq1 = "test";
        CharSequence seq2 = "test";

        assertEquals(0, CharSequenceComparator.INSTANCE.compare(seq1, seq2));
    }

    @Test
    void compareDifferentSequencesSameLength() {
        CharSequence seq1 = "abc";
        CharSequence seq2 = "abd";

        assertTrue(CharSequenceComparator.INSTANCE.compare(seq1, seq2) < 0);
        assertTrue(CharSequenceComparator.INSTANCE.compare(seq2, seq1) > 0);
    }

    @Test
    void compareDifferentLengthSequences() {
        CharSequence seq1 = "abc";
        CharSequence seq2 = "abcd";

        assertTrue(CharSequenceComparator.INSTANCE.compare(seq1, seq2) < 0);
        assertTrue(CharSequenceComparator.INSTANCE.compare(seq2, seq1) > 0);
    }

    @Test
    void compareEmptyAndNonEmptySequences() {
        CharSequence emptySeq = "";
        CharSequence nonEmptySeq = "test";

        assertTrue(CharSequenceComparator.INSTANCE.compare(emptySeq, nonEmptySeq) < 0);
        assertTrue(CharSequenceComparator.INSTANCE.compare(nonEmptySeq, emptySeq) > 0);
    }
}
