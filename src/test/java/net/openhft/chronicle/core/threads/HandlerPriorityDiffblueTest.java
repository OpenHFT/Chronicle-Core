package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertEquals;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class HandlerPriorityDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link HandlerPriority#alias()}
  */
  @Test
  public void testAlias() {
    // Arrange, Act and Assert
    assertEquals(HandlerPriority.HIGH, HandlerPriority.HIGH.alias());
    assertEquals(HandlerPriority.MEDIUM, HandlerPriority.REPLICATION.alias());
    assertEquals(HandlerPriority.TIMER, HandlerPriority.REPLICATION_TIMER.alias());
    assertEquals(HandlerPriority.MEDIUM, HandlerPriority.CONCURRENT.alias());
  }
}

