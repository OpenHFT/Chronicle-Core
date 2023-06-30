package net.openhft.chronicle.core.internal.invariant.longs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.function.LongPredicate;
import org.junit.Test;

public class LongConditionDiffblueTest {
  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate() {
    // Arrange and Act
    LongPredicate actualNegateResult = LongCondition.POSITIVE.negate();

    // Assert
    assertEquals(LongCondition.NON_POSITIVE, actualNegateResult);
    assertFalse(actualNegateResult.test(1L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate2() {
    // Arrange and Act
    LongPredicate actualNegateResult = LongCondition.NEGATIVE.negate();

    // Assert
    assertEquals(LongCondition.NON_NEGATIVE, actualNegateResult);
    assertTrue(actualNegateResult.test(1L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate3() {
    // Arrange and Act
    LongPredicate actualNegateResult = LongCondition.ZERO.negate();

    // Assert
    assertEquals(LongCondition.NON_ZERO, actualNegateResult);
    assertTrue(actualNegateResult.test(1L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate4() {
    // Arrange and Act
    LongPredicate actualNegateResult = LongCondition.NON_POSITIVE.negate();

    // Assert
    assertEquals(LongCondition.POSITIVE, actualNegateResult);
    assertTrue(actualNegateResult.test(1L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate5() {
    // Arrange and Act
    LongPredicate actualNegateResult = LongCondition.NON_NEGATIVE.negate();

    // Assert
    assertEquals(LongCondition.NEGATIVE, actualNegateResult);
    assertFalse(actualNegateResult.test(1L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate6() {
    // Arrange and Act
    LongPredicate actualNegateResult = LongCondition.NON_ZERO.negate();

    // Assert
    assertEquals(LongCondition.ZERO, actualNegateResult);
    assertFalse(actualNegateResult.test(1L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate7() {
    // Arrange and Act
    LongPredicate actualNegateResult = LongCondition.POSITIVE.negate();

    // Assert
    assertEquals(LongCondition.NON_POSITIVE, actualNegateResult);
    assertTrue(actualNegateResult.test(0L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate8() {
    // Arrange and Act
    LongPredicate actualNegateResult = LongCondition.ZERO.negate();

    // Assert
    assertEquals(LongCondition.NON_ZERO, actualNegateResult);
    assertFalse(actualNegateResult.test(0L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate9() {
    // Arrange and Act
    LongPredicate actualNegateResult = LongCondition.NON_POSITIVE.negate();

    // Assert
    assertEquals(LongCondition.POSITIVE, actualNegateResult);
    assertFalse(actualNegateResult.test(0L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate10() {
    // Arrange and Act
    LongPredicate actualNegateResult = LongCondition.NEGATIVE.negate();

    // Assert
    assertEquals(LongCondition.NON_NEGATIVE, actualNegateResult);
    assertFalse(actualNegateResult.test(-1L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate11() {
    // Arrange and Act
    LongPredicate actualNegateResult = LongCondition.NON_NEGATIVE.negate();

    // Assert
    assertEquals(LongCondition.NEGATIVE, actualNegateResult);
    assertTrue(actualNegateResult.test(-1L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate12() {
    // Arrange and Act
    LongPredicate actualNegateResult = LongCondition.NON_ZERO.negate();

    // Assert
    assertEquals(LongCondition.ZERO, actualNegateResult);
    assertTrue(actualNegateResult.test(0L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate13() {
    // Arrange, Act and Assert
    assertFalse(LongCondition.BYTE_CONVERTIBLE.negate().test(1L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate14() {
    // Arrange, Act and Assert
    assertTrue(LongCondition.BYTE_CONVERTIBLE.negate().test(Long.MAX_VALUE));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate15() {
    // Arrange, Act and Assert
    assertTrue(LongCondition.BYTE_CONVERTIBLE.negate().test(Long.MIN_VALUE));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate16() {
    // Arrange, Act and Assert
    assertFalse(LongCondition.EVEN_POWER_OF_TWO.negate().test(1L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate17() {
    // Arrange, Act and Assert
    assertTrue(LongCondition.EVEN_POWER_OF_TWO.negate().test(5L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate18() {
    // Arrange, Act and Assert
    assertTrue(LongCondition.EVEN_POWER_OF_TWO.negate().test(0L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate19() {
    // Arrange, Act and Assert
    assertTrue(LongCondition.SHORT_ALIGNED.negate().test(1L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate20() {
    // Arrange, Act and Assert
    assertFalse(LongCondition.SHORT_ALIGNED.negate().test(0L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate21() {
    // Arrange, Act and Assert
    assertTrue(LongCondition.INT_ALIGNED.negate().test(1L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate22() {
    // Arrange, Act and Assert
    assertFalse(LongCondition.INT_ALIGNED.negate().test(0L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate23() {
    // Arrange, Act and Assert
    assertTrue(LongCondition.LONG_ALIGNED.negate().test(1L));
  }

  /**
   * Method under test: {@link LongCondition#negate()}
   */
  @Test
  public void testNegate24() {
    // Arrange, Act and Assert
    assertFalse(LongCondition.LONG_ALIGNED.negate().test(0L));
  }

  /**
   * Method under test: {@link LongCondition#test(long)}
   */
  @Test
  public void testTest() {
    // Arrange, Act and Assert
    assertTrue(LongCondition.POSITIVE.test(42L));
    assertFalse(LongCondition.NEGATIVE.test(42L));
    assertFalse(LongCondition.ZERO.test(42L));
    assertFalse(LongCondition.NON_POSITIVE.test(42L));
    assertFalse(LongCondition.POSITIVE.test(0L));
    assertTrue(LongCondition.NON_NEGATIVE.test(42L));
    assertTrue(LongCondition.NEGATIVE.test(-32768L));
    assertTrue(LongCondition.ZERO.test(0L));
    assertTrue(LongCondition.NON_POSITIVE.test(0L));
    assertFalse(LongCondition.NON_NEGATIVE.test(-32768L));
    assertTrue(LongCondition.NON_ZERO.test(42L));
    assertFalse(LongCondition.NON_ZERO.test(0L));
    assertTrue(LongCondition.BYTE_CONVERTIBLE.test(42L));
    assertFalse(LongCondition.BYTE_CONVERTIBLE.test(-32768L));
    assertFalse(LongCondition.BYTE_CONVERTIBLE.test(Long.MAX_VALUE));
    assertFalse(LongCondition.EVEN_POWER_OF_TWO.test(42L));
    assertTrue(LongCondition.EVEN_POWER_OF_TWO.test(1L));
    assertFalse(LongCondition.EVEN_POWER_OF_TWO.test(0L));
    assertTrue(LongCondition.SHORT_ALIGNED.test(42L));
    assertFalse(LongCondition.SHORT_ALIGNED.test(1L));
    assertFalse(LongCondition.INT_ALIGNED.test(42L));
    assertTrue(LongCondition.INT_ALIGNED.test(0L));
    assertFalse(LongCondition.LONG_ALIGNED.test(42L));
    assertTrue(LongCondition.LONG_ALIGNED.test(0L));
  }

  /**
  * Methods under test: 
  * 
  * <ul>
  *   <li>{@link LongCondition#valueOf(String)}
  *   <li>{@link LongCondition#toString()}
  * </ul>
  */
  @Test
  public void testValueOf() {
    // Arrange, Act and Assert
    assertEquals("> 0", LongCondition.valueOf("POSITIVE").toString());
  }
}

