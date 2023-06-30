package net.openhft.chronicle.core.internal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.openhft.chronicle.core.util.ThreadConfinementAsserter;
import org.junit.Test;

public class ThreadConfinementLifecycleDiffblueTest {
  /**
   * Method under test: {@link ThreadConfinementLifecycle#create()}
   */
  @Test
  public void testCreate() {
    // Arrange and Act
    ThreadConfinementAsserter actualCreateResult = ThreadConfinementLifecycle.create();
    actualCreateResult.assertThreadConfined();

    // Assert
    assertTrue(actualCreateResult instanceof VanillaThreadConfinementAsserter);
  }

  /**
   * Method under test: {@link ThreadConfinementLifecycle#create(boolean)}
   */
  @Test
  public void testCreate2() {
    // Arrange and Act
    ThreadConfinementAsserter actualCreateResult = ThreadConfinementLifecycle.create(true);
    actualCreateResult.assertThreadConfined();

    // Assert
    assertTrue(actualCreateResult instanceof VanillaThreadConfinementAsserter);
  }

  /**
   * Method under test: {@link ThreadConfinementLifecycle#create(boolean)}
   */
  @Test
  public void testCreate3() {
    // Arrange and Act
    ThreadConfinementAsserter actualCreateResult = ThreadConfinementLifecycle.create(false);
    actualCreateResult.assertThreadConfined();

    // Assert that nothing has changed
    assertEquals(NopThreadConfinementAsserter.INSTANCE, actualCreateResult);
  }

  /**
   * Method under test: {@link ThreadConfinementLifecycle#createEnabled()}
   */
  @Test
  public void testCreateEnabled() {
    // Arrange and Act
    ThreadConfinementAsserter actualCreateEnabledResult = ThreadConfinementLifecycle.createEnabled();
    actualCreateEnabledResult.assertThreadConfined();

    // Assert
    assertTrue(actualCreateEnabledResult instanceof VanillaThreadConfinementAsserter);
  }

  /**
  * Method under test: {@link ThreadConfinementLifecycle#assertionsEnable()}
  */
  @Test
  public void testAssertionsEnable() {
    // Arrange, Act and Assert
    assertTrue(ThreadConfinementLifecycle.assertionsEnable());
  }
}

