package net.openhft.chronicle.core;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import java.util.function.Function;
import org.junit.Test;

/**
 * This class contains tests for the ClassLocal class.
 * It tests the functionality provided by the ClassLocal class methods.
 */
public class ClassLocalDiffblueTest extends CoreTestCommon {

  /**
   * This test case verifies the behavior of the {@link ClassLocal#withInitial(Function)} method.
   * It checks that the method correctly initializes a ClassLocal instance with a provided function
   * and that the computeValue method of the created ClassLocal instance returns null when called with a null argument.
   */
  @Test
  public void testWithInitial() {
    // Arrange: Create a mock function to be used as the initial function for the ClassLocal instance
    Function mockFunction = mock(Function.class);

    // Act: Create a ClassLocal instance with the mock function and call computeValue with a null argument
    Object result = ClassLocal.withInitial(mockFunction)
            .get(Class.class);

    // Assert: Verify that the result of the computeValue method is null
    assertNull(result);
  }
}


