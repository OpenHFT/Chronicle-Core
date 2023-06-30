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
 * The {@code Validatable} interface should be implemented by classes that require
 * validation of their state before being written through a method writer.
 * <p>
 * Implementing this interface indicates that the object is capable of self-validation,
 * which is essential in contexts like serialization or communication where the integrity
 * and correctness of an object's state are crucial.
 * </p>
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
 *         if (name == null || name.isEmpty()) {
 *             throw new InvalidMarshallableException("Name cannot be null or empty");
 *         }
 *         if (age == null || age < 0) {
 *             throw new InvalidMarshallableException("Age cannot be null or negative");
 *         }
 *     }
 * }
 * </pre>
 * </p>
 */
public interface Validatable {
    /**
     * Validates the state of the object. Implementations should perform checks to
     * ensure that the object's state is valid according to the defined constraints.
     *
     * @throws InvalidMarshallableException if a value is null or out of range, indicating an invalid state.
     * @throws RuntimeException             if the object is in an invalid state, a general RuntimeException or a custom exception can be thrown.
     */
    void validate() throws InvalidMarshallableException;

}