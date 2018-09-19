package net.openhft.chronicle.core.jlbh;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static net.openhft.chronicle.core.jlbh.JLBHDeterministicFixtures.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class JLBHIntegrationTest {

    private PrintStream originalSystemOut;
    private PrintStream originalSystemErr;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    @Before
    public void setUp() {
        rememberOriginalStdErrOut();
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
    }

    @After
    public void tearDown() {
        resetSystemOut();
    }

    @Test
    public void shouldMeasureLatency() throws Exception {
        // given
        redirectSystemOut();
        final JLBH jlbh = new JLBH(options());

        // when
        jlbh.start();

        // then
        String stdOut = outContent.toString();
        resetSystemOut();
        assertThat(stdOut, containsString("OS Jitter"));
        assertThat(stdOut, containsString("Warm up complete (500 iterations took "));
        assertThat(stdOut, containsString("Run time: "));
        String actual = withoutNonDeterministicFields(stdOut);
        String expected = withoutNonDeterministicFields(predictableTaskExpectedResult());
        //Assert.assertEquals(expected,actual);
        assertThat(actual, equalTo(expected));
    }

    private void redirectSystemOut() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    private void resetSystemOut() {
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);
    }

    private void rememberOriginalStdErrOut() {
        originalSystemOut = System.out;
        originalSystemErr = System.err;
    }
}