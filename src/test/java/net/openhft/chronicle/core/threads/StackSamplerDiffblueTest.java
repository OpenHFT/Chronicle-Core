package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class StackSamplerDiffblueTest extends CoreTestCommon {
  /**
  * Methods under test: 
  * 
  * <ul>
  *   <li>default or parameterless constructor of {@link StackSampler}
  *   <li>{@link StackSampler#thread(Thread)}
  * </ul>
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    StackSampler actualStackSampler = new StackSampler();
    actualStackSampler.thread(new Thread());

    // Assert
    assertNull(actualStackSampler.getAndReset());
  }

  /**
   * Method under test: default or parameterless constructor of {@link StackSampler}
   */
  @Test
  public void testConstructor2() {
    // Arrange, Act and Assert
    assertNull((new StackSampler()).getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#stop()}
   */
  @Test
  public void testStop() {
    // Arrange
    StackSampler stackSampler = new StackSampler();

    // Act
    stackSampler.stop();

    // Assert that nothing has changed
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#stop()}
   */
  @Test
  public void testStop2() {
    // Arrange
    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(new CleaningThread(mock(Runnable.class), "Name"));

    // Act
    stackSampler.stop();

    // Assert that nothing has changed
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#stop()}
   */
  @Test
  public void testStop3() {
    // Arrange
    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(new CleaningThread(new FutureTask<>(mock(Callable.class)), "Name"));

    // Act
    stackSampler.stop();

    // Assert that nothing has changed
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#stop()}
   */
  @Test
  public void testStop4() {
    // Arrange
    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(new CleaningThread(mock(Runnable.class), "net.openhft.chronicle.core.threads.CleaningThread"));

    // Act
    stackSampler.stop();

    // Assert that nothing has changed
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#stop()}
   */
  @Test
  public void testStop5() {
    // Arrange
    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(new CleaningThread(mock(Runnable.class), "value"));

    // Act
    stackSampler.stop();

    // Assert that nothing has changed
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#getAndReset()}
   */
  @Test
  public void testGetAndReset() {
    // Arrange, Act and Assert
    assertNull((new StackSampler()).getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#getAndReset()}
   */
  @Test
  public void testGetAndReset2() {
    // Arrange
    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(new CleaningThread(mock(Runnable.class), "Name"));

    // Act and Assert
    assertNull(stackSampler.getAndReset());
  }
}

