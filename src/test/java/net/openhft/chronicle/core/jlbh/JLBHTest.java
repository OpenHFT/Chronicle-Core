package net.openhft.chronicle.core.jlbh;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import static net.openhft.chronicle.core.jlbh.JLBHDeterministicFixtures.options;
import static net.openhft.chronicle.core.jlbh.JLBHDeterministicFixtures.predictableTaskExpectedResult;
import static net.openhft.chronicle.core.jlbh.JLBHDeterministicFixtures.withoutNonDeterministicFields;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class JLBHTest {

  @Test
  public void shouldWriteResultToTheOutputProvided() throws Exception {
    // given
    final OutputStream outputStream = new ByteArrayOutputStream();
    final JLBH jlbh = new JLBH(options(), new PrintStream(outputStream));

    // when
    jlbh.start();

    // then
    String result = outputStream.toString();
    assertThat(result, containsString("OS Jitter"));
    assertThat(result, containsString("Warm up complete (500 iterations took "));
    assertThat(result, containsString("Run time: "));
    assertThat(withoutNonDeterministicFields(result), equalTo(withoutNonDeterministicFields(predictableTaskExpectedResult())));
  }
}