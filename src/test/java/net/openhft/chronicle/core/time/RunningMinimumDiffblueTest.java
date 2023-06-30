package net.openhft.chronicle.core.time;

import static org.junit.Assert.assertEquals;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class RunningMinimumDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link RunningMinimum#RunningMinimum(long)}
  */
  @Test
  public void testConstructor() {
    // Arrange, Act and Assert
    assertEquals(LongTime.MAX_NANOS, (new RunningMinimum(1L)).minimum());
  }

  /**
   * Method under test: {@link RunningMinimum#sample(long, long)}
   */
  @Test
  public void testSample() {
    // Arrange
    RunningMinimum runningMinimum = new RunningMinimum(1L);

    // Act and Assert
    assertEquals(1L, runningMinimum.sample(1L, 1L));
    assertEquals(LongTime.EPOCH_SECS, runningMinimum.minimum());
  }

  /**
   * Method under test: {@link RunningMinimum#sample(long, long)}
   */
  @Test
  public void testSample2() {
    // Arrange
    RunningMinimum runningMinimum = new RunningMinimum(1L);

    // Act and Assert
    assertEquals(1L, runningMinimum.sample(100000L, 1L));
    assertEquals(-99999L, runningMinimum.minimum());
  }

  /**
   * Method under test: {@link RunningMinimum#sample(long, long)}
   */
  @Test
  public void testSample3() {
    // Arrange
    RunningMinimum runningMinimum = new RunningMinimum(1L);

    // Act and Assert
    assertEquals(1L, runningMinimum.sample(-1L, 1L));
    assertEquals(2L, runningMinimum.minimum());
  }

  /**
   * Method under test: {@link RunningMinimum#sample(long, long)}
   */
  @Test
  public void testSample4() {
    // Arrange
    RunningMinimum runningMinimum = new RunningMinimum(1L);

    // Act and Assert
    assertEquals(1L, runningMinimum.sample(5L, 1L));
    assertEquals(-4L, runningMinimum.minimum());
  }

  /**
   * Method under test: {@link RunningMinimum#sample(long, long)}
   */
  @Test
  public void testSample5() {
    // Arrange
    RunningMinimum runningMinimum = new RunningMinimum(1L);

    // Act and Assert
    assertEquals(1L, runningMinimum.sample(Long.MIN_VALUE, 1L));
    assertEquals(-9223372036854775807L, runningMinimum.minimum());
  }

  /**
   * Method under test: {@link RunningMinimum#sample(long, long)}
   */
  @Test
  public void testSample6() {
    // Arrange, Act and Assert
    assertEquals(1L, (new RunningMinimum(1L)).sample(1L, Long.MIN_VALUE));
  }

  /**
   * Method under test: {@link RunningMinimum#minimum()}
   */
  @Test
  public void testMinimum() {
    // Arrange, Act and Assert
    assertEquals(LongTime.MAX_NANOS, (new RunningMinimum(1L)).minimum());
  }
}

