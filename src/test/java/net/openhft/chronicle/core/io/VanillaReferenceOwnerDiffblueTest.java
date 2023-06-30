package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import org.junit.Test;

public class VanillaReferenceOwnerDiffblueTest {
  /**
  * Methods under test: 
  * 
  * <ul>
  *   <li>{@link VanillaReferenceOwner#VanillaReferenceOwner(String)}
  *   <li>{@link VanillaReferenceOwner#toString()}
  *   <li>{@link VanillaReferenceOwner#isClosed()}
  * </ul>
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    VanillaReferenceOwner actualVanillaReferenceOwner = new VanillaReferenceOwner("Name");
    String actualToStringResult = actualVanillaReferenceOwner.toString();

    // Assert
    assertFalse(actualVanillaReferenceOwner.isClosed());
    assertEquals("VanillaReferenceOwner{name='Name'}", actualToStringResult);
  }

  /**
   * Method under test: {@link VanillaReferenceOwner#VanillaReferenceOwner(String)}
   */
  @Test
  public void testConstructor2() {
    // Arrange and Act
    VanillaReferenceOwner actualVanillaReferenceOwner = new VanillaReferenceOwner("Name");

    // Assert
    assertFalse(actualVanillaReferenceOwner.isClosed());
    assertFalse(actualVanillaReferenceOwner.isClosing());
  }

  /**
   * Method under test: {@link VanillaReferenceOwner#VanillaReferenceOwner(String)}
   */
  @Test
  public void testConstructor3() {
    // Arrange, Act and Assert
    assertThrows(IllegalArgumentException.class, () -> new VanillaReferenceOwner(null));
  }

  /**
   * Method under test: {@link VanillaReferenceOwner#referenceName()}
   */
  @Test
  public void testReferenceName() {
    // Arrange, Act and Assert
    assertEquals("VanillaReferenceOwner{name='Name'}", (new VanillaReferenceOwner("Name")).referenceName());
  }
}

