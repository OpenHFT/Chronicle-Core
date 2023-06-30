package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import java.util.function.Supplier;
import org.junit.Test;

public class EventHandlerDiffblueTest {
  /**
  * Method under test: {@link EventHandler#eventLoop(EventLoop)}
  */
  @Test
  public void testEventLoop() {
    // Arrange
    MockEventHandler mockEventHandler = new MockEventHandler();
    DelegatingEventLoop eventLoop = new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class)));

    // Act
    mockEventHandler.eventLoop(eventLoop);

    // Assert
    assertSame(eventLoop, mockEventHandler.getEventLoop());
  }

  /**
   * Method under test: {@link EventHandler#loopStarted()}
   */
  @Test
  public void testLoopStarted() {
    // Arrange
    MockEventHandler mockEventHandler = new MockEventHandler();

    // Act
    mockEventHandler.loopStarted();

    // Assert
    assertTrue(mockEventHandler.isLoopStartedCalled());
  }

  /**
   * Method under test: {@link EventHandler#loopStarted()}
   */
  @Test
  public void testLoopStarted2() {
    // Arrange
    MockEventHandler mockEventHandler = new MockEventHandler();
    mockEventHandler.eventLoop(new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class))));

    // Act
    mockEventHandler.loopStarted();

    // Assert
    assertTrue(mockEventHandler.isLoopStartedCalled());
  }

  /**
   * Method under test: {@link EventHandler#loopFinished()}
   */
  @Test
  public void testLoopFinished() {
    // Arrange
    MockEventHandler mockEventHandler = new MockEventHandler();

    // Act
    mockEventHandler.loopFinished();

    // Assert
    assertTrue(mockEventHandler.isLoopFinishedCalled());
  }

  /**
   * Method under test: {@link EventHandler#loopFinished()}
   */
  @Test
  public void testLoopFinished2() {
    // Arrange
    MockEventHandler mockEventHandler = new MockEventHandler();
    mockEventHandler.eventLoop(new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class))));

    // Act
    mockEventHandler.loopFinished();

    // Assert
    assertTrue(mockEventHandler.isLoopFinishedCalled());
  }

  /**
   * Method under test: {@link EventHandler#priority()}
   */
  @Test
  public void testPriority() {
    // Arrange, Act and Assert
    assertEquals(HandlerPriority.MEDIUM, (new MockEventHandler()).priority());
  }

  /**
   * Method under test: {@link EventHandler#priority()}
   */
  @Test
  public void testPriority2() {
    // Arrange
    MockEventHandler mockEventHandler = new MockEventHandler();
    mockEventHandler.eventLoop(new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class))));

    // Act and Assert
    assertEquals(HandlerPriority.MEDIUM, mockEventHandler.priority());
  }
}

