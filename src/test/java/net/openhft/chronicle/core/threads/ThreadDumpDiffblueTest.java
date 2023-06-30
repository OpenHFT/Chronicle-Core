package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class ThreadDumpDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link ThreadDump#createdHereFor(Thread)}
  */
  @Test
  public void testCreatedHereFor() {
    // Arrange, Act and Assert
    assertNull(ThreadDump.createdHereFor(new Thread()));
    assertNull(ThreadDump.createdHereFor(new Thread(mock(Runnable.class), "foo")));
  }

  /**
   * Method under test: {@link ThreadDump#ignore(String)}
   */
  @Test
  public void testIgnore() {
    // Arrange
    ThreadDump threadDump = new ThreadDump();

    // Act
    threadDump.ignore("Thread Name");

    // Assert
    assertEquals(5, threadDump.ignored.size());
  }
}

