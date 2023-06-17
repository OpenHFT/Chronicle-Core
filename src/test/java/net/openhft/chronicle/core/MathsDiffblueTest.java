package net.openhft.chronicle.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class MathsDiffblueTest {
  /**
   * Method under test: {@link Maths#roundN(double, double)}
   */
  @Test
  public void testRoundN() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.roundN(10.0d, 10.0d), 0.0);
    assertEquals(0.5d, Maths.roundN(0.5d, 10.0d), 0.0);
    assertEquals(9.223372036854776E18d, Maths.roundN(9.223372036854776E18d, 10.0d), 0.0);
    assertEquals(9.223372036854776E8d, Maths.roundN(9.223372036854776E8d, 10.0d), 0.0);
    assertEquals(-0.5d, Maths.roundN(-0.5d, 10.0d), 0.0);
    assertEquals(10.0d, Maths.roundN(10.0d, 0.5d), 0.0);
    assertEquals(10.0d, Maths.roundN(10.0d, -0.5d), 0.0);
  }

  /**
   * Method under test: {@link Maths#roundingFactor(double)}
   */
  @Test
  public void testRoundingFactor() {
    // Arrange, Act and Assert
    assertEquals(10000000000L, Maths.roundingFactor(10.0d));
    assertEquals(2L, Maths.roundingFactor(0.5d));
    assertEquals(10L, Maths.roundingFactor(-0.5d));
  }

  /**
   * Method under test: {@link Maths#ceilN(double, double)}
   */
  @Test
  public void testCeilN() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.ceilN(10.0d, 10.0d), 0.0);
    assertEquals(8.0d, Maths.ceilN(8.0d, 10.0d), 0.0);
    assertEquals(0.5d, Maths.ceilN(0.5d, 10.0d), 0.0);
    assertEquals(4.503599627370496E15d, Maths.ceilN(4.503599627370496E15d, 10.0d), 0.0);
    assertEquals(10.0d, Maths.ceilN(10.0d, 0.5d), 0.0);
    assertEquals(0.2d, Maths.ceilN(10.0d, -0.5d), 0.0);
  }

  /**
   * Method under test: {@link Maths#floorN(double, double)}
   */
  @Test
  public void testFloorN() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.floorN(10.0d, 10.0d), 0.0);
    assertEquals(8.0d, Maths.floorN(8.0d, 10.0d), 0.0);
    assertEquals(0.5d, Maths.floorN(0.5d, 10.0d), 0.0);
    assertEquals(4.503599627370496E15d, Maths.floorN(4.503599627370496E15d, 10.0d), 0.0);
    assertEquals(10.0d, Maths.floorN(10.0d, 0.5d), 0.0);
    assertEquals(0.2d, Maths.floorN(10.0d, -0.5d), 0.0);
  }

  /**
   * Method under test: {@link Maths#round1(double)}
   */
  @Test
  public void testRound1() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round1(10.0d), 0.0);
    assertEquals(-0.5d, Maths.round1(-0.5d), 0.0);
    assertEquals(Double.NaN, Maths.round1(Double.NaN), 0.0);
  }

  /**
   * Method under test: {@link Maths#round1up(double)}
   */
  @Test
  public void testRound1up() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round1up(10.0d), 0.0);
    assertEquals(-0.5d, Maths.round1up(-0.5d), 0.0);
    assertEquals(Double.NaN, Maths.round1up(Double.NaN), 0.0);
  }

  /**
   * Method under test: {@link Maths#round2(double)}
   */
  @Test
  public void testRound2() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round2(10.0d), 0.0);
    assertEquals(4.503599627370496E13d, Maths.round2(4.503599627370496E13d), 0.0);
    assertEquals(-0.5d, Maths.round2(-0.5d), 0.0);
  }

  /**
   * Method under test: {@link Maths#round2up(double)}
   */
  @Test
  public void testRound2up() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round2up(10.0d), 0.0);
    assertEquals(4.503599627370497E13d, Maths.round2up(4.503599627370496E13d), 0.0);
    assertEquals(-0.5d, Maths.round2up(-0.5d), 0.0);
    assertEquals(Double.NaN, Maths.round2up(Double.NaN), 0.0);
  }

  /**
   * Method under test: {@link Maths#round3(double)}
   */
  @Test
  public void testRound3() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round3(10.0d), 0.0);
    assertEquals(4.503599627370496E12d, Maths.round3(4.503599627370496E12d), 0.0);
    assertEquals(-0.5d, Maths.round3(-0.5d), 0.0);
  }

  /**
   * Method under test: {@link Maths#round3up(double)}
   */
  @Test
  public void testRound3up() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round3up(10.0d), 0.0);
    assertEquals(4.503599627370497E12d, Maths.round3up(4.503599627370496E12d), 0.0);
    assertEquals(-0.5d, Maths.round3up(-0.5d), 0.0);
    assertEquals(Double.NaN, Maths.round3up(Double.NaN), 0.0);
  }

  /**
   * Method under test: {@link Maths#round4(double)}
   */
  @Test
  public void testRound4() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round4(10.0d), 0.0);
    assertEquals(4.503599627370496E11d, Maths.round4(4.503599627370496E11d), 0.0);
    assertEquals(-0.5d, Maths.round4(-0.5d), 0.0);
  }

  /**
   * Method under test: {@link Maths#round4up(double)}
   */
  @Test
  public void testRound4up() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round4up(10.0d), 0.0);
    assertEquals(4.503599627370497E11d, Maths.round4up(4.503599627370496E11d), 0.0);
    assertEquals(-0.5d, Maths.round4up(-0.5d), 0.0);
    assertEquals(Double.NaN, Maths.round4up(Double.NaN), 0.0);
  }

  /**
   * Method under test: {@link Maths#round5(double)}
   */
  @Test
  public void testRound5() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round5(10.0d), 0.0);
    assertEquals(4.503599627370496E10d, Maths.round5(4.503599627370496E10d), 0.0);
    assertEquals(-0.5d, Maths.round5(-0.5d), 0.0);
  }

  /**
   * Method under test: {@link Maths#round5up(double)}
   */
  @Test
  public void testRound5up() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round5up(10.0d), 0.0);
    assertEquals(4.503599627370497E10d, Maths.round5up(4.503599627370496E10d), 0.0);
    assertEquals(-0.5d, Maths.round5up(-0.5d), 0.0);
    assertEquals(Double.NaN, Maths.round5up(Double.NaN), 0.0);
  }

  /**
   * Method under test: {@link Maths#round6(double)}
   */
  @Test
  public void testRound6() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round6(10.0d), 0.0);
    assertEquals(4.503599627370496E9d, Maths.round6(4.503599627370496E9d), 0.0);
    assertEquals(-0.5d, Maths.round6(-0.5d), 0.0);
  }

  /**
   * Method under test: {@link Maths#round6up(double)}
   */
  @Test
  public void testRound6up() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round6up(10.0d), 0.0);
    assertEquals(4.503599627370497E9d, Maths.round6up(4.503599627370496E9d), 0.0);
    assertEquals(-0.5d, Maths.round6up(-0.5d), 0.0);
    assertEquals(Double.NaN, Maths.round6up(Double.NaN), 0.0);
  }

  /**
   * Method under test: {@link Maths#round7(double)}
   */
  @Test
  public void testRound7() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round7(10.0d), 0.0);
    assertEquals(4.503599627370496E8d, Maths.round7(4.503599627370496E8d), 0.0);
    assertEquals(-0.5d, Maths.round7(-0.5d), 0.0);
  }

  /**
   * Method under test: {@link Maths#round7up(double)}
   */
  @Test
  public void testRound7up() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round7up(10.0d), 0.0);
    assertEquals(4.503599627370497E8d, Maths.round7up(4.503599627370496E8d), 0.0);
    assertEquals(-0.5d, Maths.round7up(-0.5d), 0.0);
    assertEquals(Double.NaN, Maths.round7up(Double.NaN), 0.0);
  }

  /**
   * Method under test: {@link Maths#round8(double)}
   */
  @Test
  public void testRound8() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round8(10.0d), 0.0);
    assertEquals(1.0E8d, Maths.round8(1.0E8d), 0.0);
    assertEquals(-0.5d, Maths.round8(-0.5d), 0.0);
  }

  /**
   * Method under test: {@link Maths#round8up(double)}
   */
  @Test
  public void testRound8up() {
    // Arrange, Act and Assert
    assertEquals(10.0d, Maths.round8up(10.0d), 0.0);
    assertEquals(1.0E8d, Maths.round8up(1.0E8d), 0.0);
    assertEquals(4.503599627370497E7d, Maths.round8up(4.503599627370496E7d), 0.0);
    assertEquals(-0.5d, Maths.round8up(-0.5d), 0.0);
  }

  /**
   * Method under test: {@link Maths#nextPower2(long, long)}
   */
  @Test
  public void testNextPower2() throws IllegalArgumentException {
    // Arrange, Act and Assert
    assertEquals(1L, Maths.nextPower2(1L, 1L));
    assertEquals(8L, Maths.nextPower2(5L, 1L));
    assertEquals(1L, Maths.nextPower2(0L, 1L));
    assertEquals(1L, Maths.nextPower2(-1L, 1L));
    assertEquals(4611686018427387904L, Maths.nextPower2(Long.MAX_VALUE, 1L));
    assertThrows(IllegalArgumentException.class, () -> Maths.nextPower2(1L, 5L));
  }

  /**
   * Method under test: {@link Maths#isPowerOf2(long)}
   */
  @Test
  public void testIsPowerOf2() {
    // Arrange, Act and Assert
    assertTrue(Maths.isPowerOf2(1L));
    assertFalse(Maths.isPowerOf2(0L));
  }

  /**
   * Method under test: {@link Maths#hash64(long)}
   */
  @Test
  public void testHash64() {
    // Arrange, Act and Assert
    assertEquals(-903433109975627889L, Maths.hash64(81985529216486895L));
    assertEquals(6097020316241462618L, Maths.hash64(81985529216486895L, 81985529216486895L));
    assertEquals(-5059551448262358354L, Maths.hash64(32L, 81985529216486895L));
    assertEquals(1064225413442876498L, Maths.hash64(1539836845L, 81985529216486895L));
    assertEquals(-1898637338959397176L, Maths.hash64(-361396777L, 81985529216486895L));
  }

  /**
   * Method under test: {@link Maths#intLog2(long)}
   */
  @Test
  public void testIntLog2() throws IllegalArgumentException {
    // Arrange, Act and Assert
    assertEquals(0, Maths.intLog2(1L));
    assertThrows(IllegalArgumentException.class, () -> Maths.intLog2(0L));
  }

  /**
   * Method under test: {@link Maths#toInt8(long)}
   */
  @Test
  public void testToInt8() throws ArithmeticException {
    // Arrange, Act and Assert
    assertEquals('*', Maths.toInt8(42L));
    assertThrows(ArithmeticException.class, () -> Maths.toInt8(Long.MAX_VALUE));
  }

  /**
   * Method under test: {@link Maths#toInt16(long)}
   */
  @Test
  public void testToInt16() throws ArithmeticException {
    // Arrange, Act and Assert
    assertEquals((short) 42, Maths.toInt16(42L));
    assertThrows(ArithmeticException.class, () -> Maths.toInt16(Long.MAX_VALUE));
  }

  /**
   * Method under test: {@link Maths#toUInt8(long)}
   */
  @Test
  public void testToUInt8() throws ArithmeticException {
    // Arrange, Act and Assert
    assertEquals((short) 42, Maths.toUInt8(42L));
    assertThrows(ArithmeticException.class, () -> Maths.toUInt8(-1L));
  }

  /**
   * Method under test: {@link Maths#toUInt32(long)}
   */
  @Test
  public void testToUInt32() throws ArithmeticException {
    // Arrange, Act and Assert
    assertEquals(42L, Maths.toUInt32(42L));
    assertThrows(ArithmeticException.class, () -> Maths.toUInt32(-1L));
  }

  /**
   * Method under test: {@link Maths#agitate(long)}
   */
  @Test
  public void testAgitate() {
    // Arrange, Act and Assert
    assertEquals(140737488355329L, Maths.agitate(1L));
  }

  /**
   * Method under test: {@link Maths#divideRoundUp(long, long)}
   */
  @Test
  public void testDivideRoundUp() {
    // Arrange, Act and Assert
    assertEquals(1L, Maths.divideRoundUp(1L, 1L));
    assertEquals(2L, Maths.divideRoundUp(2L, 1L));
    assertEquals(3L, Maths.divideRoundUp(3L, 1L));
    assertEquals(4L, Maths.divideRoundUp(4L, 1L));
    assertEquals(0L, Maths.divideRoundUp(0L, 1L));
    assertThrows(ArithmeticException.class, () -> Maths.divideRoundUp(1L, 0L));
  }

  /**
   * Method under test: {@link Maths#digits(long)}
   */
  @Test
  public void testDigits() {
    // Arrange, Act and Assert
    assertEquals(1, Maths.digits(1L));
    assertEquals(1, Maths.digits(-1L));
    assertEquals(1, Maths.digits(2L));
  }

  /**
   * Method under test: {@link Maths#same(double, double)}
   */
  @Test
  public void testSame() {
    // Arrange, Act and Assert
    assertTrue(Maths.same(10.0d, 10.0d));
    assertFalse(Maths.same(0.5d, 10.0d));
    assertFalse(Maths.same(Double.NaN, 10.0d));
    assertTrue(Maths.same(10.0f, 10.0f));
    assertFalse(Maths.same(0.5f, 10.0f));
    assertFalse(Maths.same(Float.NaN, 10.0f));
  }

  /**
  * Method under test: {@link Maths#add(long, long, long)}
  */
  @Test
  public void testAdd() {
    // Arrange, Act and Assert
    assertEquals(2.0d, Maths.add(1L, 1L, 1L), 0.0);
    assertEquals(6.0d, Maths.add(5L, 1L, 1L), 0.0);
    assertEquals(1.0d, Maths.add(0L, 1L, 1L), 0.0);
    assertEquals(0.0d, Maths.add(-1L, 1L, 1L), 0.0);
  }
}

