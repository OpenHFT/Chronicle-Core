package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Test;

public class ValidatableUtilDiffblueTest {
  /**
   * Method under test: {@link ValidatableUtil#validateEnabled()}
   */
  @Test
  public void testValidateEnabled() {
    // Arrange, Act and Assert
    assertTrue(ValidatableUtil.validateEnabled());
  }

  /**
   * Method under test: {@link ValidatableUtil#validate(Object)}
   */
  @Test
  public void testValidate() throws InvalidMarshallableException {
    // Arrange, Act and Assert
    assertEquals("42", ValidatableUtil.validate("42"));
  }

  /**
   * Method under test: {@link ValidatableUtil#validate(Object)}
   */
  @Test
  public void testValidate2() throws InvalidMarshallableException {
    // Arrange
    Validatable validatable = mock(Validatable.class);
    doNothing().when(validatable).validate();

    // Act
    ValidatableUtil.validate(validatable);

    // Assert
    verify(validatable).validate();
  }

  /**
   * Method under test: {@link ValidatableUtil#validate(Object)}
   */
  @Test
  public void testValidate3() throws InvalidMarshallableException {
    // Arrange
    Validatable validatable = mock(Validatable.class);
    doThrow(new InvalidMarshallableException("Msg")).when(validatable).validate();

    // Act and Assert
    assertThrows(InvalidMarshallableException.class, () -> ValidatableUtil.validate(validatable));
    verify(validatable).validate();
  }

  /**
  * Method under test: {@link ValidatableUtil#requireNonNull(Object, String)}
  */
  @Test
  public void testRequireNonNull() throws InvalidMarshallableException {
    // Arrange, Act and Assert
    assertThrows(InvalidMarshallableException.class, () -> ValidatableUtil.requireNonNull(null, "foo"));
  }

  /**
   * Method under test: {@link ValidatableUtil#requireTrue(boolean, String)}
   */
  @Test
  public void testRequireTrue() throws InvalidMarshallableException {
    // Arrange, Act and Assert
    assertThrows(InvalidMarshallableException.class, () -> ValidatableUtil.requireTrue(false, "foo"));
  }
}

