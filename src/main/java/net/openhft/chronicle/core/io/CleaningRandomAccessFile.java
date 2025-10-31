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

package net.openhft.chronicle.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 * {@link RandomAccessFile} that quietly closes itself during finalisation if it
 * has not already been closed. This guards against leaks when a test forgets to
 * close the file, although callers should still use try-with-resources wherever
 * possible.
 * <p>
 * Finalisation is deprecated in recent JDKs so this class is only a safety net
 * for legacy code.
 */
public class CleaningRandomAccessFile extends RandomAccessFile {
    public CleaningRandomAccessFile(String name, String mode) throws FileNotFoundException {
        super(name, mode);
    }

    public CleaningRandomAccessFile(File file, String mode) throws FileNotFoundException {
        super(file, mode);
    }

    @SuppressWarnings({"deprecation", "removal"})
    @Override
    protected void finalize() throws Throwable {
        // best-efforts attempt to close the file if the owner forgot
        super.finalize();
        Closeable.closeQuietly(this);
    }
}
