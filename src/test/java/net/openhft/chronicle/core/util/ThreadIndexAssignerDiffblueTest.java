package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import net.openhft.chronicle.core.values.IntArrayValues;
import org.junit.Test;

public class ThreadIndexAssignerDiffblueTest {
  /**
  * Method under test: {@link ThreadIndexAssigner#getId()}
  */
  @Test
  public void testGetId() throws IllegalStateException, BufferOverflowException, BufferUnderflowException {
    // Arrange
    IntArrayValues values = mock(IntArrayValues.class);
    when(values.getCapacity()).thenThrow(new BufferUnderflowException());
    doThrow(new BufferUnderflowException()).when(values).setMaxUsed(anyLong());

    // Act and Assert
    assertThrows(BufferUnderflowException.class, () -> (new ThreadIndexAssigner(values)).getId());
    verify(values).getCapacity();
  }
}

