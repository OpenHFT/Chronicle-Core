/*
 * Copyright (c) 2016-2019 Chronicle Software Ltd
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
