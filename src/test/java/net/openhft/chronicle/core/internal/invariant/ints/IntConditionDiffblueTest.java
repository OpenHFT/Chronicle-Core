package net.openhft.chronicle.core.internal.invariant.ints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.function.IntPredicate;
import org.junit.Test;

public class IntConditionDiffblueTest {
  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate() {
    // Arrange and Act
    IntPredicate actualNegateResult = IntCondition.POSITIVE.negate();

    // Assert
    assertEquals(IntCondition.NON_POSITIVE, actualNegateResult);
    assertFalse(actualNegateResult.test(1));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate2() {
    // Arrange and Act
    IntPredicate actualNegateResult = IntCondition.NEGATIVE.negate();

    // Assert
    assertEquals(IntCondition.NON_NEGATIVE, actualNegateResult);
    assertTrue(actualNegateResult.test(1));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate3() {
    // Arrange and Act
    IntPredicate actualNegateResult = IntCondition.ZERO.negate();

    // Assert
    assertEquals(IntCondition.NON_ZERO, actualNegateResult);
    assertTrue(actualNegateResult.test(1));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate4() {
    // Arrange and Act
    IntPredicate actualNegateResult = IntCondition.NON_POSITIVE.negate();

    // Assert
    assertEquals(IntCondition.POSITIVE, actualNegateResult);
    assertTrue(actualNegateResult.test(1));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate5() {
    // Arrange and Act
    IntPredicate actualNegateResult = IntCondition.NON_NEGATIVE.negate();

    // Assert
    assertEquals(IntCondition.NEGATIVE, actualNegateResult);
    assertFalse(actualNegateResult.test(1));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate6() {
    // Arrange and Act
    IntPredicate actualNegateResult = IntCondition.NON_ZERO.negate();

    // Assert
    assertEquals(IntCondition.ZERO, actualNegateResult);
    assertFalse(actualNegateResult.test(1));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate7() {
    // Arrange and Act
    IntPredicate actualNegateResult = IntCondition.POSITIVE.negate();

    // Assert
    assertEquals(IntCondition.NON_POSITIVE, actualNegateResult);
    assertTrue(actualNegateResult.test(0));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate8() {
    // Arrange and Act
    IntPredicate actualNegateResult = IntCondition.ZERO.negate();

    // Assert
    assertEquals(IntCondition.NON_ZERO, actualNegateResult);
    assertFalse(actualNegateResult.test(0));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate9() {
    // Arrange and Act
    IntPredicate actualNegateResult = IntCondition.NON_POSITIVE.negate();

    // Assert
    assertEquals(IntCondition.POSITIVE, actualNegateResult);
    assertFalse(actualNegateResult.test(0));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate10() {
    // Arrange and Act
    IntPredicate actualNegateResult = IntCondition.NEGATIVE.negate();

    // Assert
    assertEquals(IntCondition.NON_NEGATIVE, actualNegateResult);
    assertFalse(actualNegateResult.test(-1));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate11() {
    // Arrange and Act
    IntPredicate actualNegateResult = IntCondition.NON_NEGATIVE.negate();

    // Assert
    assertEquals(IntCondition.NEGATIVE, actualNegateResult);
    assertTrue(actualNegateResult.test(-1));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate12() {
    // Arrange and Act
    IntPredicate actualNegateResult = IntCondition.NON_ZERO.negate();

    // Assert
    assertEquals(IntCondition.ZERO, actualNegateResult);
    assertTrue(actualNegateResult.test(0));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate13() {
    // Arrange, Act and Assert
    assertFalse(IntCondition.BYTE_CONVERTIBLE.negate().test(1));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate14() {
    // Arrange, Act and Assert
    assertTrue(IntCondition.BYTE_CONVERTIBLE.negate().test(Integer.MIN_VALUE));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate15() {
    // Arrange, Act and Assert
    assertFalse(IntCondition.EVEN_POWER_OF_TWO.negate().test(1));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate16() {
    // Arrange, Act and Assert
    assertTrue(IntCondition.EVEN_POWER_OF_TWO.negate().test(5));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate17() {
    // Arrange, Act and Assert
    assertTrue(IntCondition.EVEN_POWER_OF_TWO.negate().test(0));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate18() {
    // Arrange, Act and Assert
    assertTrue(IntCondition.SHORT_ALIGNED.negate().test(1));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate19() {
    // Arrange, Act and Assert
    assertFalse(IntCondition.SHORT_ALIGNED.negate().test(0));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate20() {
    // Arrange, Act and Assert
    assertTrue(IntCondition.INT_ALIGNED.negate().test(1));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate21() {
    // Arrange, Act and Assert
    assertFalse(IntCondition.INT_ALIGNED.negate().test(0));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate22() {
    // Arrange, Act and Assert
    assertTrue(IntCondition.LONG_ALIGNED.negate().test(1));
  }

  /**
   * Method under test: {@link IntCondition#negate()}
   */
  @Test
  public void testNegate23() {
    // Arrange, Act and Assert
    assertFalse(IntCondition.LONG_ALIGNED.negate().test(0));
  }

  /**
   * Method under test: {@link IntCondition#test(int)}
   */
  @Test
  public void testTest() {
    // Arrange, Act and Assert
    assertTrue(IntCondition.POSITIVE.test(42));
    assertFalse(IntCondition.NEGATIVE.test(42));
    assertFalse(IntCondition.ZERO.test(42));
    assertFalse(IntCondition.NON_POSITIVE.test(42));
    assertFalse(IntCondition.POSITIVE.test(0));
    assertTrue(IntCondition.NON_NEGATIVE.test(42));
    assertTrue(IntCondition.NEGATIVE.test(-32768));
    assertTrue(IntCondition.ZERO.test(0));
    assertTrue(IntCondition.NON_POSITIVE.test(0));
    assertFalse(IntCondition.NON_NEGATIVE.test(-32768));
    assertTrue(IntCondition.NON_ZERO.test(42));
    assertFalse(IntCondition.NON_ZERO.test(0));
    assertTrue(IntCondition.BYTE_CONVERTIBLE.test(42));
    assertFalse(IntCondition.BYTE_CONVERTIBLE.test(-32768));
    assertFalse(IntCondition.EVEN_POWER_OF_TWO.test(42));
    assertTrue(IntCondition.EVEN_POWER_OF_TWO.test(1));
    assertFalse(IntCondition.EVEN_POWER_OF_TWO.test(0));
    assertTrue(IntCondition.SHORT_ALIGNED.test(42));
    assertFalse(IntCondition.SHORT_ALIGNED.test(1));
    assertFalse(IntCondition.INT_ALIGNED.test(42));
    assertTrue(IntCondition.INT_ALIGNED.test(0));
    assertFalse(IntCondition.LONG_ALIGNED.test(42));
    assertTrue(IntCondition.LONG_ALIGNED.test(0));
  }

  /**
  * Methods under test: 
  * 
  * <ul>
  *   <li>{@link IntCondition#valueOf(String)}
  *   <li>{@link IntCondition#toString()}
  * </ul>
  */
  @Test
  public void testValueOf() {
    // Arrange, Act and Assert
    assertEquals("> 0", IntCondition.valueOf("POSITIVE").toString());
  }
}

