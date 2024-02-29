package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ValidatableUtilTest {

    @Test
    public void testValidateToggle() {
        assertTrue(ValidatableUtil.validateEnabled());

        ValidatableUtil.startValidateDisabled();
        assertFalse(ValidatableUtil.validateEnabled());

        ValidatableUtil.endValidateDisabled();
        assertTrue(ValidatableUtil.validateEnabled());
    }

    @Test
    public void testEndValidateDisabledWithoutStart() {
        AssertionError exception = assertThrows(AssertionError.class, ValidatableUtil::endValidateDisabled);
        assertNotNull(exception);
    }

    @Test
    public void testValidate() throws InvalidMarshallableException {
        Validatable validatable = mock(Validatable.class);
        ValidatableUtil.validate(validatable);

        verify(validatable, times(1)).validate();
    }
}
