package net.openhft.chronicle.core.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class SetTimeProviderDiffblueTest extends CoreTestCommon {
  /**
  * Methods under test: 
  * 
  * <ul>
  *   <li>{@link SetTimeProvider#SetTimeProvider()}
  *   <li>{@link SetTimeProvider#toString()}
  * </ul>
  */
  @Test
  public void testConstructor() {
    // Arrange, Act and Assert
    assertEquals("SetTimeProvider{autoIncrement=0, nanoTime=0}", (new SetTimeProvider()).toString());
    assertEquals("SetTimeProvider{autoIncrement=0, nanoTime=1}", (new SetTimeProvider(1L)).toString());
  }

  /**
   * Method under test: {@link SetTimeProvider#SetTimeProvider(Instant)}
   */
  @Test
  public void testConstructor2() {
    // Arrange
    Instant instant = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();

    // Act
    new SetTimeProvider(instant);

    // Assert
    assertSame(instant.EPOCH, instant);
  }

  /**
   * Method under test: {@link SetTimeProvider#initialNanos(Instant)}
   */
  @Test
  public void testInitialNanos() {
    // Arrange, Act and Assert
    assertEquals(LongTime.EPOCH_SECS,
        SetTimeProvider.initialNanos(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
  }

  /**
   * Method under test: {@link SetTimeProvider#autoIncrement(long, TimeUnit)}
   */
  @Test
  public void testAutoIncrement() {
    // Arrange
    SetTimeProvider setTimeProvider = new SetTimeProvider(1L);

    // Act and Assert
    assertSame(setTimeProvider, setTimeProvider.autoIncrement(1L, TimeUnit.NANOSECONDS));
  }

  /**
   * Method under test: {@link SetTimeProvider#currentTimeMillis()}
   */
  @Test
  public void testCurrentTimeMillis() {
    // Arrange, Act and Assert
    assertEquals(LongTime.EPOCH_SECS, (new SetTimeProvider(1L)).currentTimeMillis());
    assertThrows(IllegalArgumentException.class, () -> (new SetTimeProvider(1000000000L)).currentTimeMillis(1L));
  }

  /**
   * Method under test: {@link SetTimeProvider#currentTimeMicros()}
   */
  @Test
  public void testCurrentTimeMicros() {
    // Arrange, Act and Assert
    assertEquals(LongTime.EPOCH_SECS, (new SetTimeProvider(1L)).currentTimeMicros());
    assertThrows(IllegalArgumentException.class, () -> (new SetTimeProvider(1000000000L)).currentTimeMicros(1L));
  }

  /**
   * Method under test: {@link SetTimeProvider#currentTimeNanos()}
   */
  @Test
  public void testCurrentTimeNanos() {
    // Arrange, Act and Assert
    assertEquals(1L, (new SetTimeProvider(1L)).currentTimeNanos());
    assertThrows(IllegalArgumentException.class, () -> (new SetTimeProvider(1000000000L)).currentTimeNanos(1L));
  }

  /**
   * Method under test: {@link SetTimeProvider#currentTime(TimeUnit)}
   */
  @Test
  public void testCurrentTime() {
    // Arrange, Act and Assert
    assertEquals(1L, (new SetTimeProvider(1L)).currentTime(TimeUnit.NANOSECONDS));
  }

  /**
   * Method under test: {@link SetTimeProvider#advanceMillis(long)}
   */
  @Test
  public void testAdvanceMillis() {
    // Arrange
    SetTimeProvider setTimeProvider = new SetTimeProvider(1L);

    // Act and Assert
    assertSame(setTimeProvider, setTimeProvider.advanceMillis(1L));
  }

  /**
   * Method under test: {@link SetTimeProvider#advanceMicros(long)}
   */
  @Test
  public void testAdvanceMicros() {
    // Arrange
    SetTimeProvider setTimeProvider = new SetTimeProvider(1L);

    // Act and Assert
    assertSame(setTimeProvider, setTimeProvider.advanceMicros(1L));
  }

  /**
   * Method under test: {@link SetTimeProvider#advanceNanos(long)}
   */
  @Test
  public void testAdvanceNanos() {
    // Arrange
    SetTimeProvider setTimeProvider = new SetTimeProvider(1L);

    // Act and Assert
    assertSame(setTimeProvider, setTimeProvider.advanceNanos(1L));
  }
}

