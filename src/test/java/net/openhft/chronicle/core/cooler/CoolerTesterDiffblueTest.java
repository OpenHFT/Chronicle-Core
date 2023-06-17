package net.openhft.chronicle.core.cooler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.concurrent.Callable;
import net.openhft.chronicle.core.util.Histogram;
import org.junit.Test;

public class CoolerTesterDiffblueTest {
  /**
  * Method under test: {@link CoolerTester#CoolerTester(Callable, CpuCooler[])}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    CoolerTester actualCoolerTester = new CoolerTester(mock(Callable.class), mock(CpuCooler.class));

    // Assert
    assertEquals(20000, actualCoolerTester.maxCount());
    assertEquals(5000, actualCoolerTester.runTimeMS());
    assertEquals(10, actualCoolerTester.repeat());
    assertEquals(20, actualCoolerTester.minCount());
  }

  /**
   * Method under test: {@link CoolerTester#CoolerTester(CpuCooler, Callable[])}
   */
  @Test
  public void testConstructor2() {
    // Arrange and Act
    CoolerTester actualCoolerTester = new CoolerTester(mock(CpuCooler.class), mock(Callable.class));

    // Assert
    assertEquals(20000, actualCoolerTester.maxCount());
    assertEquals(5000, actualCoolerTester.runTimeMS());
    assertEquals(10, actualCoolerTester.repeat());
    assertEquals(20, actualCoolerTester.minCount());
  }

  /**
   * Method under test: {@link CoolerTester#innerloop0(Callable, Histogram, long, int, int, int, int)}
   */
  @Test
  public void testInnerloop0() throws Exception {
    // Arrange
    Callable<Object> tested = mock(Callable.class);
    when(tested.call()).thenReturn("Call");
    Histogram histogram = Histogram.timeMicros();

    // Act
    CoolerTester.innerloop0(tested, histogram, Long.MAX_VALUE, 3, 3, 1, 3);

    // Assert
    verify(tested, atLeast(1)).call();
    assertEquals(27L, histogram.totalCount());
    assertEquals(4, histogram.getPercentiles().length);
  }

  /**
   * Method under test: {@link CoolerTester#innerloop1(Callable, CpuCooler, Histogram, long, int, int, int, int)}
   */
  @Test
  public void testInnerloop1() throws Exception {
    // Arrange
    Callable<Object> tested = mock(Callable.class);
    when(tested.call()).thenReturn("Call");
    CpuCooler disturber = mock(CpuCooler.class);
    doNothing().when(disturber).disturb();
    Histogram histogram = Histogram.timeMicros();

    // Act
    CoolerTester.innerloop1(tested, disturber, histogram, 1L, 3, 3, 1, 3);

    // Assert
    verify(tested).call();
    verify(disturber).disturb();
    assertEquals(1L, histogram.totalCount());
    assertEquals(4, histogram.getPercentiles().length);
  }

  /**
   * Method under test: {@link CoolerTester#innerloop1(Callable, CpuCooler, Histogram, long, int, int, int, int)}
   */
  @Test
  public void testInnerloop12() throws Exception {
    // Arrange
    Callable<Object> tested = mock(Callable.class);
    when(tested.call()).thenReturn("Call");
    CpuCooler disturber = mock(CpuCooler.class);
    doThrow(new RuntimeException("foo")).when(disturber).disturb();

    // Act and Assert
    assertThrows(RuntimeException.class,
        () -> CoolerTester.innerloop1(tested, disturber, Histogram.timeMicros(), 1L, 3, 3, 1, 3));
    verify(disturber).disturb();
  }

  /**
   * Method under test: {@link CoolerTester#innerloop1(Callable, CpuCooler, Histogram, long, int, int, int, int)}
   */
  @Test
  public void testInnerloop13() throws Exception {
    // Arrange
    Callable<Object> tested = mock(Callable.class);
    when(tested.call()).thenReturn("Call");
    CpuCooler disturber = mock(CpuCooler.class);
    doNothing().when(disturber).disturb();
    Histogram histogram = Histogram.timeMicros();

    // Act
    CoolerTester.innerloop1(tested, disturber, histogram, Long.MAX_VALUE, 3, 3, 1, 3);

    // Assert
    verify(tested).call();
    verify(disturber).disturb();
    assertEquals(1L, histogram.totalCount());
    assertEquals(4, histogram.getPercentiles().length);
  }

  /**
   * Method under test: {@link CoolerTester#innerloop1(Callable, CpuCooler, Histogram, long, int, int, int, int)}
   */
  @Test
  public void testInnerloop14() throws Exception {
    // Arrange
    Callable<Object> tested = mock(Callable.class);
    when(tested.call()).thenReturn("Call");
    CpuCooler disturber = mock(CpuCooler.class);
    doNothing().when(disturber).disturb();
    Histogram histogram = Histogram.timeMicros();

    // Act
    CoolerTester.innerloop1(tested, disturber, histogram, 1L, 1, 3, 1, 3);

    // Assert
    verify(tested, atLeast(1)).call();
    verify(disturber, atLeast(1)).disturb();
    assertEquals(2L, histogram.totalCount());
    assertEquals(4, histogram.getPercentiles().length);
  }
}

