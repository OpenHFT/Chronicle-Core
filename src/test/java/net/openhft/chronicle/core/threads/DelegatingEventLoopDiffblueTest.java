package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import java.util.function.Supplier;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class DelegatingEventLoopDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link DelegatingEventLoop#DelegatingEventLoop(EventLoop)}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    DelegatingEventLoop actualDelegatingEventLoop = new DelegatingEventLoop(
        new OnDemandEventLoop(mock(Supplier.class)));

    // Assert
    assertFalse(actualDelegatingEventLoop.isAlive());
    assertFalse(actualDelegatingEventLoop.isStopped());
    assertTrue(actualDelegatingEventLoop.isClosing());
    assertTrue(actualDelegatingEventLoop.isClosed());
  }

  /**
   * Method under test: {@link DelegatingEventLoop#isClosed()}
   */
  @Test
  public void testIsClosed() {
    // Arrange, Act and Assert
    assertTrue((new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class)))).isClosed());
    assertTrue(
        (new DelegatingEventLoop(new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class))))).isClosed());
  }

  /**
   * Method under test: {@link DelegatingEventLoop#isStopped()}
   */
  @Test
  public void testIsStopped() {
    // Arrange, Act and Assert
    assertFalse((new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class)))).isStopped());
    assertFalse(
        (new DelegatingEventLoop(new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class))))).isStopped());
  }

  /**
   * Method under test: {@link DelegatingEventLoop#isClosing()}
   */
  @Test
  public void testIsClosing() {
    // Arrange, Act and Assert
    assertTrue((new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class)))).isClosing());
    assertTrue(
        (new DelegatingEventLoop(new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class))))).isClosing());
  }

  /**
   * Method under test: {@link DelegatingEventLoop#isAlive()}
   */
  @Test
  public void testIsAlive() {
    // Arrange, Act and Assert
    assertFalse((new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class)))).isAlive());
    assertFalse(
        (new DelegatingEventLoop(new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class))))).isAlive());
  }

  /**
   * Method under test: {@link DelegatingEventLoop#runsInsideCoreLoop()}
   */
  @Test
  public void testRunsInsideCoreLoop() {
    // Arrange, Act and Assert
    assertTrue((new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class)))).runsInsideCoreLoop());
    assertTrue((new DelegatingEventLoop(new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class)))))
        .runsInsideCoreLoop());
  }
}

