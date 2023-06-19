package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.function.Function;
import java.util.function.IntPredicate;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.internal.invariant.ints.IntBiCondition;
import net.openhft.chronicle.core.internal.invariant.ints.IntCondition;
import net.openhft.chronicle.core.internal.invariant.ints.IntTriCondition;
import org.junit.Test;
import org.mockito.Mockito;

public class IntsDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link Ints#requireNonNegative(int)}
   */
  @Test
  public void testRequireNonNegative() {
    // Arrange, Act and Assert
    assertEquals(42, Ints.requireNonNegative(42));
    assertThrows(IllegalArgumentException.class, () -> Ints.requireNonNegative(-1));
  }

  /**
   * Method under test: {@link Ints#require(IntPredicate, int)}
   */
  @Test
  public void testRequire() {
    // Arrange
    IntPredicate requirement = mock(IntPredicate.class);
    when(requirement.test(anyInt())).thenReturn(true);

    // Act and Assert
    assertEquals(42, Ints.require(requirement, 42));
    verify(requirement).test(anyInt());
  }

  /**
   * Method under test: {@link Ints#require(IntPredicate, int)}
   */
  @Test
  public void testRequire2() {
    // Arrange
    IntPredicate requirement = mock(IntPredicate.class);
    when(requirement.test(anyInt())).thenReturn(false);

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Ints.require(requirement, 42));
    verify(requirement).test(anyInt());
  }

  /**
   * Method under test: {@link Ints#require(IntPredicate, int)}
   */
  @Test
  public void testRequire3() {
    // Arrange
    IntPredicate requirement = mock(IntPredicate.class);
    when(requirement.test(anyInt())).thenThrow(new IllegalArgumentException(
        "The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s"));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Ints.require(requirement, 42));
    verify(requirement).test(anyInt());
  }

  /**
   * Method under test: {@link Ints#require(IntPredicate, int, Function)}
   */
  @Test
  public void testRequire4() {
    // Arrange
    IntPredicate requirement = mock(IntPredicate.class);
    when(requirement.test(anyInt())).thenReturn(true);

    // Act and Assert
    assertEquals(42, Ints.<RuntimeException>require(requirement, 42, mock(Function.class)));
    verify(requirement).test(anyInt());
  }

  /**
   * Method under test: {@link Ints#require(IntPredicate, int, Function)}
   */
  @Test
  public void testRequire5() {
    // Arrange
    IntPredicate requirement = mock(IntPredicate.class);
    when(requirement.test(anyInt())).thenReturn(false);
    Function<String, RuntimeException> exceptionMapper = mock(Function.class);
    when(exceptionMapper.apply(Mockito.<String>any())).thenThrow(new IllegalArgumentException(
        "The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s"));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Ints.require(requirement, 42, exceptionMapper));
    verify(requirement).test(anyInt());
    verify(exceptionMapper).apply(Mockito.<String>any());
  }

  /**
   * Method under test: {@link Ints#require(IntBiPredicate, int, int)}
   */
  @Test
  public void testRequire6() {
    // Arrange
    IntBiPredicate requirement = mock(IntBiPredicate.class);
    when(requirement.test(anyInt(), anyInt())).thenReturn(true);

    // Act and Assert
    assertEquals(42, Ints.require(requirement, 42, 42));
    verify(requirement).test(anyInt(), anyInt());
  }

  /**
   * Method under test: {@link Ints#require(IntBiPredicate, int, int)}
   */
  @Test
  public void testRequire7() {
    // Arrange
    IntBiPredicate requirement = mock(IntBiPredicate.class);
    when(requirement.test(anyInt(), anyInt())).thenReturn(false);

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Ints.require(requirement, 42, 42));
    verify(requirement).test(anyInt(), anyInt());
  }

  /**
   * Method under test: {@link Ints#require(IntBiPredicate, int, int)}
   */
  @Test
  public void testRequire8() {
    // Arrange
    IntBiPredicate requirement = mock(IntBiPredicate.class);
    when(requirement.test(anyInt(), anyInt())).thenThrow(new IllegalArgumentException(
        "The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s %d"));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Ints.require(requirement, 42, 42));
    verify(requirement).test(anyInt(), anyInt());
  }

  /**
   * Method under test: {@link Ints#require(IntBiPredicate, int, int, Function)}
   */
  @Test
  public void testRequire9() {
    // Arrange
    IntBiPredicate requirement = mock(IntBiPredicate.class);
    when(requirement.test(anyInt(), anyInt())).thenReturn(true);

    // Act and Assert
    assertEquals(42, Ints.<RuntimeException>require(requirement, 42, 42, mock(Function.class)));
    verify(requirement).test(anyInt(), anyInt());
  }

  /**
   * Method under test: {@link Ints#require(IntBiPredicate, int, int, Function)}
   */
  @Test
  public void testRequire10() {
    // Arrange
    IntBiPredicate requirement = mock(IntBiPredicate.class);
    when(requirement.test(anyInt(), anyInt())).thenReturn(false);
    Function<String, RuntimeException> exceptionMapper = mock(Function.class);
    when(exceptionMapper.apply(Mockito.<String>any())).thenThrow(new IllegalArgumentException(
        "The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s %d"));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Ints.require(requirement, 42, 42, exceptionMapper));
    verify(requirement).test(anyInt(), anyInt());
    verify(exceptionMapper).apply(Mockito.<String>any());
  }

  /**
   * Method under test: {@link Ints#require(IntTriPredicate, int, int, int)}
   */
  @Test
  public void testRequire11() {
    // Arrange
    IntTriPredicate requirement = mock(IntTriPredicate.class);
    when(requirement.test(anyInt(), anyInt(), anyInt())).thenReturn(true);

    // Act and Assert
    assertEquals(42, Ints.require(requirement, 42, 42, 42));
    verify(requirement).test(anyInt(), anyInt(), anyInt());
  }

  /**
   * Method under test: {@link Ints#require(IntTriPredicate, int, int, int)}
   */
  @Test
  public void testRequire12() {
    // Arrange
    IntTriPredicate requirement = mock(IntTriPredicate.class);
    when(requirement.test(anyInt(), anyInt(), anyInt())).thenReturn(false);

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Ints.require(requirement, 42, 42, 42));
    verify(requirement).test(anyInt(), anyInt(), anyInt());
  }

  /**
   * Method under test: {@link Ints#require(IntTriPredicate, int, int, int)}
   */
  @Test
  public void testRequire13() {
    // Arrange
    IntTriPredicate requirement = mock(IntTriPredicate.class);
    when(requirement.test(anyInt(), anyInt(), anyInt())).thenThrow(new IllegalArgumentException(
        "The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s"
            + " (%d, %d)"));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Ints.require(requirement, 42, 42, 42));
    verify(requirement).test(anyInt(), anyInt(), anyInt());
  }

  /**
   * Method under test: {@link Ints#require(IntTriPredicate, int, int, int, Function)}
   */
  @Test
  public void testRequire14() {
    // Arrange
    IntTriPredicate requirement = mock(IntTriPredicate.class);
    when(requirement.test(anyInt(), anyInt(), anyInt())).thenReturn(true);

    // Act and Assert
    assertEquals(42, Ints.<RuntimeException>require(requirement, 42, 42, 42, mock(Function.class)));
    verify(requirement).test(anyInt(), anyInt(), anyInt());
  }

  /**
   * Method under test: {@link Ints#require(IntTriPredicate, int, int, int, Function)}
   */
  @Test
  public void testRequire15() {
    // Arrange
    IntTriPredicate requirement = mock(IntTriPredicate.class);
    when(requirement.test(anyInt(), anyInt(), anyInt())).thenReturn(false);
    Function<String, RuntimeException> exceptionMapper = mock(Function.class);
    when(exceptionMapper.apply(Mockito.<String>any())).thenThrow(new IllegalArgumentException(
        "The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s"
            + " (%d, %d)"));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> Ints.require(requirement, 42, 42, 42, exceptionMapper));
    verify(requirement).test(anyInt(), anyInt(), anyInt());
    verify(exceptionMapper).apply(Mockito.<String>any());
  }

  /**
  * Method under test: {@link Ints#assertIfEnabled(IntPredicate, int)}
  */
  @Test
  public void testAssertIfEnabled() {
    // Arrange, Act and Assert
    assertTrue(Ints.assertIfEnabled(mock(IntPredicate.class), 42));
    assertTrue(Ints.assertIfEnabled(mock(IntBiPredicate.class), 42, 42));
    assertTrue(Ints.assertIfEnabled(mock(IntTriPredicate.class), 42, 42, 42));
  }

  /**
   * Method under test: {@link Ints#positive()}
   */
  @Test
  public void testPositive() {
    // Arrange and Act
    IntPredicate actualPositiveResult = Ints.positive();

    // Assert
    assertEquals(IntCondition.POSITIVE, actualPositiveResult);
    assertTrue(actualPositiveResult.test(1));
  }

  /**
   * Method under test: {@link Ints#positive()}
   */
  @Test
  public void testPositive2() {
    // Arrange and Act
    IntPredicate actualPositiveResult = Ints.positive();

    // Assert
    assertEquals(IntCondition.POSITIVE, actualPositiveResult);
    assertFalse(actualPositiveResult.test(0));
  }

  /**
   * Method under test: {@link Ints#negative()}
   */
  @Test
  public void testNegative() {
    // Arrange and Act
    IntPredicate actualNegativeResult = Ints.negative();

    // Assert
    assertEquals(IntCondition.NEGATIVE, actualNegativeResult);
    assertFalse(actualNegativeResult.test(1));
  }

  /**
   * Method under test: {@link Ints#negative()}
   */
  @Test
  public void testNegative2() {
    // Arrange and Act
    IntPredicate actualNegativeResult = Ints.negative();

    // Assert
    assertEquals(IntCondition.NEGATIVE, actualNegativeResult);
    assertTrue(actualNegativeResult.test(-1));
  }

  /**
   * Method under test: {@link Ints#nonNegative()}
   */
  @Test
  public void testNonNegative() {
    // Arrange and Act
    IntPredicate actualNonNegativeResult = Ints.nonNegative();

    // Assert
    assertEquals(IntCondition.NON_NEGATIVE, actualNonNegativeResult);
    assertTrue(actualNonNegativeResult.test(1));
  }

  /**
   * Method under test: {@link Ints#nonNegative()}
   */
  @Test
  public void testNonNegative2() {
    // Arrange and Act
    IntPredicate actualNonNegativeResult = Ints.nonNegative();

    // Assert
    assertEquals(IntCondition.NON_NEGATIVE, actualNonNegativeResult);
    assertFalse(actualNonNegativeResult.test(-1));
  }

  /**
   * Method under test: {@link Ints#zero()}
   */
  @Test
  public void testZero() {
    // Arrange and Act
    IntPredicate actualZeroResult = Ints.zero();

    // Assert
    assertEquals(IntCondition.ZERO, actualZeroResult);
    assertFalse(actualZeroResult.test(1));
  }

  /**
   * Method under test: {@link Ints#zero()}
   */
  @Test
  public void testZero2() {
    // Arrange and Act
    IntPredicate actualZeroResult = Ints.zero();

    // Assert
    assertEquals(IntCondition.ZERO, actualZeroResult);
    assertTrue(actualZeroResult.test(0));
  }

  /**
   * Method under test: {@link Ints#byteConvertible()}
   */
  @Test
  public void testByteConvertible() {
    // Arrange and Act
    IntPredicate actualByteConvertibleResult = Ints.byteConvertible();

    // Assert
    assertEquals(IntCondition.BYTE_CONVERTIBLE, actualByteConvertibleResult);
    assertTrue(actualByteConvertibleResult.test(1));
  }

  /**
   * Method under test: {@link Ints#byteConvertible()}
   */
  @Test
  public void testByteConvertible2() {
    // Arrange and Act
    IntPredicate actualByteConvertibleResult = Ints.byteConvertible();

    // Assert
    assertEquals(IntCondition.BYTE_CONVERTIBLE, actualByteConvertibleResult);
    assertFalse(actualByteConvertibleResult.test(Integer.MIN_VALUE));
  }

  /**
   * Method under test: {@link Ints#shortConvertible()}
   */
  @Test
  public void testShortConvertible() {
    // Arrange and Act
    IntPredicate actualShortConvertibleResult = Ints.shortConvertible();

    // Assert
    assertEquals(IntCondition.SHORT_CONVERTIBLE, actualShortConvertibleResult);
    assertTrue(actualShortConvertibleResult.test(1));
  }

  /**
   * Method under test: {@link Ints#shortConvertible()}
   */
  @Test
  public void testShortConvertible2() {
    // Arrange and Act
    IntPredicate actualShortConvertibleResult = Ints.shortConvertible();

    // Assert
    assertEquals(IntCondition.SHORT_CONVERTIBLE, actualShortConvertibleResult);
    assertFalse(actualShortConvertibleResult.test(Integer.MIN_VALUE));
  }

  /**
   * Method under test: {@link Ints#evenPowerOfTwo()}
   */
  @Test
  public void testEvenPowerOfTwo() {
    // Arrange and Act
    IntPredicate actualEvenPowerOfTwoResult = Ints.evenPowerOfTwo();

    // Assert
    assertEquals(IntCondition.EVEN_POWER_OF_TWO, actualEvenPowerOfTwoResult);
    assertTrue(actualEvenPowerOfTwoResult.test(1));
  }

  /**
   * Method under test: {@link Ints#evenPowerOfTwo()}
   */
  @Test
  public void testEvenPowerOfTwo2() {
    // Arrange and Act
    IntPredicate actualEvenPowerOfTwoResult = Ints.evenPowerOfTwo();

    // Assert
    assertEquals(IntCondition.EVEN_POWER_OF_TWO, actualEvenPowerOfTwoResult);
    assertFalse(actualEvenPowerOfTwoResult.test(5));
  }

  /**
   * Method under test: {@link Ints#shortAligned()}
   */
  @Test
  public void testShortAligned() {
    // Arrange and Act
    IntPredicate actualShortAlignedResult = Ints.shortAligned();

    // Assert
    assertEquals(IntCondition.SHORT_ALIGNED, actualShortAlignedResult);
    assertFalse(actualShortAlignedResult.test(1));
  }

  /**
   * Method under test: {@link Ints#shortAligned()}
   */
  @Test
  public void testShortAligned2() {
    // Arrange and Act
    IntPredicate actualShortAlignedResult = Ints.shortAligned();

    // Assert
    assertEquals(IntCondition.SHORT_ALIGNED, actualShortAlignedResult);
    assertTrue(actualShortAlignedResult.test(0));
  }

  /**
   * Method under test: {@link Ints#intAligned()}
   */
  @Test
  public void testIntAligned() {
    // Arrange and Act
    IntPredicate actualIntAlignedResult = Ints.intAligned();

    // Assert
    assertEquals(IntCondition.INT_ALIGNED, actualIntAlignedResult);
    assertFalse(actualIntAlignedResult.test(1));
  }

  /**
   * Method under test: {@link Ints#intAligned()}
   */
  @Test
  public void testIntAligned2() {
    // Arrange and Act
    IntPredicate actualIntAlignedResult = Ints.intAligned();

    // Assert
    assertEquals(IntCondition.INT_ALIGNED, actualIntAlignedResult);
    assertTrue(actualIntAlignedResult.test(0));
  }

  /**
   * Method under test: {@link Ints#longAligned()}
   */
  @Test
  public void testLongAligned() {
    // Arrange and Act
    IntPredicate actualLongAlignedResult = Ints.longAligned();

    // Assert
    assertEquals(IntCondition.LONG_ALIGNED, actualLongAlignedResult);
    assertFalse(actualLongAlignedResult.test(1));
  }

  /**
   * Method under test: {@link Ints#longAligned()}
   */
  @Test
  public void testLongAligned2() {
    // Arrange and Act
    IntPredicate actualLongAlignedResult = Ints.longAligned();

    // Assert
    assertEquals(IntCondition.LONG_ALIGNED, actualLongAlignedResult);
    assertTrue(actualLongAlignedResult.test(0));
  }

  /**
   * Method under test: {@link Ints#equalTo()}
   */
  @Test
  public void testEqualTo() {
    // Arrange and Act
    IntBiPredicate actualEqualToResult = Ints.equalTo();

    // Assert
    assertEquals(IntBiCondition.EQUAL_TO, actualEqualToResult);
    assertTrue(actualEqualToResult.test(1, 1));
  }

  /**
   * Method under test: {@link Ints#equalTo()}
   */
  @Test
  public void testEqualTo2() {
    // Arrange and Act
    IntBiPredicate actualEqualToResult = Ints.equalTo();

    // Assert
    assertEquals(IntBiCondition.EQUAL_TO, actualEqualToResult);
    assertFalse(actualEqualToResult.test(6, 1));
  }

  /**
   * Method under test: {@link Ints#greaterThan()}
   */
  @Test
  public void testGreaterThan() {
    // Arrange and Act
    IntBiPredicate actualGreaterThanResult = Ints.greaterThan();

    // Assert
    assertEquals(IntBiCondition.GREATER_THAN, actualGreaterThanResult);
    assertFalse(actualGreaterThanResult.test(1, 1));
  }

  /**
   * Method under test: {@link Ints#greaterThan()}
   */
  @Test
  public void testGreaterThan2() {
    // Arrange and Act
    IntBiPredicate actualGreaterThanResult = Ints.greaterThan();

    // Assert
    assertEquals(IntBiCondition.GREATER_THAN, actualGreaterThanResult);
    assertTrue(actualGreaterThanResult.test(6, 1));
  }

  /**
   * Method under test: {@link Ints#greaterOrEqual()}
   */
  @Test
  public void testGreaterOrEqual() {
    // Arrange and Act
    IntBiPredicate actualGreaterOrEqualResult = Ints.greaterOrEqual();

    // Assert
    assertEquals(IntBiCondition.GREATER_OR_EQUAL, actualGreaterOrEqualResult);
    assertTrue(actualGreaterOrEqualResult.test(1, 1));
  }

  /**
   * Method under test: {@link Ints#greaterOrEqual()}
   */
  @Test
  public void testGreaterOrEqual2() {
    // Arrange and Act
    IntBiPredicate actualGreaterOrEqualResult = Ints.greaterOrEqual();

    // Assert
    assertEquals(IntBiCondition.GREATER_OR_EQUAL, actualGreaterOrEqualResult);
    assertFalse(actualGreaterOrEqualResult.test(0, 1));
  }

  /**
   * Method under test: {@link Ints#lessThan()}
   */
  @Test
  public void testLessThan() {
    // Arrange and Act
    IntBiPredicate actualLessThanResult = Ints.lessThan();

    // Assert
    assertEquals(IntBiCondition.LESS_THAN, actualLessThanResult);
    assertFalse(actualLessThanResult.test(1, 1));
  }

  /**
   * Method under test: {@link Ints#lessThan()}
   */
  @Test
  public void testLessThan2() {
    // Arrange and Act
    IntBiPredicate actualLessThanResult = Ints.lessThan();

    // Assert
    assertEquals(IntBiCondition.LESS_THAN, actualLessThanResult);
    assertTrue(actualLessThanResult.test(0, 1));
  }

  /**
   * Method under test: {@link Ints#lessOrEqual()}
   */
  @Test
  public void testLessOrEqual() {
    // Arrange and Act
    IntBiPredicate actualLessOrEqualResult = Ints.lessOrEqual();

    // Assert
    assertEquals(IntBiCondition.LESS_OR_EQUAL, actualLessOrEqualResult);
    assertTrue(actualLessOrEqualResult.test(1, 1));
  }

  /**
   * Method under test: {@link Ints#lessOrEqual()}
   */
  @Test
  public void testLessOrEqual2() {
    // Arrange and Act
    IntBiPredicate actualLessOrEqualResult = Ints.lessOrEqual();

    // Assert
    assertEquals(IntBiCondition.LESS_OR_EQUAL, actualLessOrEqualResult);
    assertFalse(actualLessOrEqualResult.test(6, 1));
  }

  /**
   * Method under test: {@link Ints#betweenZeroAnd()}
   */
  @Test
  public void testBetweenZeroAnd() {
    // Arrange and Act
    IntBiPredicate actualBetweenZeroAndResult = Ints.betweenZeroAnd();

    // Assert
    assertEquals(IntBiCondition.BETWEEN_ZERO_AND, actualBetweenZeroAndResult);
    assertFalse(actualBetweenZeroAndResult.test(1, 1));
  }

  /**
   * Method under test: {@link Ints#betweenZeroAnd()}
   */
  @Test
  public void testBetweenZeroAnd2() {
    // Arrange and Act
    IntBiPredicate actualBetweenZeroAndResult = Ints.betweenZeroAnd();

    // Assert
    assertEquals(IntBiCondition.BETWEEN_ZERO_AND, actualBetweenZeroAndResult);
    assertTrue(actualBetweenZeroAndResult.test(0, 1));
  }

  /**
   * Method under test: {@link Ints#betweenZeroAndClosed()}
   */
  @Test
  public void testBetweenZeroAndClosed() {
    // Arrange and Act
    IntBiPredicate actualBetweenZeroAndClosedResult = Ints.betweenZeroAndClosed();

    // Assert
    assertEquals(IntBiCondition.BETWEEN_ZERO_AND_CLOSED, actualBetweenZeroAndClosedResult);
    assertFalse(actualBetweenZeroAndClosedResult.test(1, 1));
  }

  /**
   * Method under test: {@link Ints#betweenZeroAndClosed()}
   */
  @Test
  public void testBetweenZeroAndClosed2() {
    // Arrange and Act
    IntBiPredicate actualBetweenZeroAndClosedResult = Ints.betweenZeroAndClosed();

    // Assert
    assertEquals(IntBiCondition.BETWEEN_ZERO_AND_CLOSED, actualBetweenZeroAndClosedResult);
    assertTrue(actualBetweenZeroAndClosedResult.test(0, 1));
  }

  /**
   * Method under test: {@link Ints#powerOfTwo()}
   */
  @Test
  public void testPowerOfTwo() {
    // Arrange and Act
    IntBiPredicate actualPowerOfTwoResult = Ints.powerOfTwo();

    // Assert
    assertEquals(IntBiCondition.POWER_OF_TWO, actualPowerOfTwoResult);
    assertFalse(actualPowerOfTwoResult.test(1, 1));
  }

  /**
   * Method under test: {@link Ints#powerOfTwo()}
   */
  @Test
  public void testPowerOfTwo2() {
    // Arrange and Act
    IntBiPredicate actualPowerOfTwoResult = Ints.powerOfTwo();

    // Assert
    assertEquals(IntBiCondition.POWER_OF_TWO, actualPowerOfTwoResult);
    assertTrue(actualPowerOfTwoResult.test(2, 1));
  }

  /**
   * Method under test: {@link Ints#log2()}
   */
  @Test
  public void testLog2() {
    // Arrange and Act
    IntBiPredicate actualLog2Result = Ints.log2();

    // Assert
    assertEquals(IntBiCondition.LOG2, actualLog2Result);
    assertFalse(actualLog2Result.test(1, 1));
  }

  /**
   * Method under test: {@link Ints#log2()}
   */
  @Test
  public void testLog22() {
    // Arrange and Act
    IntBiPredicate actualLog2Result = Ints.log2();

    // Assert
    assertEquals(IntBiCondition.LOG2, actualLog2Result);
    assertTrue(actualLog2Result.test(0, 1));
  }

  /**
   * Method under test: {@link Ints#between()}
   */
  @Test
  public void testBetween() {
    // Arrange and Act
    IntTriPredicate actualBetweenResult = Ints.between();

    // Assert
    assertEquals(IntTriCondition.BETWEEN, actualBetweenResult);
    assertFalse(actualBetweenResult.test(1, 1, 1));
  }

  /**
   * Method under test: {@link Ints#between()}
   */
  @Test
  public void testBetween2() {
    // Arrange and Act
    IntTriPredicate actualBetweenResult = Ints.between();

    // Assert
    assertEquals(IntTriCondition.BETWEEN, actualBetweenResult);
    assertTrue(actualBetweenResult.test(1, 1, 7));
  }

  /**
   * Method under test: {@link Ints#betweenClosed()}
   */
  @Test
  public void testBetweenClosed() {
    // Arrange and Act
    IntTriPredicate actualBetweenClosedResult = Ints.betweenClosed();

    // Assert
    assertEquals(IntTriCondition.BETWEEN_CLOSED, actualBetweenClosedResult);
    assertTrue(actualBetweenClosedResult.test(1, 1, 1));
  }

  /**
   * Method under test: {@link Ints#betweenClosed()}
   */
  @Test
  public void testBetweenClosed2() {
    // Arrange and Act
    IntTriPredicate actualBetweenClosedResult = Ints.betweenClosed();

    // Assert
    assertEquals(IntTriCondition.BETWEEN_CLOSED, actualBetweenClosedResult);
    assertFalse(actualBetweenClosedResult.test(7, 1, 1));
  }

  /**
   * Method under test: {@link Ints#betweenZeroAndReserving()}
   */
  @Test
  public void testBetweenZeroAndReserving() {
    // Arrange and Act
    IntTriPredicate actualBetweenZeroAndReservingResult = Ints.betweenZeroAndReserving();

    // Assert
    assertEquals(IntTriCondition.BETWEEN_ZERO_AND_ENSURING, actualBetweenZeroAndReservingResult);
    assertFalse(actualBetweenZeroAndReservingResult.test(1, 1, 1));
  }

  /**
   * Method under test: {@link Ints#betweenZeroAndReserving()}
   */
  @Test
  public void testBetweenZeroAndReserving2() {
    // Arrange and Act
    IntTriPredicate actualBetweenZeroAndReservingResult = Ints.betweenZeroAndReserving();

    // Assert
    assertEquals(IntTriCondition.BETWEEN_ZERO_AND_ENSURING, actualBetweenZeroAndReservingResult);
    assertTrue(actualBetweenZeroAndReservingResult.test(0, 1, 1));
  }
}

