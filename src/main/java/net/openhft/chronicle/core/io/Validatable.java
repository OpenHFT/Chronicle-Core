/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

/**
 * Implement to provide a validation hook for object state checks before serialisation.
 * <p>
 * Chronicle libraries call {@link ValidatableUtil#validate(Object)} before
 * serialising method-writer arguments and it is common to invoke it from
 * {@code toString()} whilst debugging. Validation can be temporarily disabled
 * by wrapping the call in {@link ValidatableUtil#startValidateDisabled()} and
 * {@link ValidatableUtil#endValidateDisabled()}.
 */
public interface Validatable {

    /**
     * Validates the state of the object before it is marshalled.
     *
     * <p>This method should be called prior to writing the object via a method writer.
     * Implementations should check the state of the object and throw an
     * {@link InvalidMarshallableException} if the object is in an invalid state.
     * <p>
     * For example, this could involve checking for null values in required fields,
     * validating that numerical values are within acceptable ranges, etc.
     *
     * @throws InvalidMarshallableException if the object is in an invalid state,
     *                                      such as having null values in required fields or values
     *                                      out of acceptable range.
     * @throws RuntimeException             if an unexpected error occurs during validation.
     */
    void validate() throws InvalidMarshallableException;
}
