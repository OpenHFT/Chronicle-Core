package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.function.Supplier;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class OnDemandEventLoopDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link OnDemandEventLoop#OnDemandEventLoop(Supplier)}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    OnDemandEventLoop actualOnDemandEventLoop = new OnDemandEventLoop(mock(Supplier.class));

    // Assert
    assertFalse(actualOnDemandEventLoop.hasEventLoop());
    assertFalse(actualOnDemandEventLoop.isStopped());
    assertFalse(actualOnDemandEventLoop.isAlive());
  }

  /**
   * Method under test: {@link OnDemandEventLoop#eventLoop()}
   */
  @Test
  public void testEventLoop() {
    // Arrange
    Supplier<EventLoop> eventLoopSupplier = mock(Supplier.class);
    DelegatingEventLoop delegatingEventLoop = new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class)));
    when(eventLoopSupplier.get()).thenReturn(delegatingEventLoop);
    OnDemandEventLoop onDemandEventLoop = new OnDemandEventLoop(eventLoopSupplier);

    // Act and Assert
    assertSame(delegatingEventLoop, onDemandEventLoop.eventLoop());
    verify(eventLoopSupplier).get();
    assertTrue(onDemandEventLoop.hasEventLoop());
  }

  /**
   * Method under test: {@link OnDemandEventLoop#hasEventLoop()}
   */
  @Test
  public void testHasEventLoop() {
    // Arrange, Act and Assert
    assertFalse((new OnDemandEventLoop(mock(Supplier.class))).hasEventLoop());
  }

  /**
   * Method under test: {@link OnDemandEventLoop#unpause()}
   */
  @Test
  public void testUnpause() {
    // Arrange
    OnDemandEventLoop onDemandEventLoop = new OnDemandEventLoop(mock(Supplier.class));

    // Act
    onDemandEventLoop.unpause();

    // Assert that nothing has changed
    assertFalse(onDemandEventLoop.hasEventLoop());
  }

  /**
   * Method under test: {@link OnDemandEventLoop#stop()}
   */
  @Test
  public void testStop() {
    // Arrange
    OnDemandEventLoop onDemandEventLoop = new OnDemandEventLoop(mock(Supplier.class));

    // Act
    onDemandEventLoop.stop();

    // Assert that nothing has changed
    assertFalse(onDemandEventLoop.hasEventLoop());
  }

  /**
   * Method under test: {@link OnDemandEventLoop#isAlive()}
   */
  @Test
  public void testIsAlive() {
    // Arrange, Act and Assert
    assertFalse((new OnDemandEventLoop(mock(Supplier.class))).isAlive());
  }

  /**
   * Method under test: {@link OnDemandEventLoop#isStopped()}
   */
  @Test
  public void testIsStopped() {
    // Arrange, Act and Assert
    assertFalse((new OnDemandEventLoop(mock(Supplier.class))).isStopped());
  }

  /**
   * Method under test: {@link OnDemandEventLoop#awaitTermination()}
   */
  @Test
  public void testAwaitTermination() {
    // Arrange
    OnDemandEventLoop onDemandEventLoop = new OnDemandEventLoop(mock(Supplier.class));

    // Act
    onDemandEventLoop.awaitTermination();

    // Assert that nothing has changed
    assertFalse(onDemandEventLoop.hasEventLoop());
  }

  /**
   * Method under test: {@link OnDemandEventLoop#close()}
   */
  @Test
  public void testClose() {
    // Arrange
    OnDemandEventLoop onDemandEventLoop = new OnDemandEventLoop(mock(Supplier.class));

    // Act
    onDemandEventLoop.close();

    // Assert that nothing has changed
    assertFalse(onDemandEventLoop.hasEventLoop());
  }
}

