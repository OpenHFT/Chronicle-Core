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
 * A {@code CleaningRandomAccessFile} is a subclass of {@link RandomAccessFile} that ensures
 * the file is properly closed when it is no longer in use, thereby preventing resource leaks.
 * <p>
 * This class overrides the {@link #finalize()} method to close the file quietly when the object
 * is garbage collected. This is particularly useful in scenarios where weak references to
 * {@code RandomAccessFile} could lead to resource leaks if the garbage collector does not
 * run frequently.
 * </p>
 * <p>
 * It is important to note that relying on {@code finalize()} for resource management is generally
 * discouraged because it adds overhead to garbage collection and may delay resource release.
 * Explicitly closing files using try-with-resources or manually invoking {@link #close()} is preferred.
 * </p>
 */
public class CleaningRandomAccessFile extends RandomAccessFile {

    /**
     * Creates a random access file stream to read from, and optionally to write to, a file with the specified name.
     *
     * @param name the system-dependent filename
     * @param mode the access mode, either "r" for read-only mode or "rw" for read-write mode
     * @throws FileNotFoundException if the file exists but is a directory rather than a regular file,
     *                               or cannot be opened or created for any other reason
     */
    public CleaningRandomAccessFile(String name, String mode) throws FileNotFoundException {
        super(name, mode);
    }

    /**
     * Creates a random access file stream to read from, and optionally to write to, a file with the specified {@link File} object.
     *
     * @param file the file object representing the file to be opened
     * @param mode the access mode, either "r" for read-only mode or "rw" for read-write mode
     * @throws FileNotFoundException if the file exists but is a directory rather than a regular file,
     *                               or cannot be opened or created for any other reason
     */
    public CleaningRandomAccessFile(File file, String mode) throws FileNotFoundException {
        super(file, mode);
    }

    /**
     * Ensures that the file is closed when the object is garbage collected.
     * <p>
     * This method overrides {@link Object#finalize()} to close the {@code RandomAccessFile} quietly
     * without throwing any {@code IOException}. This is a safeguard to prevent resource leaks if
     * the object is not closed explicitly.
     * </p>
     *
     * @throws Throwable if an exception occurs during the finalization process
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Closeable.closeQuietly(this);
    }
}
