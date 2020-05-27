package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.annotation.UsedViaReflection;

public class VanillaReferenceOwner implements ReferenceOwner {
    private final String name;

    public VanillaReferenceOwner(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "VanillaReferenceOwner{" +
                "name='" + name + '\'' +
                '}';
    }

    @UsedViaReflection
    public boolean isClosed() {
        return false;
    }
}
