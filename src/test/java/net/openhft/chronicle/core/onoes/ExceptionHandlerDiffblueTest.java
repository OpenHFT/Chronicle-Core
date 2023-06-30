package net.openhft.chronicle.core.onoes;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.HashMap;
import java.util.LinkedList;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.helpers.SubstituteLogger;

public class ExceptionHandlerDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link ExceptionHandler#ignoresEverything()}
  */
  @Test
  public void testIgnoresEverything() {
    // Arrange and Act
    ExceptionHandler actualIgnoresEverythingResult = ExceptionHandler.ignoresEverything();
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    actualIgnoresEverythingResult.on(logger, "An error occurred", new Throwable());

    // Assert that nothing has changed
    assertTrue(logger.isDelegateNull());
  }
 }

