package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.StackTrace;
import org.junit.Test;

public class ReadResolvableDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link ReadResolvable#readResolve(Object)}
  */
  @Test
  public void testReadResolve() {
    // Arrange, Act and Assert
    assertEquals("42", ReadResolvable.readResolve("42"));
    assertNull(ReadResolvable.readResolve(null));
    assertTrue(ReadResolvable.readResolve(new StackTrace()) instanceof StackTrace);
    assertEquals("readResolve", ReadResolvable.readResolve("readResolve"));
  }
}

