package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.function.DoubleFunction;
import org.junit.Test;

public class RecordingHistogramDiffblueTest {
  /**
  * Method under test: default or parameterless constructor of {@link RecordingHistogram}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    RecordingHistogram actualRecordingHistogram = new RecordingHistogram();

    // Assert
    assertEquals(264424L, actualRecordingHistogram.floor());
    assertEquals(6656, actualRecordingHistogram.sampleCount().length);
    assertEquals(26, actualRecordingHistogram.powersOf2());
    assertEquals(8, actualRecordingHistogram.fractionBits());
  }

  /**
   * Method under test: {@link RecordingHistogram#sampleNanos(long)}
   */
  @Test
  public void testSampleNanos() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();

    // Act
    recordingHistogram.sampleNanos(1L);

    // Assert
    assertEquals(1L, recordingHistogram.totalCount());
  }

  /**
   * Method under test: {@link RecordingHistogram#sampleNanos(long)}
   */
  @Test
  public void testSampleNanos2() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();

    // Act
    recordingHistogram.sampleNanos(6656L);

    // Assert
    assertEquals(1L, recordingHistogram.totalCount());
    assertEquals(6664.0d, recordingHistogram.min(), 0.0);
  }

  /**
   * Method under test: {@link RecordingHistogram#sampleNanos(long)}
   */
  @Test
  public void testSampleNanos3() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();

    // Act
    recordingHistogram.sampleNanos(Long.MAX_VALUE);

    // Assert
    assertEquals(1L, recordingHistogram.totalCount());
    assertEquals(1L, recordingHistogram.overRange());
  }

  /**
   * Method under test: {@link RecordingHistogram#toMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToMicrosFormat() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(10.0d);

    // Act and Assert
    assertEquals("{ 50/90 99/99.9 99.99 - worst  was: 10.00 / 10.00  10.00 / 10.00  10.00 - 10.00, top:  }",
        recordingHistogram.toMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link RecordingHistogram#toMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToMicrosFormat2() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(0.5d);

    // Act and Assert
    assertEquals("{ 50/90 99/99.9 99.99 - worst  was: 0.500 / 0.500  0.500 / 0.500  0.500 - 0.500, top:  }",
        recordingHistogram.toMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link RecordingHistogram#toMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToMicrosFormat3() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(100.0d);

    // Act and Assert
    assertEquals("{ 50/90 99/99.9 99.99 - worst  was: 100.0 / 100.0  100.0 / 100.0  100.0 - 100.0, top:  }",
        recordingHistogram.toMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link RecordingHistogram#toMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToMicrosFormat4() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(Double.NaN);

    // Act and Assert
    assertEquals("{ 50/90 99/99.9 99.99 - worst  was: 0 / 0  0 / 0  0 - 0, top:  }",
        recordingHistogram.toMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link RecordingHistogram#toMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToMicrosFormat5() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();
    recordingHistogram.sampleNanos(1000000L);
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(10.0d);

    // Act and Assert
    assertEquals("{ 50/90 99/99.9 99.99 - worst  was: 10.00 / 10.00  10.00 / 10.00  10.00 - 10.00, top: [{ off: 10.0,"
        + " dur: 10.0 }] }", recordingHistogram.toMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link RecordingHistogram#toMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToMicrosFormat6() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();
    recordingHistogram.sampleNanos(Long.MAX_VALUE);
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(10.0d);

    // Act and Assert
    assertEquals("{ 50/90 99/99.9 99.99 - worst  was: 10.00 / 10.00  10.00 / 10.00  10.00 - 10.00, top: [{ off: 10.0,"
        + " dur: 10.0 }] }", recordingHistogram.toMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link RecordingHistogram#toLongMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToLongMicrosFormat() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(10.0d);

    // Act and Assert
    assertEquals("{ 50/90 97/99 99.7/99.9 99.97/99.99 - worst  was: 10.00 / 10.00  10.00 / 10.00  10.00 / 10.00  10.00"
        + " / 10.00 - 10.00, top:  }", recordingHistogram.toLongMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link RecordingHistogram#toLongMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToLongMicrosFormat2() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(0.5d);

    // Act and Assert
    assertEquals("{ 50/90 97/99 99.7/99.9 99.97/99.99 - worst  was: 0.500 / 0.500  0.500 / 0.500  0.500 / 0.500  0.500"
        + " / 0.500 - 0.500, top:  }", recordingHistogram.toLongMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link RecordingHistogram#toLongMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToLongMicrosFormat3() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(100.0d);

    // Act and Assert
    assertEquals("{ 50/90 97/99 99.7/99.9 99.97/99.99 - worst  was: 100.0 / 100.0  100.0 / 100.0  100.0 / 100.0  100.0"
        + " / 100.0 - 100.0, top:  }", recordingHistogram.toLongMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link RecordingHistogram#toLongMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToLongMicrosFormat4() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(Double.NaN);

    // Act and Assert
    assertEquals("{ 50/90 97/99 99.7/99.9 99.97/99.99 - worst  was: 0 / 0  0 / 0  0 / 0  0 / 0 - 0, top:  }",
        recordingHistogram.toLongMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link RecordingHistogram#toLongMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToLongMicrosFormat5() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();
    recordingHistogram.sampleNanos(1000000L);
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(10.0d);

    // Act and Assert
    assertEquals("{ 50/90 97/99 99.7/99.9 99.97/99.99 - worst  was: 10.00 / 10.00  10.00 / 10.00  10.00 / 10.00  10.00"
        + " / 10.00 - 10.00, top: [{ off: 10.0, dur: 10.0 }] }", recordingHistogram.toLongMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link RecordingHistogram#toLongMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToLongMicrosFormat6() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();
    recordingHistogram.sampleNanos(Long.MAX_VALUE);
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(10.0d);

    // Act and Assert
    assertEquals("{ 50/90 97/99 99.7/99.9 99.97/99.99 - worst  was: 10.00 / 10.00  10.00 / 10.00  10.00 / 10.00  10.00"
        + " / 10.00 - 10.00, top: [{ off: 10.0, dur: 10.0 }] }", recordingHistogram.toLongMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link RecordingHistogram#reset()}
   */
  @Test
  public void testReset() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();

    // Act
    recordingHistogram.reset();

    // Assert
    assertEquals(0L, recordingHistogram.totalCount());
    assertEquals(6656, recordingHistogram.sampleCount().length);
    assertEquals(0L, recordingHistogram.overRange());
  }

  /**
   * Method under test: {@link RecordingHistogram.Top10#add(long)}
   */
  @Test
  public void testTop10Add() {
    // Arrange
    RecordingHistogram.Top10 top10 = (new RecordingHistogram()).new Top10();

    // Act
    top10.add(1L);

    // Assert
    assertEquals(1, top10.count);
  }

  /**
   * Method under test: {@link RecordingHistogram.Top10#add(long)}
   */
  @Test
  public void testTop10Add2() {
    // Arrange
    RecordingHistogram.Top10 top10 = (new RecordingHistogram()).new Top10();
    top10.add(1000000L);

    // Act
    top10.add(1L);

    // Assert that nothing has changed
    assertEquals(1, top10.count);
  }

  /**
   * Method under test: {@link RecordingHistogram.Top10#add(long)}
   */
  @Test
  public void testTop10Add3() {
    // Arrange
    RecordingHistogram.Top10 top10 = (new RecordingHistogram()).new Top10();
    top10.add(-1L);

    // Act
    top10.add(1L);

    // Assert
    assertEquals(2, top10.count);
  }

  /**
   * Method under test: {@link RecordingHistogram.Top10#add(long)}
   */
  @Test
  public void testTop10Add4() {
    // Arrange
    RecordingHistogram.Top10 top10 = (new RecordingHistogram()).new Top10();
    top10.add(-1L);
    top10.add(1000000L);

    // Act
    top10.add(1L);

    // Assert
    assertEquals(3, top10.count);
  }

  /**
   * Method under test: {@link RecordingHistogram.Top10#add(long, long)}
   */
  @Test
  public void testTop10Add5() {
    // Arrange
    RecordingHistogram.Top10 top10 = (new RecordingHistogram()).new Top10();

    // Act
    top10.add(10L, 1L);

    // Assert
    assertEquals(1, top10.count);
  }

  /**
   * Method under test: {@link RecordingHistogram.Top10#add(long, long)}
   */
  @Test
  public void testTop10Add6() {
    // Arrange
    RecordingHistogram.Top10 top10 = (new RecordingHistogram()).new Top10();
    top10.add(20L);

    // Act
    top10.add(10L, 1L);

    // Assert
    assertEquals(2, top10.count);
  }

  /**
   * Method under test: {@link RecordingHistogram.Top10#add(long, long)}
   */
  @Test
  public void testTop10Add7() {
    // Arrange
    RecordingHistogram.Top10 top10 = (new RecordingHistogram()).new Top10();
    top10.add(0L);

    // Act
    top10.add(10L, 1L);

    // Assert
    assertEquals(2, top10.count);
  }

  /**
   * Method under test: {@link RecordingHistogram.Top10#asString(DoubleFunction, int)}
   */
  @Test
  public void testTop10AsString() {
    // Arrange, Act and Assert
    assertEquals("", ((new RecordingHistogram()).new Top10()).asString(mock(DoubleFunction.class), 3));
  }

  /**
   * Method under test: {@link RecordingHistogram.Top10#asString(DoubleFunction, int)}
   */
  @Test
  public void testTop10AsString2() {
    // Arrange
    RecordingHistogram.Top10 top10 = (new RecordingHistogram()).new Top10();
    top10.add(1L);
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(10.0d);

    // Act and Assert
    assertEquals("[{ off: 10.0, dur: 10.0 }]", top10.asString(toMicros, 3));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Methods under test: 
   * 
   * <ul>
   *   <li>{@link RecordingHistogram.Top10#Top10(RecordingHistogram)}
   *   <li>{@link RecordingHistogram.Top10#reset()}
   * </ul>
   */
  @Test
  public void testTop10Constructor() {
    // Arrange and Act
    RecordingHistogram.Top10 actualTop10 = (new RecordingHistogram()).new Top10();
    actualTop10.reset();

    // Assert
    assertEquals(0, actualTop10.count);
    assertEquals(20, actualTop10.top.length);
  }

  /**
   * Method under test: {@link RecordingHistogram.Top10#Top10(RecordingHistogram)}
   */
  @Test
  public void testTop10Constructor2() {
    // Arrange, Act and Assert
    assertEquals(20, ((new RecordingHistogram()).new Top10()).top.length);
  }

  /**
   * Method under test: {@link RecordingHistogram#was()}
   */
  @Test
  public void testWas() {
    // Arrange, Act and Assert
    assertEquals(" was: ", (new RecordingHistogram()).was());
  }
}

