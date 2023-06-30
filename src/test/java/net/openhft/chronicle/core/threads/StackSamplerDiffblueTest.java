package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import java.awt.geom.AffineTransform;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImageOp;
import java.awt.image.renderable.RenderableImageProducer;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class StackSamplerDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: default or parameterless constructor of {@link StackSampler}
  */
  @Test
  public void testConstructor() {
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
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, new ParameterBlock());

    stackSampler.thread(new CleaningThread(
        new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())), "Name"));

    // Act
    stackSampler.stop();

    // Assert
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#stop()}
   */
  @Test
  public void testStop4() {
    // Arrange
    ParameterBlock parameterBlock = new ParameterBlock();
    parameterBlock.add(1L);
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, parameterBlock);

    CleaningThread thread = new CleaningThread(
        new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())), "Name");

    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(thread);

    // Act
    stackSampler.stop();

    // Assert
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#stop()}
   */
  @Test
  public void testStop5() {
    // Arrange
    ParameterBlock parameterBlock = new ParameterBlock();
    parameterBlock.add(10.0f);
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, parameterBlock);

    CleaningThread thread = new CleaningThread(
        new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())), "Name");

    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(thread);

    // Act
    stackSampler.stop();

    // Assert
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#stop()}
   */
  @Test
  public void testStop6() {
    // Arrange
    ParameterBlock parameterBlock = new ParameterBlock();
    parameterBlock.addSource("");
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, parameterBlock);

    CleaningThread thread = new CleaningThread(
        new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())), "Name");

    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(thread);

    // Act
    stackSampler.stop();

    // Assert that nothing has changed
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#stop()}
   */
  @Test
  public void testStop7() {
    // Arrange
    ParameterBlock parameterBlock = new ParameterBlock();
    parameterBlock.add("foo");
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, parameterBlock);

    CleaningThread thread = new CleaningThread(
        new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())), "Name");

    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(thread);

    // Act
    stackSampler.stop();

    // Assert that nothing has changed
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#stop()}
   */
  @Test
  public void testStop8() {
    // Arrange
    ParameterBlock parameterBlock = new ParameterBlock();
    parameterBlock.add("");
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, parameterBlock);

    CleaningThread thread = new CleaningThread(
        new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())), "Name");

    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(thread);

    // Act
    stackSampler.stop();

    // Assert that nothing has changed
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#stop()}
   */
  @Test
  public void testStop9() {
    // Arrange
    ParameterBlock parameterBlock = new ParameterBlock();
    parameterBlock.addSource(42);
    parameterBlock.add(10.0f);
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, parameterBlock);

    CleaningThread thread = new CleaningThread(
        new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())), "Name");

    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(thread);

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

  /**
   * Method under test: {@link StackSampler#getAndReset()}
   */
  @Test
  public void testGetAndReset3() {
    // Arrange
    StackSampler stackSampler = new StackSampler();
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, new ParameterBlock());

    stackSampler.thread(new CleaningThread(
        new CleaningThread(new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())),
            "Name", true),
        "Name"));

    // Act and Assert
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#getAndReset()}
   */
  @Test
  public void testGetAndReset4() {
    // Arrange
    ParameterBlock parameterBlock = new ParameterBlock();
    parameterBlock.add(Byte.MAX_VALUE);
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, parameterBlock);

    CleaningThread thread = new CleaningThread(
        new CleaningThread(new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())),
            "Name", true),
        "Name");

    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(thread);

    // Act and Assert
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#getAndReset()}
   */
  @Test
  public void testGetAndReset5() {
    // Arrange
    ParameterBlock parameterBlock = new ParameterBlock();
    parameterBlock.add(Byte.MIN_VALUE);
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, parameterBlock);

    CleaningThread thread = new CleaningThread(
        new CleaningThread(new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())),
            "Name", true),
        "Name");

    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(thread);

    // Act and Assert
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#getAndReset()}
   */
  @Test
  public void testGetAndReset6() {
    // Arrange
    ParameterBlock parameterBlock = new ParameterBlock();
    parameterBlock.add(Long.MAX_VALUE);
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, parameterBlock);

    CleaningThread thread = new CleaningThread(
        new CleaningThread(new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())),
            "Name", true),
        "Name");

    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(thread);

    // Act and Assert
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#getAndReset()}
   */
  @Test
  public void testGetAndReset7() {
    // Arrange
    ParameterBlock parameterBlock = new ParameterBlock();
    parameterBlock.add(0.5f);
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, parameterBlock);

    CleaningThread thread = new CleaningThread(
        new CleaningThread(new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())),
            "Name", true),
        "Name");

    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(thread);

    // Act and Assert
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#getAndReset()}
   */
  @Test
  public void testGetAndReset8() {
    // Arrange
    ParameterBlock parameterBlock = new ParameterBlock();
    parameterBlock.add(Float.NaN);
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, parameterBlock);

    CleaningThread thread = new CleaningThread(
        new CleaningThread(new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())),
            "Name", true),
        "Name");

    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(thread);

    // Act and Assert
    assertNull(stackSampler.getAndReset());
  }

  /**
   * Method under test: {@link StackSampler#getAndReset()}
   */
  @Test
  public void testGetAndReset9() {
    // Arrange
    ParameterBlock parameterBlock = new ParameterBlock();
    parameterBlock.add(0.5d);
    RenderableImageOp renderableImageOp = new RenderableImageOp(null, parameterBlock);

    CleaningThread thread = new CleaningThread(
        new CleaningThread(new RenderableImageProducer(renderableImageOp, new RenderContext(new AffineTransform())),
            "Name", true),
        "Name");

    StackSampler stackSampler = new StackSampler();
    stackSampler.thread(thread);

    // Act and Assert
    assertNull(stackSampler.getAndReset());
  }
}

