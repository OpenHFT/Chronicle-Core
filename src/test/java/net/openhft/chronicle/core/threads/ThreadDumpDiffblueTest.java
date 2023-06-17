package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import org.junit.Test;

public class ThreadDumpDiffblueTest {
  /**
  * Method under test: {@link ThreadDump#createdHereFor(Thread)}
  */
  @Test
  public void testCreatedHereFor() {
    // Arrange, Act and Assert
    assertNull(ThreadDump.createdHereFor(new Thread()));
    assertNull(ThreadDump.createdHereFor(new Thread(mock(Runnable.class), "foo")));
    assertNull(ThreadDump.createdHereFor(new Thread((Runnable) null, "")));
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

