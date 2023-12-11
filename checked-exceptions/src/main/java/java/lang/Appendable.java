/*
 *     Copyright (C) 2015-2020 chronicle.software
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package java.lang;

import java.io.IOException;

public interface Appendable {

    /**
     * Appends the specified character sequence to this <code>Appendable</code>.
     * <p>
     * <p> Depending on which class implements the character sequence
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
     * <p> An invocation of this method of the form <code>out.append(csq, start,
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
