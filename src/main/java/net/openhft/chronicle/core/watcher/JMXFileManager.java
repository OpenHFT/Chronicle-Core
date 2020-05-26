/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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
package net.openhft.chronicle.core.watcher;

import net.openhft.chronicle.core.Jvm;

import javax.management.*;
import java.lang.management.ManagementFactory;

public abstract class JMXFileManager implements FileManager, JMXFileManagerMBean {
    protected static final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    protected final String basePath, relativePath;
    private final ObjectName objectName;

    public JMXFileManager(String basePath, String relativePath) {
        this.basePath = basePath;
        this.relativePath = relativePath;
        try {
            objectName = new ObjectName(jmxPath() + ":type=" + type() + ",name=" + relativePath);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected String type() {
        return "basic";
    }

    protected String jmxPath() {
        return "chronicle";
    }

    public String getBasePath() {
        return basePath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public void start() {
        try {
            mbs.registerMBean(this, objectName);
        } catch (Exception e) {
            Jvm.warn().on(getClass(), "Unable to register " + this, e);
        }
    }

    @Override
    public void stop() {
        try {
            mbs.unregisterMBean(objectName);
        } catch (InstanceNotFoundException | MBeanRegistrationException e) {
            Jvm.warn().on(getClass(), "Unable to unregister " + this, e);
        }
    }
}
