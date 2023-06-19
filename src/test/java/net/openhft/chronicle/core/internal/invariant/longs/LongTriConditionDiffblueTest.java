package net.openhft.chronicle.core.internal.invariant.longs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class LongTriConditionDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link LongTriCondition#test(long, long, long)}
  */
  @Test
  public void testTest() {
    // Arrange, Act and Assert
    assertFalse(LongTriCondition.BETWEEN.test(42L, 1L, 1L));
    assertFalse(LongTriCondition.BETWEEN_CLOSED.test(42L, 1L, 1L));
    assertFalse(LongTriCondition.BETWEEN_ZERO_AND_ENSURING.test(42L, 1L, 1L));
    assertFalse(LongTriCondition.BETWEEN.test(0L, 1L, 1L));
    assertTrue(LongTriCondition.BETWEEN.test(42L, 1L, Long.MAX_VALUE));
    assertTrue(LongTriCondition.BETWEEN_CLOSED.test(1L, 1L, 1L));
    assertFalse(LongTriCondition.BETWEEN_CLOSED.test(0L, 1L, 1L));
    assertTrue(LongTriCondition.BETWEEN_ZERO_AND_ENSURING.test(0L, 1L, 1L));
    assertFalse(LongTriCondition.BETWEEN_ZERO_AND_ENSURING.test(-1L, 1L, 1L));
  }
}

