package net.openhft.chronicle.core.internal.invariant.longs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.openhft.chronicle.core.util.LongBiPredicate;
import org.junit.Test;

public class LongBiConditionDiffblueTest {
  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate() {
    // Arrange and Act
    LongBiPredicate actualNegateResult = LongBiCondition.EQUAL_TO.negate();

    // Assert
    assertEquals(LongBiCondition.NOT_EQUAL_TO, actualNegateResult);
    assertFalse(actualNegateResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate2() {
    // Arrange and Act
    LongBiPredicate actualNegateResult = LongBiCondition.NOT_EQUAL_TO.negate();

    // Assert
    assertEquals(LongBiCondition.EQUAL_TO, actualNegateResult);
    assertTrue(actualNegateResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate3() {
    // Arrange and Act
    LongBiPredicate actualNegateResult = LongBiCondition.GREATER_THAN.negate();

    // Assert
    assertEquals(LongBiCondition.LESS_OR_EQUAL, actualNegateResult);
    assertTrue(actualNegateResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate4() {
    // Arrange and Act
    LongBiPredicate actualNegateResult = LongBiCondition.GREATER_OR_EQUAL.negate();

    // Assert
    assertEquals(LongBiCondition.LESS_THAN, actualNegateResult);
    assertFalse(actualNegateResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate5() {
    // Arrange and Act
    LongBiPredicate actualNegateResult = LongBiCondition.LESS_THAN.negate();

    // Assert
    assertEquals(LongBiCondition.GREATER_OR_EQUAL, actualNegateResult);
    assertTrue(actualNegateResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate6() {
    // Arrange and Act
    LongBiPredicate actualNegateResult = LongBiCondition.LESS_OR_EQUAL.negate();

    // Assert
    assertEquals(LongBiCondition.GREATER_THAN, actualNegateResult);
    assertFalse(actualNegateResult.test(1L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate7() {
    // Arrange and Act
    LongBiPredicate actualNegateResult = LongBiCondition.EQUAL_TO.negate();

    // Assert
    assertEquals(LongBiCondition.NOT_EQUAL_TO, actualNegateResult);
    assertTrue(actualNegateResult.test(6L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate8() {
    // Arrange and Act
    LongBiPredicate actualNegateResult = LongBiCondition.NOT_EQUAL_TO.negate();

    // Assert
    assertEquals(LongBiCondition.EQUAL_TO, actualNegateResult);
    assertFalse(actualNegateResult.test(6L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate9() {
    // Arrange and Act
    LongBiPredicate actualNegateResult = LongBiCondition.GREATER_THAN.negate();

    // Assert
    assertEquals(LongBiCondition.LESS_OR_EQUAL, actualNegateResult);
    assertFalse(actualNegateResult.test(6L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate10() {
    // Arrange and Act
    LongBiPredicate actualNegateResult = LongBiCondition.GREATER_OR_EQUAL.negate();

    // Assert
    assertEquals(LongBiCondition.LESS_THAN, actualNegateResult);
    assertTrue(actualNegateResult.test(0L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate11() {
    // Arrange and Act
    LongBiPredicate actualNegateResult = LongBiCondition.LESS_THAN.negate();

    // Assert
    assertEquals(LongBiCondition.GREATER_OR_EQUAL, actualNegateResult);
    assertFalse(actualNegateResult.test(0L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate12() {
    // Arrange and Act
    LongBiPredicate actualNegateResult = LongBiCondition.LESS_OR_EQUAL.negate();

    // Assert
    assertEquals(LongBiCondition.GREATER_THAN, actualNegateResult);
    assertTrue(actualNegateResult.test(6L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate13() {
    // Arrange, Act and Assert
    assertTrue(LongBiCondition.BETWEEN_ZERO_AND.negate().test(1L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate14() {
    // Arrange, Act and Assert
    assertFalse(LongBiCondition.BETWEEN_ZERO_AND.negate().test(0L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate15() {
    // Arrange, Act and Assert
    assertTrue(LongBiCondition.BETWEEN_ZERO_AND.negate().test(-1L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate16() {
    // Arrange, Act and Assert
    assertTrue(LongBiCondition.BETWEEN_ZERO_AND_CLOSED.negate().test(1L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate17() {
    // Arrange, Act and Assert
    assertFalse(LongBiCondition.BETWEEN_ZERO_AND_CLOSED.negate().test(0L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate18() {
    // Arrange, Act and Assert
    assertTrue(LongBiCondition.BETWEEN_ZERO_AND_CLOSED.negate().test(-1L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate19() {
    // Arrange, Act and Assert
    assertTrue(LongBiCondition.POWER_OF_TWO.negate().test(1L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate20() {
    // Arrange, Act and Assert
    assertFalse(LongBiCondition.POWER_OF_TWO.negate().test(2L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate21() {
    // Arrange, Act and Assert
    assertTrue(LongBiCondition.POWER_OF_TWO.negate().test(1L, 31L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate22() {
    // Arrange, Act and Assert
    assertTrue(LongBiCondition.LOG2.negate().test(1L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate23() {
    // Arrange, Act and Assert
    assertTrue(LongBiCondition.LOG2.negate().test(31L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#negate()}
   */
  @Test
  public void testNegate24() {
    // Arrange, Act and Assert
    assertFalse(LongBiCondition.LOG2.negate().test(0L, 1L));
  }

  /**
   * Method under test: {@link LongBiCondition#test(long, long)}
   */
  @Test
  public void testTest() {
    // Arrange, Act and Assert
    assertFalse(LongBiCondition.EQUAL_TO.test(42L, 1L));
    assertTrue(LongBiCondition.EQUAL_TO.test(1L, 1L));
    assertTrue(LongBiCondition.NOT_EQUAL_TO.test(42L, 1L));
    assertTrue(LongBiCondition.GREATER_THAN.test(42L, 1L));
    assertTrue(LongBiCondition.GREATER_OR_EQUAL.test(42L, 1L));
    assertFalse(LongBiCondition.LESS_THAN.test(42L, 1L));
    assertFalse(LongBiCondition.NOT_EQUAL_TO.test(1L, 1L));
    assertFalse(LongBiCondition.GREATER_THAN.test(1L, 1L));
    assertFalse(LongBiCondition.GREATER_OR_EQUAL.test(0L, 1L));
    assertTrue(LongBiCondition.LESS_THAN.test(0L, 1L));
    assertFalse(LongBiCondition.LESS_OR_EQUAL.test(42L, 1L));
    assertTrue(LongBiCondition.LESS_OR_EQUAL.test(1L, 1L));
    assertFalse(LongBiCondition.BETWEEN_ZERO_AND.test(42L, 1L));
    assertTrue(LongBiCondition.BETWEEN_ZERO_AND.test(0L, 1L));
    assertFalse(LongBiCondition.BETWEEN_ZERO_AND.test(-1L, 1L));
    assertFalse(LongBiCondition.BETWEEN_ZERO_AND_CLOSED.test(42L, 1L));
    assertTrue(LongBiCondition.BETWEEN_ZERO_AND_CLOSED.test(0L, 1L));
    assertFalse(LongBiCondition.BETWEEN_ZERO_AND_CLOSED.test(-1L, 1L));
    assertFalse(LongBiCondition.POWER_OF_TWO.test(42L, 1L));
    assertTrue(LongBiCondition.POWER_OF_TWO.test(2L, 1L));
    assertFalse(LongBiCondition.POWER_OF_TWO.test(42L, 31L));
    assertFalse(LongBiCondition.LOG2.test(42L, 1L));
    assertFalse(LongBiCondition.LOG2.test(1L, 1L));
    assertTrue(LongBiCondition.LOG2.test(0L, 1L));
  }

  /**
  * Methods under test: 
  * 
  * <ul>
  *   <li>{@link LongBiCondition#valueOf(String)}
  *   <li>{@link LongBiCondition#toString()}
  * </ul>
  */
  @Test
  public void testValueOf() {
    // Arrange, Act and Assert
    assertEquals("==", LongBiCondition.valueOf("EQUAL_TO").toString());
  }
}

