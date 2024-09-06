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

package net.openhft.chronicle.core.util;

/**
 * The {@code MisAlignedAssertionError} is thrown to indicate that an attempted memory operation
 * failed due to a misaligned memory address.
 * <p>
 * This error typically indicates a programming error in low-level memory manipulation.
 * For example, it is thrown by methods like {@code compareAndSwapInt} when the memory
 * address provided for a compare-and-swap operation is not properly aligned according
 * to the requirements of the underlying architecture or API.
 * 
 * <p>
 * As this error is an {@code AssertionError}, it is considered as an unchecked error.
 * 
 *
 * @see AssertionError
 */
public class MisAlignedAssertionError extends AssertionError {
}
