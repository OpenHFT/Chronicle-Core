package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.function.DoubleFunction;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class HistogramDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link Histogram#Histogram()}
   */
  @Test
  public void testConstructor() {
    // Arrange and Act
    Histogram actualHistogram = new Histogram();

    // Assert
    assertEquals(261888L, actualHistogram.floor());
    assertEquals(10752, actualHistogram.sampleCount().length);
    assertEquals(42, actualHistogram.powersOf2());
    assertEquals(8, actualHistogram.fractionBits());
  }

  /**
   * Method under test: {@link Histogram#Histogram(int, int)}
   */
  @Test
  public void testConstructor2() {
    // Arrange and Act
    Histogram actualHistogram = new Histogram(1, 1);

    // Assert
    assertEquals(2046L, actualHistogram.floor());
    assertEquals(2, actualHistogram.sampleCount().length);
    assertEquals(1, actualHistogram.powersOf2());
    assertEquals(1, actualHistogram.fractionBits());
  }

  /**
   * Method under test: {@link Histogram#Histogram(int, int)}
   */
  @Test
  public void testConstructor3() {
    // Arrange, Act and Assert
    assertThrows(NegativeArraySizeException.class, () -> new Histogram(-1, 1));

  }

  /**
   * Method under test: {@link Histogram#Histogram(int, int, double)}
   */
  @Test
  public void testConstructor4() {
    // Arrange and Act
    Histogram actualHistogram = new Histogram(1, 1, 10.0d);

    // Assert
    assertEquals(2052L, actualHistogram.floor());
    assertEquals(2, actualHistogram.sampleCount().length);
    assertEquals(1, actualHistogram.powersOf2());
    assertEquals(1, actualHistogram.fractionBits());
  }

  /**
   * Method under test: {@link Histogram#Histogram(int, int, double)}
   */
  @Test
  public void testConstructor5() {
    // Arrange, Act and Assert
    assertThrows(NegativeArraySizeException.class, () -> new Histogram(-1, 1, 10.0d));

  }

  /**
   * Method under test: {@link Histogram#equals(Object)}
   */
  @Test
  public void testEquals() {
    // Arrange, Act and Assert
    assertNotEquals(Histogram.timeMicros(), null);
    assertNotEquals(Histogram.timeMicros(), "Different type to Histogram");
    assertNotEquals(Histogram.timeMicros(), mock(RecordingHistogram.class));
  }

  /**
   * Methods under test: 
   * 
   * <ul>
   *   <li>{@link Histogram#equals(Object)}
   *   <li>{@link Histogram#hashCode()}
   * </ul>
   */
  @Test
  public void testEquals2() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();

    // Act and Assert
    assertEquals(timeMicrosResult, timeMicrosResult);
    int expectedHashCodeResult = timeMicrosResult.hashCode();
    assertEquals(expectedHashCodeResult, timeMicrosResult.hashCode());
  }

  /**
   * Methods under test: 
   * 
   * <ul>
   *   <li>{@link Histogram#equals(Object)}
   *   <li>{@link Histogram#hashCode()}
   * </ul>
   */
  @Test
  public void testEquals3() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();
    Histogram timeMicrosResult2 = Histogram.timeMicros();

    // Act and Assert
    assertEquals(timeMicrosResult, timeMicrosResult2);
    int expectedHashCodeResult = timeMicrosResult.hashCode();
    assertEquals(expectedHashCodeResult, timeMicrosResult2.hashCode());
  }

  /**
   * Method under test: {@link Histogram#equals(Object)}
   */
  @Test
  public void testEquals4() {
    // Arrange
    Histogram histogram = new Histogram();

    // Act and Assert
    assertNotEquals(histogram, Histogram.timeMicros());
  }

  /**
  * Methods under test: 
  * 
  * <ul>
  *   <li>{@link Histogram#timeMicros()}
  *   <li>{@link Histogram#toString()}
  * </ul>
  */
  @Test
  public void testTimeMicros() {
    // Arrange, Act and Assert
    assertEquals("Histogram{fractionBits=3, powersOf2=22, overRange=0, totalCount=0, floor=8263, sampleCount=[0, 0, 0,"
        + " 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,"
        + " 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,"
        + " 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,"
        + " 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,"
        + " 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,"
        + " 0, 0, 0, 0, 0, 0, 0, 0]}", Histogram.timeMicros().toString());
  }

  /**
   * Method under test: {@link Histogram#timeMicros()}
   */
  @Test
  public void testTimeMicros2() {
    // Arrange and Act
    Histogram actualTimeMicrosResult = Histogram.timeMicros();

    // Assert
    assertEquals(8263L, actualTimeMicrosResult.floor());
    assertEquals(176, actualTimeMicrosResult.sampleCount().length);
    assertEquals(22, actualTimeMicrosResult.powersOf2());
    assertEquals(3, actualTimeMicrosResult.fractionBits());
  }

  /**
   * Method under test: {@link Histogram#percentilesFor(long)}
   */
  @Test
  public void testPercentilesFor() {
    // Arrange and Act
    double[] actualPercentilesForResult = Histogram.percentilesFor(3L);

    // Assert
    assertEquals(4, actualPercentilesForResult.length);
    assertEquals(0.5d, actualPercentilesForResult[0], 0.0);
    assertEquals(0.9d, actualPercentilesForResult[1], 0.0);
    assertEquals(0.99d, actualPercentilesForResult[2], 0.0);
    assertEquals(1.0d, actualPercentilesForResult[3], 0.0);
  }

  /**
   * Method under test: {@link Histogram#percentilesFor(long)}
   */
  @Test
  public void testPercentilesFor2() {
    // Arrange and Act
    double[] actualPercentilesForResult = Histogram.percentilesFor(10000000L);

    // Assert
    assertEquals(10, actualPercentilesForResult.length);
    assertEquals(0.5d, actualPercentilesForResult[0], 0.0);
    assertEquals(0.9d, actualPercentilesForResult[1], 0.0);
    assertEquals(0.99d, actualPercentilesForResult[2], 0.0);
    assertEquals(0.997d, actualPercentilesForResult[3], 0.0);
    assertEquals(0.999d, actualPercentilesForResult[4], 0.0);
    assertEquals(0.9997d, actualPercentilesForResult[5], 0.0);
    assertEquals(0.9999d, actualPercentilesForResult[6], 0.0);
    assertEquals(0.99997d, actualPercentilesForResult[7], 0.0);
    assertEquals(0.99999d, actualPercentilesForResult[8], 0.0);
    assertEquals(1.0d, actualPercentilesForResult[9], 0.0);
  }

  /**
   * Method under test: {@link Histogram#percentilesFor(long)}
   */
  @Test
  public void testPercentilesFor3() {
    // Arrange, Act and Assert
    assertEquals(15, Histogram.percentilesFor(Long.MAX_VALUE).length);
  }

  /**
   * Method under test: {@link Histogram#init(int, int, long, long, long)}
   */
  @Test
  public void testInit() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();

    // Act
    timeMicrosResult.init(1, 1, 1L, 3L, 1L);

    // Assert
    assertEquals(1L, timeMicrosResult.floor());
    assertEquals(3L, timeMicrosResult.totalCount());
    assertEquals(1, timeMicrosResult.powersOf2());
    assertEquals(1L, timeMicrosResult.overRange());
    assertEquals(1, timeMicrosResult.fractionBits());
  }

  /**
   * Method under test: {@link Histogram#init(int, int, long, long, long)}
   */
  @Test
  public void testInit2() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();

    // Act
    timeMicrosResult.init(176, 1, 1L, 3L, 1L);

    // Assert
    assertEquals(1L, timeMicrosResult.floor());
    assertEquals(3L, timeMicrosResult.totalCount());
    assertEquals(352, timeMicrosResult.sampleCount().length);
    assertEquals(176, timeMicrosResult.powersOf2());
    assertEquals(1L, timeMicrosResult.overRange());
    assertEquals(1, timeMicrosResult.fractionBits());
  }

  /**
   * Method under test: {@link Histogram#fractionBits()}
   */
  @Test
  public void testFractionBits() {
    // Arrange, Act and Assert
    assertEquals(3, Histogram.timeMicros().fractionBits());
  }

  /**
   * Method under test: {@link Histogram#powersOf2()}
   */
  @Test
  public void testPowersOf2() {
    // Arrange, Act and Assert
    assertEquals(22, Histogram.timeMicros().powersOf2());
  }

  /**
   * Method under test: {@link Histogram#overRange()}
   */
  @Test
  public void testOverRange() {
    // Arrange, Act and Assert
    assertEquals(0L, Histogram.timeMicros().overRange());
  }

  /**
   * Method under test: {@link Histogram#sampleCount()}
   */
  @Test
  public void testSampleCount() {
    // Arrange, Act and Assert
    assertEquals(176, Histogram.timeMicros().sampleCount().length);
  }

  /**
   * Method under test: {@link Histogram#add(Histogram)}
   */
  @Test
  public void testAdd() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();

    // Act
    timeMicrosResult.add(Histogram.timeMicros());

    // Assert
    assertEquals(0L, timeMicrosResult.totalCount());
    assertEquals(0L, timeMicrosResult.overRange());
    assertEquals(1.0d, timeMicrosResult.min(), 0.0);
  }

  /**
   * Method under test: {@link Histogram#sample(double)}
   */
  @Test
  public void testSample() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();

    // Act and Assert
    assertEquals(-53, timeMicrosResult.sample(10.0d));
    assertEquals(1L, timeMicrosResult.totalCount());
  }

  /**
   * Method under test: {@link Histogram#sample(double)}
   */
  @Test
  public void testSample2() {
    // Arrange
    Histogram histogram = new Histogram();

    // Act and Assert
    assertEquals(832, histogram.sample(10.0d));
    assertEquals(1L, histogram.totalCount());
    assertEquals(10.015625d, histogram.min(), 0.0);
  }

  /**
   * Method under test: {@link Histogram#sample(double)}
   */
  @Test
  public void testSample3() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();

    // Act and Assert
    assertEquals(8117, timeMicrosResult.sample(Double.NaN));
    assertEquals(1L, timeMicrosResult.totalCount());
    assertEquals(1L, timeMicrosResult.overRange());
  }

  /**
   * Method under test: {@link Histogram#min()}
   */
  @Test
  public void testMin() {
    // Arrange, Act and Assert
    assertEquals(1.0d, Histogram.timeMicros().min(), 0.0);
  }

  /**
   * Method under test: {@link Histogram#typical()}
   */
  @Test
  public void testTypical() {
    // Arrange, Act and Assert
    assertEquals(1.0d, Histogram.timeMicros().typical(), 0.0);
  }

  /**
   * Method under test: {@link Histogram#max()}
   */
  @Test
  public void testMax() {
    // Arrange, Act and Assert
    assertEquals(1.0d, Histogram.timeMicros().max(), 0.0);
  }

  /**
   * Method under test: {@link Histogram#percentile(double)}
   */
  @Test
  public void testPercentile() {
    // Arrange, Act and Assert
    assertEquals(1.0d, Histogram.timeMicros().percentile(10.0d), 0.0);
    assertEquals(1.0d, Histogram.timeMicros().percentile(0.0d), 0.0);
  }

  /**
   * Method under test: {@link Histogram#percentile(double)}
   */
  @Test
  public void testPercentile2() {
    // Arrange
    RecordingHistogram recordingHistogram = new RecordingHistogram();
    recordingHistogram.sampleNanos(1L);

    // Act and Assert
    assertEquals(Double.POSITIVE_INFINITY, recordingHistogram.percentile(10.0d), 0.0);
  }

  /**
   * Method under test: {@link Histogram#percentageLessThan(double)}
   */
  @Test
  public void testPercentageLessThan() {
    // Arrange, Act and Assert
    assertThrows(ArithmeticException.class, () -> Histogram.timeMicros().percentageLessThan(10.0d));
    assertThrows(ArithmeticException.class, () -> (new Histogram()).percentageLessThan(10.0d));
  }

  /**
   * Method under test: {@link Histogram#getPercentiles()}
   */
  @Test
  public void testGetPercentiles() {
    // Arrange and Act
    double[] actualPercentiles = Histogram.timeMicros().getPercentiles();

    // Assert
    assertEquals(4, actualPercentiles.length);
    assertEquals(1.0d, actualPercentiles[0], 0.0);
    assertEquals(1.0d, actualPercentiles[1], 0.0);
    assertEquals(1.0d, actualPercentiles[2], 0.0);
    assertEquals(1.0d, actualPercentiles[3], 0.0);
  }

  /**
   * Method under test: {@link Histogram#getPercentiles(double[])}
   */
  @Test
  public void testGetPercentiles2() {
    // Arrange and Act
    double[] actualPercentiles = Histogram.timeMicros().getPercentiles(new double[]{10.0d, 0.5d, 10.0d, 0.5d});

    // Assert
    assertEquals(4, actualPercentiles.length);
    assertEquals(1.0d, actualPercentiles[0], 0.0);
    assertEquals(1.0d, actualPercentiles[1], 0.0);
    assertEquals(1.0d, actualPercentiles[2], 0.0);
    assertEquals(1.0d, actualPercentiles[3], 0.0);
  }

  /**
   * Method under test: {@link Histogram#getPercentiles(double[])}
   */
  @Test
  public void testGetPercentiles3() {
    // Arrange and Act
    double[] actualPercentiles = Histogram.timeMicros().getPercentiles(new double[]{0.0d, 0.5d, 10.0d, 0.5d});

    // Assert
    assertEquals(4, actualPercentiles.length);
    assertEquals(1.0d, actualPercentiles[0], 0.0);
    assertEquals(1.0d, actualPercentiles[1], 0.0);
    assertEquals(1.0d, actualPercentiles[2], 0.0);
    assertEquals(1.0d, actualPercentiles[3], 0.0);
  }

  /**
   * Method under test: {@link Histogram#getPercentiles(double[])}
   */
  @Test
  public void testGetPercentiles4() {
    // Arrange, Act and Assert
    assertEquals(0, Histogram.timeMicros().getPercentiles(new double[]{}).length);
  }

  /**
   * Method under test: {@link Histogram#toMicrosFormat()}
   */
  @Test
  public void testToMicrosFormat() {
    // Arrange, Act and Assert
    assertEquals("50/90 99/99.9 99.99 - worst was 0.001 / 0.001  0.001 / 0.001  0.001 - 0.001",
        Histogram.timeMicros().toMicrosFormat());
  }

  /**
   * Method under test: {@link Histogram#toMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToMicrosFormat2() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(10.0d);

    // Act and Assert
    assertEquals("50/90 99/99.9 99.99 - worst was 10 / 10  10 / 10  10 - 10",
        timeMicrosResult.toMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link Histogram#toMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToMicrosFormat3() {
    // Arrange
    Histogram histogram = new Histogram();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(10.0d);

    // Act and Assert
    assertEquals("50/90 99/99.9 99.99 - worst was 10.00 / 10.00  10.00 / 10.00  10.00 - 10.00",
        histogram.toMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link Histogram#toMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToMicrosFormat4() {
    // Arrange
    Histogram histogram = new Histogram(1000000, 1000000);
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(10.0d);

    // Act and Assert
    assertEquals("50/90 99/99.9 99.99 - worst was 10 / 10  10 / 10  10 - 10", histogram.toMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link Histogram#toMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToMicrosFormat5() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(1.0d);

    // Act and Assert
    assertEquals("50/90 99/99.9 99.99 - worst was 1.0 / 1.0  1.0 / 1.0  1.0 - 1.0",
        timeMicrosResult.toMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link Histogram#toMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToMicrosFormat6() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(0.0d);

    // Act and Assert
    assertEquals("50/90 99/99.9 99.99 - worst was 0.000 / 0.000  0.000 / 0.000  0.000 - 0.000",
        timeMicrosResult.toMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link Histogram#toLongMicrosFormat()}
   */
  @Test
  public void testToLongMicrosFormat() {
    // Arrange, Act and Assert
    assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 0.001 / 0.001  0.001 / 0.001  0.001 / 0.001  0.001 /"
        + " 0.001 - 0.001", Histogram.timeMicros().toLongMicrosFormat());
  }

  /**
   * Method under test: {@link Histogram#toLongMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToLongMicrosFormat2() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(10.0d);

    // Act and Assert
    assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 10 / 10  10 / 10  10 / 10  10 / 10 - 10",
        timeMicrosResult.toLongMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link Histogram#toLongMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToLongMicrosFormat3() {
    // Arrange
    Histogram histogram = new Histogram();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(10.0d);

    // Act and Assert
    assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 10.00 / 10.00  10.00 / 10.00  10.00 / 10.00  10.00 /"
        + " 10.00 - 10.00", histogram.toLongMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link Histogram#toLongMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToLongMicrosFormat4() {
    // Arrange
    Histogram histogram = new Histogram(1000000, 1000000);
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(10.0d);

    // Act and Assert
    assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 10 / 10  10 / 10  10 / 10  10 / 10 - 10",
        histogram.toLongMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link Histogram#toLongMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToLongMicrosFormat5() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(1.0d);

    // Act and Assert
    assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 1.0 / 1.0  1.0 / 1.0  1.0 / 1.0  1.0 / 1.0 - 1.0",
        timeMicrosResult.toLongMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link Histogram#toLongMicrosFormat(DoubleFunction)}
   */
  @Test
  public void testToLongMicrosFormat6() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();
    DoubleFunction<Double> toMicros = mock(DoubleFunction.class);
    when(toMicros.apply(anyDouble())).thenReturn(0.0d);

    // Act and Assert
    assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 0.000 / 0.000  0.000 / 0.000  0.000 / 0.000  0.000 /"
        + " 0.000 - 0.000", timeMicrosResult.toLongMicrosFormat(toMicros));
    verify(toMicros, atLeast(1)).apply(anyDouble());
  }

  /**
   * Method under test: {@link Histogram#was()}
   */
  @Test
  public void testWas() {
    // Arrange, Act and Assert
    assertEquals("was ", Histogram.timeMicros().was());
  }

  /**
   * Method under test: {@link Histogram#totalCount()}
   */
  @Test
  public void testTotalCount() {
    // Arrange, Act and Assert
    assertEquals(0L, Histogram.timeMicros().totalCount());
  }

  /**
   * Method under test: {@link Histogram#floor()}
   */
  @Test
  public void testFloor() {
    // Arrange, Act and Assert
    assertEquals(8263L, Histogram.timeMicros().floor());
  }

  /**
   * Method under test: {@link Histogram#reset()}
   */
  @Test
  public void testReset() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();

    // Act
    timeMicrosResult.reset();

    // Assert
    assertEquals(0L, timeMicrosResult.totalCount());
    assertEquals(0L, timeMicrosResult.overRange());
  }

  /**
   * Method under test: {@link Histogram#sampleNanos(long)}
   */
  @Test
  public void testSampleNanos() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();

    // Act
    timeMicrosResult.sampleNanos(1L);

    // Assert
    assertEquals(1L, timeMicrosResult.totalCount());
  }

  /**
   * Method under test: {@link Histogram#sampleNanos(long)}
   */
  @Test
  public void testSampleNanos2() {
    // Arrange
    Histogram histogram = new Histogram();

    // Act
    histogram.sampleNanos(1L);

    // Assert
    assertEquals(1L, histogram.totalCount());
    assertEquals(1.001953125d, histogram.min(), 0.0);
  }

  /**
   * Method under test: {@link Histogram#sampleNanos(long)}
   */
  @Test
  public void testSampleNanos3() {
    // Arrange
    Histogram timeMicrosResult = Histogram.timeMicros();

    // Act
    timeMicrosResult.sampleNanos(Long.MAX_VALUE);

    // Assert
    assertEquals(1L, timeMicrosResult.totalCount());
    assertEquals(1L, timeMicrosResult.overRange());
  }
}

