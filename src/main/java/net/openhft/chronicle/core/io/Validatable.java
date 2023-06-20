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
 * An interface for validating the state of an object before writing it via a method writer.
 * Implementing classes should perform checks to ensure that the object's state is valid.
 */
public interface Validatable {
    /**
     * Method which can be called when writing DTOs via the method writer.
     *
     * @throws InvalidMarshallableException If a value is null or out of range, indicating an invalid state.
     * @throws RuntimeException             or any other exception if the object is not considered valid.
     */
    void validate() throws InvalidMarshallableException;

}