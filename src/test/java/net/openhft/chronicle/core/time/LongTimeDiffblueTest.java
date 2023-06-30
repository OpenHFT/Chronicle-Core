package net.openhft.chronicle.core.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class LongTimeDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link LongTime#isSecs(long)}
   */
  @Test
  public void testIsSecs() {
    // Arrange, Act and Assert
    assertTrue(LongTime.isSecs(10L));
    assertFalse(LongTime.isSecs(-1L));
    assertFalse(LongTime.isSecs(LongTime.MAX_NANOS));
  }

  /**
   * Method under test: {@link LongTime#isMillis(long)}
   */
  @Test
  public void testIsMillis() {
    // Arrange, Act and Assert
    assertFalse(LongTime.isMillis(10L));
    assertFalse(LongTime.isMillis(LongTime.MAX_NANOS));
    assertTrue(LongTime.isMillis(LongTime.EPOCH_MILLIS));
  }

  /**
  * Method under test: {@link LongTime#isMicros(long)}
  */
  @Test
  public void testIsMicros() {
    // Arrange, Act and Assert
    assertFalse(LongTime.isMicros(10L));
    assertFalse(LongTime.isMicros(LongTime.MAX_NANOS));
    assertTrue(LongTime.isMicros(LongTime.EPOCH_MICROS));
  }

  /**
   * Method under test: {@link LongTime#isNanos(long)}
   */
  @Test
  public void testIsNanos() {
    // Arrange, Act and Assert
    assertFalse(LongTime.isNanos(10L));
    assertTrue(LongTime.isNanos(LongTime.MAX_NANOS));
  }

  /**
   * Method under test: {@link LongTime#toSecs(long)}
   */
  @Test
  public void testToSecs() {
    // Arrange, Act and Assert
    assertEquals(10L, LongTime.toSecs(10L));
    assertEquals(LongTime.MAX_SECS, LongTime.toSecs(LongTime.MAX_NANOS));
    assertEquals(9223372L, LongTime.toSecs(LongTime.EPOCH_MILLIS));
    assertEquals(9223372L, LongTime.toSecs(LongTime.EPOCH_MICROS));
  }

  /**
   * Method under test: {@link LongTime#toMillis(long)}
   */
  @Test
  public void testToMillis() {
    // Arrange, Act and Assert
    assertEquals(10000L, LongTime.toMillis(10L));
    assertEquals(-1L, LongTime.toMillis(-1L));
    assertEquals(LongTime.MAX_MILLIS, LongTime.toMillis(LongTime.MAX_NANOS));
    assertEquals(LongTime.EPOCH_MILLIS, LongTime.toMillis(LongTime.EPOCH_MILLIS));
    assertEquals(LongTime.EPOCH_MILLIS, LongTime.toMillis(LongTime.EPOCH_MICROS));
  }

  /**
   * Method under test: {@link LongTime#toMicros(long)}
   */
  @Test
  public void testToMicros() {
    // Arrange, Act and Assert
    assertEquals(10000000L, LongTime.toMicros(10L));
    assertEquals(-1L, LongTime.toMicros(-1L));
    assertEquals(LongTime.MAX_MICROS, LongTime.toMicros(LongTime.MAX_NANOS));
    assertEquals(LongTime.EPOCH_MICROS, LongTime.toMicros(LongTime.EPOCH_MILLIS));
    assertEquals(LongTime.EPOCH_MICROS, LongTime.toMicros(LongTime.EPOCH_MICROS));
  }

  /**
   * Method under test: {@link LongTime#toNanos(long)}
   */
  @Test
  public void testToNanos() {
    // Arrange, Act and Assert
    assertEquals(10000000000L, LongTime.toNanos(10L));
    assertEquals(-1511683264L, LongTime.toNanos(-1511683264L));
    assertEquals(LongTime.MAX_NANOS, LongTime.toNanos(LongTime.MAX_NANOS));
    assertEquals(LongTime.EPOCH_NANOS, LongTime.toNanos(LongTime.EPOCH_MILLIS));
    assertEquals(LongTime.EPOCH_NANOS, LongTime.toNanos(LongTime.EPOCH_MICROS));
  }
}

