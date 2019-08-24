/*
 * Copyright (c) 2016-2019 Chronicle Software Ltd
 */

package net.openhft.chronicle.core.watcher;

import net.openhft.chronicle.core.annotation.UsedViaReflection;

@SuppressWarnings("unused")
public interface PlainFileManagerMBean {
    String getFileSize();
    String getContentType();
}
