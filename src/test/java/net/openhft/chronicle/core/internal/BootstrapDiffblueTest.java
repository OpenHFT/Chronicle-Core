package net.openhft.chronicle.core.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class BootstrapDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link Bootstrap#isArm0()}
   */
  @Test
  public void testIsArm0() {
    // Arrange, Act and Assert
    assertFalse(Bootstrap.isArm0());
  }

  /**
   * Method under test: {@link Bootstrap#isMacArm0()}
   */
  @Test
  public void testIsMacArm0() {
    // Arrange, Act and Assert
    assertFalse(Bootstrap.isMacArm0());
  }

  /**
   * Method under test: {@link Bootstrap#isAzulZing0()}
   */
  @Test
  public void testIsAzulZing0() {
    // Arrange, Act and Assert
    assertFalse(Bootstrap.isAzulZing0());
  }

  /**
   * Method under test: {@link Bootstrap#isAzulZulu0()}
   */
  @Test
  public void testIsAzulZulu0() {
    // Arrange, Act and Assert
    assertTrue(Bootstrap.isAzulZulu0());
  }

  /**
  * Method under test: {@link Bootstrap#is64bit0()}
  */
  @Test
  public void testIs64bit0() {
    // Arrange, Act and Assert
    assertTrue(Bootstrap.is64bit0());
  }
}

