package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.function.Function;
import java.util.function.LongPredicate;
import net.openhft.chronicle.core.internal.invariant.longs.LongBiCondition;
import net.openhft.chronicle.core.internal.invariant.longs.LongCondition;
import net.openhft.chronicle.core.internal.invariant.longs.LongTriCondition;
import org.junit.Test;
import org.mockito.Mockito;

public class LongsDiffblueTest {
  /**
   * Method under test: {@link Longs#requireNonNegative(long)}
   */
  @Test
  public void testRequireNonNegative() {
    // Arrange, Act and Assert
    assertEquals(42L, Longs.requireNonNegative(42L));
    assertThrows(IllegalArgumentException.class, () -> Longs.requireNonNegative(-1L));
  }

  /**
   * Method under test: {@link Longs#require(LongPredicate, long)}
   */
  @Test
  public void testRequire() {
    // Arrange
    LongPredicate requirement = mock(LongPredicate.class);
    when(requirement.test(anyLong())).thenReturn(true);

    // Act and Assert
    assertEquals(42L, Longs.require(requirement, 42L));
    verify(requirement).test(anyLong());
  }

  /**
   * Method under test: {@link Longs#require(LongPredicate, long)}
   */
  @Test
  public void testRequire2() {
    // Arrange
    LongPredicate requirement = mock(LongPredicate.class);
    when(requirement.test(anyLong())).thenReturn(false);

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Longs.require(requirement, 42L));
    verify(requirement).test(anyLong());
  }

  /**
   * Method under test: {@link Longs#require(LongPredicate, long)}
   */
  @Test
  public void testRequire3() {
    // Arrange
    LongPredicate requirement = mock(LongPredicate.class);
    when(requirement.test(anyLong())).thenThrow(new IllegalArgumentException(
        "The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s"));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Longs.require(requirement, 42L));
    verify(requirement).test(anyLong());
  }

  /**
   * Method under test: {@link Longs#require(LongPredicate, long, Function)}
   */
  @Test
  public void testRequire4() {
    // Arrange
    LongPredicate requirement = mock(LongPredicate.class);
    when(requirement.test(anyLong())).thenReturn(true);

    // Act and Assert
    assertEquals(42L, Longs.<RuntimeException>require(requirement, 42L, mock(Function.class)));
    verify(requirement).test(anyLong());
  }

  /**
   * Method under test: {@link Longs#require(LongPredicate, long, Function)}
   */
  @Test
  public void testRequire5() {
    // Arrange
    LongPredicate requirement = mock(LongPredicate.class);
    when(requirement.test(anyLong())).thenReturn(false);
    Function<String, RuntimeException> exceptionMapper = mock(Function.class);
    when(exceptionMapper.apply(Mockito.<String>any())).thenThrow(new IllegalArgumentException(
        "The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s"));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Longs.require(requirement, 42L, exceptionMapper));
    verify(requirement).test(anyLong());
    verify(exceptionMapper).apply(Mockito.<String>any());
  }

  /**
   * Method under test: {@link Longs#require(LongBiPredicate, long, long)}
   */
  @Test
  public void testRequire6() {
    // Arrange
    LongBiPredicate requirement = mock(LongBiPredicate.class);
    when(requirement.test(anyLong(), anyLong())).thenReturn(true);

    // Act and Assert
    assertEquals(42L, Longs.require(requirement, 42L, 42L));
    verify(requirement).test(anyLong(), anyLong());
  }

  /**
   * Method under test: {@link Longs#require(LongBiPredicate, long, long)}
   */
  @Test
  public void testRequire7() {
    // Arrange
    LongBiPredicate requirement = mock(LongBiPredicate.class);
    when(requirement.test(anyLong(), anyLong())).thenReturn(false);

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Longs.require(requirement, 42L, 42L));
    verify(requirement).test(anyLong(), anyLong());
  }

  /**
   * Method under test: {@link Longs#require(LongBiPredicate, long, long)}
   */
  @Test
  public void testRequire8() {
    // Arrange
    LongBiPredicate requirement = mock(LongBiPredicate.class);
    when(requirement.test(anyLong(), anyLong())).thenThrow(new IllegalArgumentException(
        "The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s %d"));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Longs.require(requirement, 42L, 42L));
    verify(requirement).test(anyLong(), anyLong());
  }

  /**
   * Method under test: {@link Longs#require(LongBiPredicate, long, long, Function)}
   */
  @Test
  public void testRequire9() {
    // Arrange
    LongBiPredicate requirement = mock(LongBiPredicate.class);
    when(requirement.test(anyLong(), anyLong())).thenReturn(true);

    // Act and Assert
    assertEquals(42L, Longs.<RuntimeException>require(requirement, 42L, 42L, mock(Function.class)));
    verify(requirement).test(anyLong(), anyLong());
  }

  /**
   * Method under test: {@link Longs#require(LongBiPredicate, long, long, Function)}
   */
  @Test
  public void testRequire10() {
    // Arrange
    LongBiPredicate requirement = mock(LongBiPredicate.class);
    when(requirement.test(anyLong(), anyLong())).thenReturn(false);
    Function<String, RuntimeException> exceptionMapper = mock(Function.class);
    when(exceptionMapper.apply(Mockito.<String>any())).thenThrow(new IllegalArgumentException(
        "The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s %d"));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Longs.require(requirement, 42L, 42L, exceptionMapper));
    verify(requirement).test(anyLong(), anyLong());
    verify(exceptionMapper).apply(Mockito.<String>any());
  }

  /**
   * Method under test: {@link Longs#require(LongTriPredicate, long, long, long)}
   */
  @Test
  public void testRequire11() {
    // Arrange
    LongTriPredicate requirement = mock(LongTriPredicate.class);
    when(requirement.test(anyLong(), anyLong(), anyLong())).thenReturn(true);

    // Act and Assert
    assertEquals(42L, Longs.require(requirement, 42L, 42L, 42L));
    verify(requirement).test(anyLong(), anyLong(), anyLong());
  }

  /**
   * Method under test: {@link Longs#require(LongTriPredicate, long, long, long)}
   */
  @Test
  public void testRequire12() {
    // Arrange
    LongTriPredicate requirement = mock(LongTriPredicate.class);
    when(requirement.test(anyLong(), anyLong(), anyLong())).thenReturn(false);

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Longs.require(requirement, 42L, 42L, 42L));
    verify(requirement).test(anyLong(), anyLong(), anyLong());
  }

  /**
   * Method under test: {@link Longs#require(LongTriPredicate, long, long, long)}
   */
  @Test
  public void testRequire13() {
    // Arrange
    LongTriPredicate requirement = mock(LongTriPredicate.class);
    when(requirement.test(anyLong(), anyLong(), anyLong())).thenThrow(new IllegalArgumentException(
        "The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s"
            + " (%d, %d)"));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Longs.require(requirement, 42L, 42L, 42L));
    verify(requirement).test(anyLong(), anyLong(), anyLong());
  }

  /**
   * Method under test: {@link Longs#require(LongTriPredicate, long, long, long, Function)}
   */
  @Test
  public void testRequire14() {
    // Arrange
    LongTriPredicate requirement = mock(LongTriPredicate.class);
    when(requirement.test(anyLong(), anyLong(), anyLong())).thenReturn(true);

    // Act and Assert
    assertEquals(42L, Longs.<RuntimeException>require(requirement, 42L, 42L, 42L, mock(Function.class)));
    verify(requirement).test(anyLong(), anyLong(), anyLong());
  }

  /**
   * Method under test: {@link Longs#require(LongTriPredicate, long, long, long, Function)}
   */
  @Test
  public void testRequire15() {
    // Arrange
    LongTriPredicate requirement = mock(LongTriPredicate.class);
    when(requirement.test(anyLong(), anyLong(), anyLong())).thenReturn(false);
    Function<String, RuntimeException> exceptionMapper = mock(Function.class);
    when(exceptionMapper.apply(Mockito.<String>any())).thenThrow(new IllegalArgumentException(
        "The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s"
            + " (%d, %d)"));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Longs.require(requirement, 42L, 42L, 42L, exceptionMapper));
    verify(requirement).test(anyLong(), anyLong(), anyLong());
    verify(exceptionMapper).apply(Mockito.<String>any());
  }

  /**
  * Method under test: {@link Longs#assertIfEnabled(LongPredicate, long)}
  */
  @Test
  public void testAssertIfEnabled() {
    // Arrange, Act and Assert
    assertTrue(Longs.assertIfEnabled(mock(LongPredicate.class), 42L));
    assertTrue(Longs.assertIfEnabled(mock(LongBiPredicate.class), 42L, 42L));
    assertTrue(Longs.assertIfEnabled(mock(LongTriPredicate.class), 42L, 42L, 42L));
  }

  /**
   * Method under test: {@link Longs#positive()}
   */
  @Test
  public void testPositive() {
    // Arrange and Act
    LongPredicate actualPositiveResult = Longs.positive();

    // Assert
    assertEquals(LongCondition.POSITIVE, actualPositiveResult);
    assertTrue(actualPositiveResult.test(1L));
  }

  /**
   * Method under test: {@link Longs#positive()}
   */
  @Test
  public void testPositive2() {
    // Arrange and Act
    LongPredicate actualPositiveResult = Longs.positive();

    // Assert
    assertEquals(LongCondition.POSITIVE, actualPositiveResult);
    assertFalse(actualPositiveResult.test(0L));
  }

  /**
   * Method under test: {@link Longs#negative()}
   */
  @Test
  public void testNegative() {
    // Arrange and Act
    LongPredicate actualNegativeResult = Longs.negative();

    // Assert
    assertEquals(LongCondition.NEGATIVE, actualNegativeResult);
    assertFalse(actualNegativeResult.test(1L));
  }

  /**
   * Method under test: {@link Longs#negative()}
   */
  @Test
  public void testNegative2() {
    // Arrange and Act
    LongPredicate actualNegativeResult = Longs.negative();

    // Assert
    assertEquals(LongCondition.NEGATIVE, actualNegativeResult);
    assertTrue(actualNegativeResult.test(-1L));
  }

  /**
   * Method under test: {@link Longs#nonNegative()}
   */
  @Test
  public void testNonNegative() {
    // Arrange and Act
    LongPredicate actualNonNegativeResult = Longs.nonNegative();

    // Assert
    assertEquals(LongCondition.NON_NEGATIVE, actualNonNegativeResult);
    assertTrue(actualNonNegativeResult.test(1L));
  }

  /**
   * Method under test: {@link Longs#nonNegative()}
   */
  @Test
  public void testNonNegative2() {
    // Arrange and Act
    LongPredicate actualNonNegativeResult = Longs.nonNegative();

    // Assert
    assertEquals(LongCondition.NON_NEGATIVE, actualNonNegativeResult);
    assertFalse(actualNonNegativeResult.test(-1L));
  }

  /**
   * Method under test: {@link Longs#zero()}
   */
  @Test
  public void testZero() {
    // Arrange and Act
    LongPredicate actualZeroResult = Longs.zero();

    // Assert
    assertEquals(LongCondition.ZERO, actualZeroResult);
    assertFalse(actualZeroResult.test(1L));
  }

  /**
   * Method under test: {@link Longs#zero()}
   */
  @Test
  public void testZero2() {
    // Arrange and Act
    LongPredicate actualZeroResult = Longs.zero();

    // Assert
    assertEquals(LongCondition.ZERO, actualZeroResult);
    assertTrue(actualZeroResult.test(0L));
  }

  /**
   * Method under test: {@link Longs#byteConvertible()}
   */
  @Test
  public void testByteConvertible() {
    // Arrange and Act
    LongPredicate actualByteConvertibleResult = Longs.byteConvertible();

    // Assert
    assertEquals(LongCondition.BYTE_CONVERTIBLE, actualByteConvertibleResult);
    assertTrue(actualByteConvertibleResult.test(1L));
  }

  /**
   * Method under test: {@link Longs#byteConvertible()}
   */
  @Test
  public void testByteConvertible2() {
    // Arrange and Act
    LongPredicate actualByteConvertibleResult = Longs.byteConvertible();

    // Assert
    assertEquals(LongCondition.BYTE_CONVERTIBLE, actualByteConvertibleResult);
    assertFalse(actualByteConvertibleResult.test(Long.MAX_VALUE));
  }

  /**
   * Method under test: {@link Longs#shortConvertible()}
   */
  @Test
  public void testShortConvertible() {
    // Arrange and Act
    LongPredicate actualShortConvertibleResult = Longs.shortConvertible();

    // Assert
    assertEquals(LongCondition.SHORT_CONVERTIBLE, actualShortConvertibleResult);
    assertTrue(actualShortConvertibleResult.test(1L));
  }

  /**
   * Method under test: {@link Longs#shortConvertible()}
   */
  @Test
  public void testShortConvertible2() {
    // Arrange and Act
    LongPredicate actualShortConvertibleResult = Longs.shortConvertible();

    // Assert
    assertEquals(LongCondition.SHORT_CONVERTIBLE, actualShortConvertibleResult);
    assertFalse(actualShortConvertibleResult.test(Long.MAX_VALUE));
  }

  /**
   * Method under test: {@link Longs#evenPowerOfTwo()}
   */
  @Test
  public void testEvenPowerOfTwo() {
    // Arrange and Act
    LongPredicate actualEvenPowerOfTwoResult = Longs.evenPowerOfTwo();

    // Assert
    assertEquals(LongCondition.EVEN_POWER_OF_TWO, actualEvenPowerOfTwoResult);
    assertTrue(actualEvenPowerOfTwoResult.test(1L));
  }

  /**
   * Method under test: {@link Longs#evenPowerOfTwo()}
   */
  @Test
  public void testEvenPowerOfTwo2() {
    // Arrange and Act
    LongPredicate actualEvenPowerOfTwoResult = Longs.evenPowerOfTwo();

    // Assert
    assertEquals(LongCondition.EVEN_POWER_OF_TWO, actualEvenPowerOfTwoResult);
    assertFalse(actualEvenPowerOfTwoResult.test(5L));
  }

  /**
   * Method under test: {@link Longs#shortAligned()}
   */
  @Test
  public void testShortAligned() {
    // Arrange and Act
    LongPredicate actualShortAlignedResult = Longs.shortAligned();

    // Assert
    assertEquals(LongCondition.SHORT_ALIGNED, actualShortAlignedResult);
    assertFalse(actualShortAlignedResult.test(1L));
  }

  /**
   * Method under test: {@link Longs#shortAligned()}
   */
  @Test
  public void testShortAligned2() {
    // Arrange and Act
    LongPredicate actualShortAlignedResult = Longs.shortAligned();

    // Assert
    assertEquals(LongCondition.SHORT_ALIGNED, actualShortAlignedResult);
    assertTrue(actualShortAlignedResult.test(0L));
  }

  /**
   * Method under test: {@link Longs#intAligned()}
   */
  @Test
  public void testIntAligned() {
    // Arrange and Act
    LongPredicate actualIntAlignedResult = Longs.intAligned();

    // Assert
    assertEquals(LongCondition.INT_ALIGNED, actualIntAlignedResult);
    assertFalse(actualIntAlignedResult.test(1L));
  }

  /**
   * Method under test: {@link Longs#intAligned()}
   */
  @Test
  public void testIntAligned2() {
    // Arrange and Act
    LongPredicate actualIntAlignedResult = Longs.intAligned();

    // Assert
    assertEquals(LongCondition.INT_ALIGNED, actualIntAlignedResult);
    assertTrue(actualIntAlignedResult.test(0L));
  }

  /**
   * Method under test: {@link Longs#longAligned()}
   */
  @Test
  public void testLongAligned() {
    // Arrange and Act
    LongPredicate actualLongAlignedResult = Longs.longAligned();

    // Assert
    assertEquals(LongCondition.LONG_ALIGNED, actualLongAlignedResult);
    assertFalse(actualLongAlignedResult.test(1L));
  }

  /**
   * Method under test: {@link Longs#longAligned()}
   */
  @Test
  public void testLongAligned2() {
    // Arrange and Act
    LongPredicate actualLongAlignedResult = Longs.longAligned();

    // Assert
    assertEquals(LongCondition.LONG_ALIGNED, actualLongAlignedResult);
    assertTrue(actualLongAlignedResult.test(0L));
  }

  /**
   * Method under test: {@link Longs#equalTo()}
   */
  @Test
  public void testEqualTo() {
    // Arrange and Act
    LongBiPredicate actualEqualToResult = Longs.equalTo();

    // Assert
    assertEquals(LongBiCondition.EQUAL_TO, actualEqualToResult);
    assertTrue(actualEqualToResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link Longs#equalTo()}
   */
  @Test
  public void testEqualTo2() {
    // Arrange and Act
    LongBiPredicate actualEqualToResult = Longs.equalTo();

    // Assert
    assertEquals(LongBiCondition.EQUAL_TO, actualEqualToResult);
    assertFalse(actualEqualToResult.test(6L, 1L));
  }

  /**
   * Method under test: {@link Longs#greaterThan()}
   */
  @Test
  public void testGreaterThan() {
    // Arrange and Act
    LongBiPredicate actualGreaterThanResult = Longs.greaterThan();

    // Assert
    assertEquals(LongBiCondition.GREATER_THAN, actualGreaterThanResult);
    assertFalse(actualGreaterThanResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link Longs#greaterThan()}
   */
  @Test
  public void testGreaterThan2() {
    // Arrange and Act
    LongBiPredicate actualGreaterThanResult = Longs.greaterThan();

    // Assert
    assertEquals(LongBiCondition.GREATER_THAN, actualGreaterThanResult);
    assertTrue(actualGreaterThanResult.test(6L, 1L));
  }

  /**
   * Method under test: {@link Longs#greaterOrEqual()}
   */
  @Test
  public void testGreaterOrEqual() {
    // Arrange and Act
    LongBiPredicate actualGreaterOrEqualResult = Longs.greaterOrEqual();

    // Assert
    assertEquals(LongBiCondition.GREATER_OR_EQUAL, actualGreaterOrEqualResult);
    assertTrue(actualGreaterOrEqualResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link Longs#greaterOrEqual()}
   */
  @Test
  public void testGreaterOrEqual2() {
    // Arrange and Act
    LongBiPredicate actualGreaterOrEqualResult = Longs.greaterOrEqual();

    // Assert
    assertEquals(LongBiCondition.GREATER_OR_EQUAL, actualGreaterOrEqualResult);
    assertFalse(actualGreaterOrEqualResult.test(0L, 1L));
  }

  /**
   * Method under test: {@link Longs#lessThan()}
   */
  @Test
  public void testLessThan() {
    // Arrange and Act
    LongBiPredicate actualLessThanResult = Longs.lessThan();

    // Assert
    assertEquals(LongBiCondition.LESS_THAN, actualLessThanResult);
    assertFalse(actualLessThanResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link Longs#lessThan()}
   */
  @Test
  public void testLessThan2() {
    // Arrange and Act
    LongBiPredicate actualLessThanResult = Longs.lessThan();

    // Assert
    assertEquals(LongBiCondition.LESS_THAN, actualLessThanResult);
    assertTrue(actualLessThanResult.test(0L, 1L));
  }

  /**
   * Method under test: {@link Longs#lessOrEqual()}
   */
  @Test
  public void testLessOrEqual() {
    // Arrange and Act
    LongBiPredicate actualLessOrEqualResult = Longs.lessOrEqual();

    // Assert
    assertEquals(LongBiCondition.LESS_OR_EQUAL, actualLessOrEqualResult);
    assertTrue(actualLessOrEqualResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link Longs#lessOrEqual()}
   */
  @Test
  public void testLessOrEqual2() {
    // Arrange and Act
    LongBiPredicate actualLessOrEqualResult = Longs.lessOrEqual();

    // Assert
    assertEquals(LongBiCondition.LESS_OR_EQUAL, actualLessOrEqualResult);
    assertFalse(actualLessOrEqualResult.test(6L, 1L));
  }

  /**
   * Method under test: {@link Longs#betweenZeroAnd()}
   */
  @Test
  public void testBetweenZeroAnd() {
    // Arrange and Act
    LongBiPredicate actualBetweenZeroAndResult = Longs.betweenZeroAnd();

    // Assert
    assertEquals(LongBiCondition.BETWEEN_ZERO_AND, actualBetweenZeroAndResult);
    assertFalse(actualBetweenZeroAndResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link Longs#betweenZeroAnd()}
   */
  @Test
  public void testBetweenZeroAnd2() {
    // Arrange and Act
    LongBiPredicate actualBetweenZeroAndResult = Longs.betweenZeroAnd();

    // Assert
    assertEquals(LongBiCondition.BETWEEN_ZERO_AND, actualBetweenZeroAndResult);
    assertTrue(actualBetweenZeroAndResult.test(0L, 1L));
  }

  /**
   * Method under test: {@link Longs#betweenZeroAndClosed()}
   */
  @Test
  public void testBetweenZeroAndClosed() {
    // Arrange and Act
    LongBiPredicate actualBetweenZeroAndClosedResult = Longs.betweenZeroAndClosed();

    // Assert
    assertEquals(LongBiCondition.BETWEEN_ZERO_AND_CLOSED, actualBetweenZeroAndClosedResult);
    assertFalse(actualBetweenZeroAndClosedResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link Longs#betweenZeroAndClosed()}
   */
  @Test
  public void testBetweenZeroAndClosed2() {
    // Arrange and Act
    LongBiPredicate actualBetweenZeroAndClosedResult = Longs.betweenZeroAndClosed();

    // Assert
    assertEquals(LongBiCondition.BETWEEN_ZERO_AND_CLOSED, actualBetweenZeroAndClosedResult);
    assertTrue(actualBetweenZeroAndClosedResult.test(0L, 1L));
  }

  /**
   * Method under test: {@link Longs#powerOfTwo()}
   */
  @Test
  public void testPowerOfTwo() {
    // Arrange and Act
    LongBiPredicate actualPowerOfTwoResult = Longs.powerOfTwo();

    // Assert
    assertEquals(LongBiCondition.POWER_OF_TWO, actualPowerOfTwoResult);
    assertFalse(actualPowerOfTwoResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link Longs#powerOfTwo()}
   */
  @Test
  public void testPowerOfTwo2() {
    // Arrange and Act
    LongBiPredicate actualPowerOfTwoResult = Longs.powerOfTwo();

    // Assert
    assertEquals(LongBiCondition.POWER_OF_TWO, actualPowerOfTwoResult);
    assertTrue(actualPowerOfTwoResult.test(2L, 1L));
  }

  /**
   * Method under test: {@link Longs#log2()}
   */
  @Test
  public void testLog2() {
    // Arrange and Act
    LongBiPredicate actualLog2Result = Longs.log2();

    // Assert
    assertEquals(LongBiCondition.LOG2, actualLog2Result);
    assertFalse(actualLog2Result.test(1L, 1L));
  }

  /**
   * Method under test: {@link Longs#log2()}
   */
  @Test
  public void testLog22() {
    // Arrange and Act
    LongBiPredicate actualLog2Result = Longs.log2();

    // Assert
    assertEquals(LongBiCondition.LOG2, actualLog2Result);
    assertTrue(actualLog2Result.test(0L, 1L));
  }

  /**
   * Method under test: {@link Longs#between()}
   */
  @Test
  public void testBetween() {
    // Arrange and Act
    LongTriPredicate actualBetweenResult = Longs.between();

    // Assert
    assertEquals(LongTriCondition.BETWEEN, actualBetweenResult);
    assertFalse(actualBetweenResult.test(1L, 1L, 1L));
  }

  /**
   * Method under test: {@link Longs#between()}
   */
  @Test
  public void testBetween2() {
    // Arrange and Act
    LongTriPredicate actualBetweenResult = Longs.between();

    // Assert
    assertEquals(LongTriCondition.BETWEEN, actualBetweenResult);
    assertTrue(actualBetweenResult.test(1L, 1L, 7L));
  }

  /**
   * Method under test: {@link Longs#betweenClosed()}
   */
  @Test
  public void testBetweenClosed() {
    // Arrange and Act
    LongTriPredicate actualBetweenClosedResult = Longs.betweenClosed();

    // Assert
    assertEquals(LongTriCondition.BETWEEN_CLOSED, actualBetweenClosedResult);
    assertTrue(actualBetweenClosedResult.test(1L, 1L, 1L));
  }

  /**
   * Method under test: {@link Longs#betweenClosed()}
   */
  @Test
  public void testBetweenClosed2() {
    // Arrange and Act
    LongTriPredicate actualBetweenClosedResult = Longs.betweenClosed();

    // Assert
    assertEquals(LongTriCondition.BETWEEN_CLOSED, actualBetweenClosedResult);
    assertFalse(actualBetweenClosedResult.test(7L, 1L, 1L));
  }

  /**
   * Method under test: {@link Longs#betweenZeroAndReserving()}
   */
  @Test
  public void testBetweenZeroAndReserving() {
    // Arrange and Act
    LongTriPredicate actualBetweenZeroAndReservingResult = Longs.betweenZeroAndReserving();

    // Assert
    assertEquals(LongTriCondition.BETWEEN_ZERO_AND_ENSURING, actualBetweenZeroAndReservingResult);
    assertFalse(actualBetweenZeroAndReservingResult.test(1L, 1L, 1L));
  }

  /**
   * Method under test: {@link Longs#betweenZeroAndReserving()}
   */
  @Test
  public void testBetweenZeroAndReserving2() {
    // Arrange and Act
    LongTriPredicate actualBetweenZeroAndReservingResult = Longs.betweenZeroAndReserving();

    // Assert
    assertEquals(LongTriCondition.BETWEEN_ZERO_AND_ENSURING, actualBetweenZeroAndReservingResult);
    assertTrue(actualBetweenZeroAndReservingResult.test(0L, 1L, 1L));
  }
}

