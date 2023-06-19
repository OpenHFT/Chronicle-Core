package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertTrue;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class ReferenceOwnerDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link ReferenceOwner#temporary(String)}
  */
  @Test
  public void testTemporary() {
    // Arrange, Act and Assert
    assertTrue(ReferenceOwner.temporary("Name") instanceof VanillaReferenceOwner);
  }
}

