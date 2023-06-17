package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertNull;
import org.junit.Test;

public class JitterSamplerDiffblueTest {
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

