/*
 *     Copyright (C) 2015  higherfrequencytrading.com
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

package net.openhft.chronicle.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by peter on 23/06/15.
 */
public enum ObjectUtils {
    ;


    public static <E> E convertTo(Class<E> eClass, Object o) throws ClassCastException {
        if (eClass.isInstance(o) || o == null) return (E) o;
        if (Enum.class.isAssignableFrom(eClass)) {
            return (E) Enum.valueOf((Class) eClass, o.toString());
        }
        if (o instanceof String) {
            try {
                Method valueOf = eClass.getDeclaredMethod("valueOf", String.class);
                valueOf.setAccessible(true);
                return (E) valueOf.invoke(null, o);
            } catch (InvocationTargetException | IllegalAccessException e) {
                ClassCastException cce = new ClassCastException();
                cce.initCause(e);
                throw cce;
            } catch (NoSuchMethodException e) {
            }
            try {
                Constructor<E> constructor = eClass.getDeclaredConstructor(String.class);
                constructor.setAccessible(true);
                return constructor.newInstance(o);
            } catch (Exception e) {
                ClassCastException cce = new ClassCastException();
                cce.initCause(e);
                throw cce;
            }
        }
        if (ReadResolvable.class.isAssignableFrom(eClass))
            return (E) o;
        throw new ClassCastException("Unable to convert " + o.getClass() + " " + o + " to " + eClass);
    }
}
