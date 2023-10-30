/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 * A RandomAccessFile must be explicitly close or cause a resources leak.
 * <p>
 * Weak references RAF can result in a resource leak when GC'ed which doesn't appear if the GC isn't running.
 */
public class CleaningRandomAccessFile extends RandomAccessFile {
    public CleaningRandomAccessFile(String name, String mode) throws FileNotFoundException {
        super(name, mode);
    }

    public CleaningRandomAccessFile(File file, String mode) throws FileNotFoundException {
        super(file, mode);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Closeable.closeQuietly(this);
    }
}
