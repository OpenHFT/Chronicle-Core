package net.openhft.chronicle.core;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import java.util.function.Function;
import org.junit.Test;

public class ClassLocalDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link ClassLocal#withInitial(Function)}
  */
  @Test
  public void testWithInitial() {
    // Arrange, Act and Assert
    assertNull(ClassLocal.<Object>withInitial(mock(Function.class)).computeValue(null));
  }
}

