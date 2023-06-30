package net.openhft.chronicle.core.internal.util;

import static org.junit.Assert.assertSame;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;

public class DirectBufferUtilDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link DirectBufferUtil#directBufferClass()}
  */
  @Test
  public void testDirectBufferClass() {
    // Arrange and Act
    Class<?> actualDirectBufferClassResult = DirectBufferUtil.directBufferClass();

    // Assert
    assertSame(DirectBuffer.class, actualDirectBufferClassResult);
  }
}

