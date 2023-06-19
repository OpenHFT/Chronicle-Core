package net.openhft.chronicle.core.onoes;

import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.LinkedList;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.helpers.SubstituteLogger;

public class RecordingExceptionHandlerDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link RecordingExceptionHandler#on(Logger, String, Throwable)}
  */
  @Test
  public void testOn() {
    // Arrange
    RecordingExceptionHandler recordingExceptionHandler = new RecordingExceptionHandler(LogLevel.ERROR, new HashMap<>(),
        true);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    // Act
    recordingExceptionHandler.on(logger, "An error occurred", null);

    // Assert that nothing has changed
    assertTrue(logger.isDelegateNull());
  }
}

