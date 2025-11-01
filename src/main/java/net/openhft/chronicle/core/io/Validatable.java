/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

/**
 * Implement to provide a validation hook for the object.
 * <p>
 * Chronicle libraries call {@link ValidatableUtil#validate(Object)} before
 * serialising method-writer arguments and it is common to invoke it from
 * {@code toString()} whilst debugging. Validation can be temporarily disabled
 * by wrapping the call in {@link ValidatableUtil#startValidateDisabled()} and
 * {@link ValidatableUtil#endValidateDisabled()}.
 * <p>
 * Example usage:
 * <pre>
 * public class MyData implements Validatable {
 *     private String name;
 *     private Integer age;
 *
 *     // getters and setters
 *
 *     {@literal @}Override
 *     public void validate() throws InvalidMarshallableException {
 *         if (name == null || name.isEmpty())
 *             throw new InvalidMarshallableException("Name cannot be null or empty");
 *         if (age == null || age &lt; 0)
 *             throw new InvalidMarshallableException("Age cannot be null or negative");
 *     }
 * }
 * </pre>
 */
public interface Validatable {

    /**
     * Validates the state of the object.
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
