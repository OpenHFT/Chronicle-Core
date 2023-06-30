package net.openhft.chronicle.core.time;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Test;

public class UniqueMicroTimeProviderDiffblueTest {
  /**
  * Method under test: {@link UniqueMicroTimeProvider#provider(TimeProvider)}
  */
  @Test
  public void testProvider() throws IllegalStateException {
    // Arrange
    UniqueMicroTimeProvider uniqueMicroTimeProvider = new UniqueMicroTimeProvider();
    TimeProvider provider = mock(TimeProvider.class);
    when(provider.currentTimeMicros()).thenThrow(new IllegalStateException("foo"));

    // Act and Assert
    assertThrows(IllegalStateException.class, () -> uniqueMicroTimeProvider.provider(provider));
    verify(provider).currentTimeMicros();
  }
}

