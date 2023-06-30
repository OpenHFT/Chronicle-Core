package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertNull;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class JitterSamplerDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link JitterSampler#takeSnapshot()}
  */
  @Test
  public void testTakeSnapshot() {
    // Arrange, Act and Assert
    assertNull(JitterSampler.takeSnapshot());
    assertNull(JitterSampler.takeSnapshot(1L));
  }
}

