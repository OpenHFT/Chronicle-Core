/*
 *
 *  *     Copyright (C) ${YEAR}  higherfrequencytrading.com
 *  *
 *  *     This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU Lesser General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU Lesser General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU Lesser General Public License
 *  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package net.openhft.chronicle.core.pool;

/**
 * Created by peter on 15/02/16.
 */
public interface ClassLookup {

    static ClassLookup create(ClassLoader loader) {
        return ClassAliasPool.CLASS_ALIASES.wrap(loader);
    }

    default ClassLookup wrap(ClassLoader loader) {
        return new ClassAliasPool(this, loader);
    }

    Class forName(CharSequence name) throws ClassNotFoundException;

    String nameFor(Class clazz);

    void addAlias(Class... classes);

    void addAlias(Class clazz, String names);
}
