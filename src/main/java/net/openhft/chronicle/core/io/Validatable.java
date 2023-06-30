/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

/**
 * An interface that allows objects to be validated before they are written via a method writer.
 * Implementing classes should perform checks to ensure that the object's state is valid,
 * i.e., all required fields are properly initialized, values are within a valid range, etc.
 * <p>
 * This can be especially useful when dealing with Data Transfer Objects (DTOs) that require certain
 * conditions to be met before they are serialized or passed to another component.
 * <p>
 * The {@code validate} method should be implemented to perform the necessary validation and throw
 * an {@link InvalidMarshallableException} if the object is in an invalid state.
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
     * @throws RuntimeException              if an unexpected error occurs during validation.
     */
    void validate() throws InvalidMarshallableException;
}
