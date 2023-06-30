package net.openhft.chronicle.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.openhft.chronicle.core.io.DualReferenceCounted;
import net.openhft.chronicle.core.io.MonitorReferenceCounted;
import net.openhft.chronicle.core.io.ReferenceOwner;
import org.junit.Test;

public class CloseableUtilsDiffblueTest {
  /**
   * Method under test: {@link CloseableUtils#waitForCloseablesToClose(long)}
   */
  @Test
  public void testWaitForCloseablesToClose() {
    // Arrange, Act and Assert
    assertTrue(CloseableUtils.waitForCloseablesToClose(1L));
  }

  /**
  * Method under test: {@link CloseableUtils#asString(Object)}
  */
  @Test
  public void testAsString() {
    // Arrange, Act and Assert
    assertEquals("INIT", CloseableUtils.asString(ReferenceOwner.INIT));
  }

  /**
   * Method under test: {@link CloseableUtils#asString(Object)}
   */
  @Test
  public void testAsString2() {
    // Arrange
    MonitorReferenceCounted a = mock(MonitorReferenceCounted.class);
    when(a.refCount()).thenReturn(3);
    when(a.referenceName()).thenReturn("Reference Name");

    // Act and Assert
    assertEquals("Reference Name refCount=3",
        CloseableUtils.asString(new DualReferenceCounted(a, mock(MonitorReferenceCounted.class))));
    verify(a).refCount();
    verify(a).referenceName();
  }
}

