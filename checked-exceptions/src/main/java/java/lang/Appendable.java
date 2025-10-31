/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.lang;

import java.io.IOException;

public interface Appendable {

    /**
     * Appends the specified character sequence to this <code>Appendable</code>.
     * <p>
     *  Depending on which class implements the character sequence
     * <code>csq</code>, the entire sequence may not be appended.  For
     * instance, if <code>csq</code> is a {@link java.nio.CharBuffer} then
     * the subsequence to append is defined by the buffer's position and limit.
     *
     * @param csq The character sequence to append.  If <code>csq</code> is
     *            <code>null</code>, then the four characters <code>"null"</code> are
     *            appended to this Appendable.
     * @return A reference to this <code>Appendable</code>
     * @throws IOException If an I/O error occurs
     */
    Appendable append(CharSequence csq) throws IOException;

    /**
     * Appends a subsequence of the specified character sequence to this
     * <code>Appendable</code>.
     * <p>
     *  An invocation of this method of the form <code>out.append(csq, start,
     * end)</code> when <code>csq</code> is not <code>null</code>, behaves in
     * exactly the same way as the invocation
     * <p>
     * <pre>
     *     out.append(csq.subSequence(start, end)) </pre>
     *
     * @param csq   The character sequence from which a subsequence will be
     *              appended.  If <code>csq</code> is <code>null</code>, then characters
     *              will be appended as if <code>csq</code> contained the four
     *              characters <code>"null"</code>.
     * @param start The index of the first character in the subsequence
     * @param end   The index of the character following the last character in the
     *              subsequence
     * @return A reference to this <code>Appendable</code>
     * @throws IndexOutOfBoundsException If <code>start</code> or <code>end</code> are negative, <code>start</code>
     *                                   is greater than <code>end</code>, or <code>end</code> is greater than
     *                                   <code>csq.length()</code>
     */
    Appendable append(CharSequence csq, int start, int end) throws IndexOutOfBoundsException;

    /**
     * Appends the specified character to this <code>Appendable</code>.
     *
     * @param c The character to append
     * @return A reference to this <code>Appendable</code>
     */
    Appendable append(char c) throws IllegalStateException;
}
