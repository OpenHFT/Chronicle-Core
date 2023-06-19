package net.openhft.chronicle.core.shutdown;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;
import org.mockito.Mockito;

public class PriorityHookDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link PriorityHook#add(int, Runnable)}
  */
  @Test
  public void testAdd() {
    // Arrange, Act and Assert
    assertFalse(PriorityHook.add(2, mock(Runnable.class)));
    assertFalse(PriorityHook.add(7, mock(Runnable.class)));
    assertFalse(PriorityHook.add(0, mock(Runnable.class)));
    assertFalse(PriorityHook.add(1, new Thread("foo")));
  }

  /**
   * Method under test: {@link PriorityHook#addAndGet(Hooklet)}
   */
  @Test
  public void testAddAndGet() {
    // Arrange
    Hooklet hooklet = mock(Hooklet.class);
    when(hooklet.compareTo(Mockito.<Hooklet>any())).thenReturn(0);

    // Act
    PriorityHook.addAndGet(hooklet);

    // Assert
    verify(hooklet).compareTo(Mockito.<Hooklet>any());
  }
}

