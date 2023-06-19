package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

public class PrintExceptionHandlerGptTest extends CoreTestCommon {

    @Test
    public void testPrintExceptionHandler() {
        Logger logger = LoggerFactory.getLogger(NullExceptionHandlerTest.class);
        String testMessage = "Test message";
        Exception testException = new RuntimeException("Test exception");
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        PrintExceptionHandler.OUT.on(logger, testMessage, testException);

        String output = outContent.toString();

        assertTrue(output.contains(logger.getName()));
        assertTrue(output.contains(testMessage));
        assertTrue(output.contains(testException.toString()));
    }
}
