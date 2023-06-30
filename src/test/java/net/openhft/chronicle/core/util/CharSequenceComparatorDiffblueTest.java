package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertEquals;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class CharSequenceComparatorDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link CharSequenceComparator#compare(CharSequence, CharSequence)}
  */
  @Test
  public void testCompare() {
    // Arrange
    String o1 = System.lineSeparator();

    // Act and Assert
    assertEquals(0, CharSequenceComparator.INSTANCE.compare(o1, System.lineSeparator()));
  }
}

