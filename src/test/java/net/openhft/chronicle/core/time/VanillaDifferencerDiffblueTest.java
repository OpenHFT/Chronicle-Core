package net.openhft.chronicle.core.time;

import static org.junit.Assert.assertEquals;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class VanillaDifferencerDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link VanillaDifferencer#sample(long, long)}
  */
  @Test
  public void testSample() {
    // Arrange, Act and Assert
    assertEquals(LongTime.EPOCH_SECS, (new VanillaDifferencer()).sample(1L, 1L));
    assertEquals(-4L, (new VanillaDifferencer()).sample(5L, 1L));
    assertEquals(1L, (new VanillaDifferencer()).sample(LongTime.EPOCH_SECS, 1L));
    assertEquals(2L, (new VanillaDifferencer()).sample(-1L, 1L));
  }
}

