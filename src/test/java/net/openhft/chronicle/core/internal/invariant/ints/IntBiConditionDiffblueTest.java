package net.openhft.chronicle.core.internal.invariant.ints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.openhft.chronicle.core.util.IntBiPredicate;
import org.junit.Test;

public class IntBiConditionDiffblueTest {
  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate() {
    // Arrange and Act
    IntBiPredicate actualNegateResult = IntBiCondition.EQUAL_TO.negate();

    // Assert
    assertEquals(IntBiCondition.NOT_EQUAL_TO, actualNegateResult);
    assertFalse(actualNegateResult.test(1, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate2() {
    // Arrange and Act
    IntBiPredicate actualNegateResult = IntBiCondition.NOT_EQUAL_TO.negate();

    // Assert
    assertEquals(IntBiCondition.EQUAL_TO, actualNegateResult);
    assertTrue(actualNegateResult.test(1, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate3() {
    // Arrange and Act
    IntBiPredicate actualNegateResult = IntBiCondition.GREATER_THAN.negate();

    // Assert
    assertEquals(IntBiCondition.LESS_OR_EQUAL, actualNegateResult);
    assertTrue(actualNegateResult.test(1, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate4() {
    // Arrange and Act
    IntBiPredicate actualNegateResult = IntBiCondition.GREATER_OR_EQUAL.negate();

    // Assert
    assertEquals(IntBiCondition.LESS_THAN, actualNegateResult);
    assertFalse(actualNegateResult.test(1, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate5() {
    // Arrange and Act
    IntBiPredicate actualNegateResult = IntBiCondition.LESS_THAN.negate();

    // Assert
    assertEquals(IntBiCondition.GREATER_OR_EQUAL, actualNegateResult);
    assertTrue(actualNegateResult.test(1, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate6() {
    // Arrange and Act
    IntBiPredicate actualNegateResult = IntBiCondition.LESS_OR_EQUAL.negate();

    // Assert
    assertEquals(IntBiCondition.GREATER_THAN, actualNegateResult);
    assertFalse(actualNegateResult.test(1, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate7() {
    // Arrange and Act
    IntBiPredicate actualNegateResult = IntBiCondition.EQUAL_TO.negate();

    // Assert
    assertEquals(IntBiCondition.NOT_EQUAL_TO, actualNegateResult);
    assertTrue(actualNegateResult.test(6, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate8() {
    // Arrange and Act
    IntBiPredicate actualNegateResult = IntBiCondition.NOT_EQUAL_TO.negate();

    // Assert
    assertEquals(IntBiCondition.EQUAL_TO, actualNegateResult);
    assertFalse(actualNegateResult.test(6, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate9() {
    // Arrange and Act
    IntBiPredicate actualNegateResult = IntBiCondition.GREATER_THAN.negate();

    // Assert
    assertEquals(IntBiCondition.LESS_OR_EQUAL, actualNegateResult);
    assertFalse(actualNegateResult.test(6, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate10() {
    // Arrange and Act
    IntBiPredicate actualNegateResult = IntBiCondition.GREATER_OR_EQUAL.negate();

    // Assert
    assertEquals(IntBiCondition.LESS_THAN, actualNegateResult);
    assertTrue(actualNegateResult.test(0, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate11() {
    // Arrange and Act
    IntBiPredicate actualNegateResult = IntBiCondition.LESS_THAN.negate();

    // Assert
    assertEquals(IntBiCondition.GREATER_OR_EQUAL, actualNegateResult);
    assertFalse(actualNegateResult.test(0, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate12() {
    // Arrange and Act
    IntBiPredicate actualNegateResult = IntBiCondition.LESS_OR_EQUAL.negate();

    // Assert
    assertEquals(IntBiCondition.GREATER_THAN, actualNegateResult);
    assertTrue(actualNegateResult.test(6, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate13() {
    // Arrange, Act and Assert
    assertTrue(IntBiCondition.BETWEEN_ZERO_AND.negate().test(1, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate14() {
    // Arrange, Act and Assert
    assertFalse(IntBiCondition.BETWEEN_ZERO_AND.negate().test(0, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate15() {
    // Arrange, Act and Assert
    assertTrue(IntBiCondition.BETWEEN_ZERO_AND.negate().test(-1, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate16() {
    // Arrange, Act and Assert
    assertTrue(IntBiCondition.BETWEEN_ZERO_AND_CLOSED.negate().test(1, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate17() {
    // Arrange, Act and Assert
    assertFalse(IntBiCondition.BETWEEN_ZERO_AND_CLOSED.negate().test(0, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate18() {
    // Arrange, Act and Assert
    assertTrue(IntBiCondition.BETWEEN_ZERO_AND_CLOSED.negate().test(-1, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate19() {
    // Arrange, Act and Assert
    assertTrue(IntBiCondition.POWER_OF_TWO.negate().test(1, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate20() {
    // Arrange, Act and Assert
    assertFalse(IntBiCondition.POWER_OF_TWO.negate().test(2, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate21() {
    // Arrange, Act and Assert
    assertTrue(IntBiCondition.POWER_OF_TWO.negate().test(1, 31));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate22() {
    // Arrange, Act and Assert
    assertTrue(IntBiCondition.LOG2.negate().test(1, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate23() {
    // Arrange, Act and Assert
    assertTrue(IntBiCondition.LOG2.negate().test(31, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#negate()}
   */
  @Test
  public void testNegate24() {
    // Arrange, Act and Assert
    assertFalse(IntBiCondition.LOG2.negate().test(0, 1));
  }

  /**
   * Method under test: {@link IntBiCondition#test(int, int)}
   */
  @Test
  public void testTest() {
    // Arrange, Act and Assert
    assertFalse(IntBiCondition.EQUAL_TO.test(42, 1));
    assertTrue(IntBiCondition.EQUAL_TO.test(1, 1));
    assertTrue(IntBiCondition.NOT_EQUAL_TO.test(42, 1));
    assertTrue(IntBiCondition.GREATER_THAN.test(42, 1));
    assertTrue(IntBiCondition.GREATER_OR_EQUAL.test(42, 1));
    assertFalse(IntBiCondition.LESS_THAN.test(42, 1));
    assertFalse(IntBiCondition.NOT_EQUAL_TO.test(1, 1));
    assertFalse(IntBiCondition.GREATER_THAN.test(1, 1));
    assertFalse(IntBiCondition.GREATER_OR_EQUAL.test(0, 1));
    assertTrue(IntBiCondition.LESS_THAN.test(0, 1));
    assertFalse(IntBiCondition.LESS_OR_EQUAL.test(42, 1));
    assertTrue(IntBiCondition.LESS_OR_EQUAL.test(1, 1));
    assertFalse(IntBiCondition.BETWEEN_ZERO_AND.test(42, 1));
    assertTrue(IntBiCondition.BETWEEN_ZERO_AND.test(0, 1));
    assertFalse(IntBiCondition.BETWEEN_ZERO_AND.test(-1, 1));
    assertFalse(IntBiCondition.BETWEEN_ZERO_AND_CLOSED.test(42, 1));
    assertTrue(IntBiCondition.BETWEEN_ZERO_AND_CLOSED.test(0, 1));
    assertFalse(IntBiCondition.BETWEEN_ZERO_AND_CLOSED.test(-1, 1));
    assertFalse(IntBiCondition.POWER_OF_TWO.test(42, 1));
    assertTrue(IntBiCondition.POWER_OF_TWO.test(2, 1));
    assertFalse(IntBiCondition.POWER_OF_TWO.test(42, 31));
    assertFalse(IntBiCondition.LOG2.test(42, 1));
    assertFalse(IntBiCondition.LOG2.test(6, 1));
    assertTrue(IntBiCondition.LOG2.test(0, 1));
  }

  /**
  * Methods under test: 
  * 
  * <ul>
  *   <li>{@link IntBiCondition#valueOf(String)}
  *   <li>{@link IntBiCondition#toString()}
  * </ul>
  */
  @Test
  public void testValueOf() {
    // Arrange, Act and Assert
    assertEquals("==", IntBiCondition.valueOf("EQUAL_TO").toString());
  }
}

