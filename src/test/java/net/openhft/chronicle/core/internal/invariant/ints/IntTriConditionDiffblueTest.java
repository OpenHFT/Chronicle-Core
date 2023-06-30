package net.openhft.chronicle.core.internal.invariant.ints;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class IntTriConditionDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link IntTriCondition#test(int, int, int)}
  */
  @Test
  public void testTest() {
    // Arrange, Act and Assert
    assertFalse(IntTriCondition.BETWEEN.test(42, 1, 1));
    assertFalse(IntTriCondition.BETWEEN_CLOSED.test(42, 1, 1));
    assertFalse(IntTriCondition.BETWEEN_ZERO_AND_ENSURING.test(42, 1, 1));
    assertFalse(IntTriCondition.BETWEEN.test(0, 1, 1));
    assertTrue(IntTriCondition.BETWEEN_CLOSED.test(1, 1, 1));
    assertFalse(IntTriCondition.BETWEEN_CLOSED.test(0, 1, 1));
    assertTrue(IntTriCondition.BETWEEN_ZERO_AND_ENSURING.test(0, 1, 1));
    assertFalse(IntTriCondition.BETWEEN_ZERO_AND_ENSURING.test(-1, 1, 1));
    assertTrue(IntTriCondition.BETWEEN.test(0, 0, 1));
  }
}

