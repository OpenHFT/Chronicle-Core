package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class URIEncoderDiffblueTest {
  /**
  * Method under test: {@link URIEncoder#encodeURI(String)}
  */
  @Test
  public void testEncodeURI() {
    // Arrange, Act and Assert
    assertEquals("Arg%20String", URIEncoder.encodeURI("Arg String"));
    assertEquals("-_.!~*'()\"", URIEncoder.encodeURI("-_.!~*'()\""));
    assertEquals("42", URIEncoder.encodeURI("42"));
  }
}

