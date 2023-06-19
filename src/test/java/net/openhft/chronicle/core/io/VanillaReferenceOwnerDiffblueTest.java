package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class VanillaReferenceOwnerDiffblueTest extends CoreTestCommon {
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
   * Method under test: {@link VanillaReferenceOwner#referenceName()}
   */
  @Test
  public void testReferenceName() {
    // Arrange, Act and Assert
    assertEquals("VanillaReferenceOwner{name='Name'}", (new VanillaReferenceOwner("Name")).referenceName());
  }
}

