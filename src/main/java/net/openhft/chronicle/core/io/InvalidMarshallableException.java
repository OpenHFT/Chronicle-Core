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
 * {@code InvalidMarshallableException} is thrown to indicate that an object being
 * serialized (marshalled) or deserialized (unmarshalled) is in an invalid state.
 * This could happen if the object fails to meet the required constraints, such as
 * a mandatory field being {@code null}, a value being out of its valid range, or
 * any other violation of the integrity rules.
 * <p>
 * This exception is particularly useful in scenarios where objects are being
 * converted to or from a different representation, such as serialization, and
 * you need to ensure that the object adheres to its contract or the defined
 * schema.
 * 
 * <p>
 * It can also be used in conjunction with the {@link Validatable} interface. When
 * an object implementing {@code Validatable} is validated using its {@code validate}
 * method, this exception can be thrown if the object does not meet the defined criteria.
 * 
 * <p>
 * Example usage:
 * <pre>
 * public class MyObject implements Validatable {
 *     private String someField;
 *
 *     {@literal @}Override
 *     public void validate() {
 *         if (someField == null) {
 *             throw new InvalidMarshallableException("someField cannot be null");
 *         }
 *         // ... other validations ...
 *     }
 * }
 * </pre>
 * 
 */
public class InvalidMarshallableException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    /**
     * Constructs an {@code InvalidMarshallableException} with the specified detail message.
     * The detail message is meant to provide more information on why the exception was thrown,
     * typically indicating which validation failed.
     *
     * @param msg The detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     */
    public InvalidMarshallableException(String msg) {
        super(msg);
    }
}
