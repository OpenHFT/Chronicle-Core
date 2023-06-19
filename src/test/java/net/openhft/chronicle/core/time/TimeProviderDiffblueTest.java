package net.openhft.chronicle.core.time;

import static org.junit.Assert.assertEquals;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class TimeProviderDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link TimeProvider#currentTimeMicros()}
  */
  @Test
  public void testCurrentTimeMicros() {
    // Arrange, Act and Assert
    assertEquals(LongTime.EPOCH_SECS, (new SetTimeProvider(1L)).currentTimeMicros());
  }

  /**
   * Method under test: {@link TimeProvider#currentTimeNanos()}
   */
  @Test
  public void testCurrentTimeNanos() {
    // Arrange, Act and Assert
    assertEquals(1L, (new SetTimeProvider(1L)).currentTimeNanos());
  }
}

