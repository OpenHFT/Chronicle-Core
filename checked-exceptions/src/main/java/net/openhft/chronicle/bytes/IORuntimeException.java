package net.openhft.chronicle.bytes;/*
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

@Deprecated(/* To be removed in x.23 */)
public class IORuntimeException extends Exception {
    public IORuntimeException(String message) {
        super(message);
    }

    public IORuntimeException(Throwable e) {
        super(e);
    }

    public IORuntimeException(String s, Throwable e) {
        super(s, e);
    }
}
