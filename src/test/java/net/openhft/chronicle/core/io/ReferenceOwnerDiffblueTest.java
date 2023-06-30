package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ReferenceOwnerDiffblueTest {
  /**
  * Method under test: {@link ReferenceOwner#temporary(String)}
  */
  @Test
  public void testTemporary() {
    // Arrange, Act and Assert
    assertTrue(ReferenceOwner.temporary("Name") instanceof VanillaReferenceOwner);
  }

  /**
   * Method under test: {@link ReferenceOwner#referenceName()}
   */
  @Test
  public void testReferenceName() {
    // Arrange, Act and Assert
    assertEquals("VanillaReferenceOwner{name='Name'}", (new VanillaReferenceOwner("Name")).referenceName());
  }
}

